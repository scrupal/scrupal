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

import reactivemongo.bson._

trait ValidationLocation {
  def index(key: Int) : Option[ValidationLocation] = Some(this)
  def get(key: String) : Option[ValidationLocation] = Some(this)
  def location: String
}

object SomeValidationLocation extends ValidationLocation { def location = "something" }

sealed trait ValidationResults[VAL] {
  def ref: ValidationLocation
  def value: VAL
  def isError : Boolean
  def message : StringBuilder
  def bsonMessage : BSONDocument
  def add(vr: ValidationErrorResults[VAL]) : ValidationResults[VAL] = {
    this match {
      case ValidationSucceeded(oref, oval) ⇒ ValidationFailed(oref, oval, Seq(vr))
      case ValidationFailed(oref, oval, oerrors) ⇒ ValidationFailed(oref, oval, oerrors :+ vr)
      case x: ValidationErrorResults[VAL] ⇒ ValidationFailed(ref, value, Seq(x, vr))
    }
  }
  def errorMap: Map[ValidationLocation, Seq[String]]
}

case class ValidationSucceeded[VAL](ref: ValidationLocation, value: VAL) extends ValidationResults[VAL] {
  def isError = false
  def message = new StringBuilder("Validation of ").append(ref.location).append(" succeeded.")
  def errorMap = Map.empty[ValidationLocation,Seq[String]]
  def bsonMessage = BSONDocument("form" → BSONString(ref.location), "valid" → BSONBoolean(value=true))
}

sealed trait ValidationErrorResults[VAL] extends ValidationResults[VAL] {
  def ref: ValidationLocation
  def isError = true
  def message : StringBuilder  = {
    new StringBuilder("\nFailed to validate ").append(ref.location).append(": ")
  }
  def errorMap = Map(ref → Seq(message.toString()))
  def bsonMessage : BSONDocument = BSONDocument( ref.location → BSONString( message.toString() ) )
}

case class ValidationFailed[VAL](ref: ValidationLocation, value: VAL, errors: Seq[ValidationErrorResults[VAL]])
  extends ValidationErrorResults[VAL] {
  override def message : StringBuilder = {
    val s = new StringBuilder
    for (err ← errors) {
      s.append(err.message).append("\n")
    }
    s
  }
  override def bsonMessage : BSONDocument = {
    val grouped = errors.groupBy { vr ⇒ vr.ref }
    BSONDocument(
      "form" → BSONString(ref.location), "valid" → BSONBoolean(value=false), "errors" → BSONDocument(
        grouped.map { case ( ref, errs)  ⇒ ref.location -> BSONArray(
          errs.map { err ⇒ err.bsonMessage.stream.head.getOrElse(""→BSONString("Unspecified error"))._2 }
        ) }
      )
    )
  }
  override def errorMap = {
    val grouped = errors.groupBy { vr ⇒ vr.ref }
    for ((ref,errs) ← grouped) yield {
      ref → errs.map { e ⇒ e.message.toString() }
    }
  }
}

object ValidationFailed {
  def apply[VAL](ref: ValidationLocation, value: VAL, error: ValidationErrorResults[VAL]) = {
    new ValidationFailed[VAL](ref, value, Seq(error))
  }
}

case class ValidationError[VAL](ref: ValidationLocation, value: VAL, errMsg: String)
  extends ValidationErrorResults[VAL] {
  override def message : StringBuilder = {
    super.message.append(errMsg)
  }
}

case class ValidationException[VAL](ref: ValidationLocation, value: VAL, cause: Throwable)
  extends ValidationErrorResults[VAL] {
  override def message : StringBuilder = {
    super.message.append(cause.getClass.getName).append(": ").append(cause.getMessage)
  }
}

case class TypeValidationError[VAL, T <: Type](ref: ValidationLocation, value: VAL, t: T, errors: Seq[String])
  extends ValidationErrorResults[VAL]
{
  override def message: StringBuilder = {
    val s = super.message.append("value does not conform to ").append(t.label).append(":\n")
    for (err <- errors) { s.append("\t").append(err).append("\n") }
    s.deleteCharAt(s.length-1)
  }
}

object TypeValidationError {
  def apply[VAL,T <: Type](ref: ValidationLocation, value: VAL, t: T, error: String) = {
    new TypeValidationError(ref, value, t, Seq(error))
  }
}

trait Validator[VAL] {
  /** A Type alias for brevity */
  type VR = ValidationResults[VAL]

  /** Validate value of BSON type B with this validator
    *
    * @param value the BSONValue to be validated
    * @return Any of the ValidationResults
    */
  def validate(ref: ValidationLocation, value: VAL) : VR

  protected def simplify(ref: ValidationLocation, value: VAL, classes: String)
    (validator: (VAL) => Option[String]): VR = {
    validator(value) match {
      case Some("") ⇒ wrongClass(ref, value, classes)
      case Some(msg: String) ⇒ ValidationError(ref, value,  msg)
      case None ⇒ ValidationSucceeded(ref, value)
    }
  }

  protected def wrongClass(ref: ValidationLocation, value: VAL, expected: String) : VR = {
    ValidationError(ref, value, s"Expected value of type $expected but got ${value.getClass.getSimpleName} instead.")
  }
}

/** Generic Value Validator as a Function. You can apply these validations in other validations making them
  * composable.
 */
trait BSONValidator extends Validator[BSONValue] {

  /** The validation method for validating a BSONArray
    * This traverses the array and validates that each element conforms to the Validator provided
    * @param value The BSONArray to be validated
    * @param validator The validator each element of the array will be validated against.
    * @return Any of the ValidationResults
    */
  protected def validateArray(ref: ValidationLocation, value : BSONArray, validator: BSONValidator) : VR = {
    val errors : Seq[ValidationErrorResults[BSONValue]] = {
      for (
        v <- value.values;
        e = validator.validate(ref, v.asInstanceOf[BSONValue]) if e.isError
      ) yield { e.asInstanceOf[ValidationErrorResults[BSONValue]] }
    }
    if (errors.isEmpty)
      ValidationSucceeded(ref, value)
    else
      ValidationFailed(ref, value, errors)
  }

  protected def validateMaps(
    ref: ValidationLocation,
    value: BSONValue,
    validators: Map[String, BSONValidator],
    defaults: BSONDocument
  ) : VR = {
    value match {
      case doc: BSONDocument ⇒
        val elems = doc.elements.toMap // read it all in once, we'll look at everything in the typical case
        val combined = for (
          (key,validator) ← validators
        ) yield {
          elems.get(key) match {
            case Some(v) ⇒ validator.validate(ref, elems.get(key).get)
            case None ⇒ {
              defaults.get(key) match {
                case Some(v) ⇒ validator.validate(ref, v)
                case None ⇒ ValidationError(ref, value, s"Element '$key' is missing and has no default.")
              }
            }
          }
        }
        val errors : Seq[ValidationErrorResults[BSONValue]] = {
          combined.filter { vr ⇒ vr.isError} map { vr ⇒ vr.asInstanceOf[ValidationErrorResults[BSONValue]]}
        }.toSeq
        if (errors.isEmpty)
          ValidationSucceeded(ref, value)
        else
          ValidationFailed(ref, value, errors.toSeq)
      case x: BSONValue ⇒ wrongClass(ref, x, "BSONDocument")
    }
  }
}
