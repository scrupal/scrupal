/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.core.api

import reactivemongo.bson.{BSONArray, BSONDocument, BSONValue}

sealed trait ValidationResults[T] {
  def value: T
  def isError : Boolean
  def message : String
  def add(vr: ValidationResults[T]) : ValidationResults[T] = {
    this match {
      case ValidationSucceeded(value) ⇒ ValidationFailed(value, Seq(vr))
      case ValidationFailed(value, errors) ⇒ ValidationFailed(value, errors :+ vr)
      case x: ValidationError[T] ⇒ ValidationFailed(vr.value, Seq(vr, x))
      case x: ValidationException[T] ⇒ ValidationFailed(vr.value, Seq(vr, x))
      case x: TypeValidationError[_] ⇒ ValidationFailed(vr.value, Seq(vr, x))
    }
  }
}

case class ValidationSucceeded[T](value: T) extends ValidationResults[T] {
  def isError = false
  def message = ""
}

case class ValidationFailed[T](value: T, errors: Seq[ValidationResults[T]]) extends ValidationResults[T] {
  def isError = true
  def message : String = errors.mkString("\n")
}

case class ValidationError[T](value: T, errors: Seq[String]) extends ValidationResults[T]
{
  def isError = true
  def message: String = {
    s"Failed to validate value $value:\n ${errors.map{ e => "\t"+e+"\n"}}\n"
  }
}

object ValidationError {
  def apply[T](value: T, error: String) = new ValidationError(value, Seq(error))
}

case class ValidationException[T](value: T, cause: Throwable) extends ValidationResults[T] {
  def isError = true
  def message : String = {
    s"Failed to validate value $value: ${cause.getClass.getName}: ${cause.getMessage}"
  }
}

case class TypeValidationError[T <: Type](value: BSONValue, t: T, errors: Seq[String])
  extends ValidationResults[BSONValue]
{
  def isError = true
  def message: String = {
    s"Value, $value, does not conform to ${t.label}:\n ${errors.map{ e => "\t"+e+"\n"}}\n"
  }
}

object TypeValidationError {
  def apply[T <: Type](value: BSONValue, t: T, error: String) = new TypeValidationError(value, t, Seq(error))
}

trait SelfValidator[T] {
  def validate() : ValidationResults[T]
}

/** Generic Value Validator as a Function. You can apply these validations in other validations making them
  * composable.
 */
trait BSONValidator {

  /** A Type alias for brevity */
  type BVR = ValidationResults[BSONValue]

  protected def simplify(value: BSONValue, classes: String)(validator: (BSONValue) => Option[String]): BVR = {
    validator(value) match {
      case Some("") ⇒ wrongClass(value, classes)
      case Some(msg: String) ⇒ ValidationError(value, msg)
      case None ⇒ ValidationSucceeded(value)
    }
  }

  protected def wrongClass(value: BSONValue, expected: String) : BVR = {
    ValidationError(value, s"Expected value of type $expected but got ${value.getClass.getSimpleName} instead.")
  }

  /** Validate value of BSON type B with this validator
    *
    * @param value the BSONValue to be validated
    * @return Any of the ValidationResults
    */
  def validate(value: BSONValue) : BVR

  /** The validation method for validating a BSONArray
    * This traverses the array and validates that each element conforms to the Validator provided
    * @param value The BSONArray to be validated
    * @param validator The validator each element of the array will be validated against.
    * @return Any of the ValidationResults
    */
  protected def validateArray(value : BSONArray, validator: BSONValidator) : BVR = {
    val errors = {
      for (
        v <- value.values;
        e = validator.validate(v.asInstanceOf[BSONValue]) if e.isError
      ) yield { e }
    }
    if (errors.isEmpty)
      ValidationSucceeded(value)
    else
      ValidationFailed(value, errors)
  }

  protected def validateMaps(
    document: BSONValue,
    validators: Map[String,BSONValidator],
    defaults: BSONDocument
  ) : BVR = {
    document match {
      case doc: BSONDocument ⇒
        val elems = doc.elements.toMap // read it all in once, we'll look at everything in the typical case
        val combined = for (
          (key,validator) ← validators
        ) yield {
          if (elems.contains(key)) {
            validator.validate(elems.get(key).get)
          } else if (defaults.elements.contains(key)) {
            validator.validate(defaults.get(key).get)
          } else {
            ValidationError(document, s"Element '$key' is missing and has no default.")
          }
        }
        val errors = combined.filter { vr ⇒ vr.isError }
        if (errors.isEmpty)
          ValidationSucceeded(document)
        else
          ValidationFailed(document, errors.toSeq)
      case x: BSONValue ⇒ wrongClass(x, "BSONDocument")
    }
  }
}
