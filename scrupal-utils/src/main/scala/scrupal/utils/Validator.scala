/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
  *                                                                                                               *
  * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
  *                                                                                                               *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
  * with the License. You may obtain a copy of the License at                                                          *
  *                                                                                                               *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                     *
  *                                                                                                               *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
  * the specific language governing permissions and limitations under the License.                                     *
  * ********************************************************************************************************************
  */

package scrupal.utils

import play.api.libs.json._

/** Base class of a location where validation can occur.
  * A location is just a name of something but permits member selection and index subscripting for nested locations.
  */
sealed trait ValidationLocation {
  def location : String
  def index(i : Int) : IndexedValidationLocation = IndexedValidationLocation(this, i)
  def select(s : String) : SelectedValidationLocation = SelectedValidationLocation(this, s)
}

/** The default valication location */
object DefaultValidationLocation extends ValidationLocation { def location = "somewhere" }

/** A Simple, non-nsted validation location */
case class SimpleValidationLocation(location : String) extends ValidationLocation

case class TypedValidationLocation[VT](value : VT) extends ValidationLocation {
  def location = value.toString
}

/** An index subscripted validation location */
case class IndexedValidationLocation(parent : ValidationLocation, index : Int) extends ValidationLocation {
  def location : String = s"${parent.location}[$index]"
}

/** A member selected valication location */
case class SelectedValidationLocation(parent : ValidationLocation, key : String) extends ValidationLocation {
  def location : String = s"${parent.location}.$key"
}

/** The most abstract kind of validation result.
  *
  * Note that results can be accumulated with the add(vr)
  */
sealed trait ValidationResults[VT] {
  def ref : ValidationLocation
  def value : VT
  def isError : Boolean
  def msgBldr : StringBuilder = { new StringBuilder }
  def message : String = {
    val bldr = msgBldr
    bldr.append(", at ").append(ref.location).append(".")
    bldr.toString()
  }
  def jsonMessage : JsObject
  def add(vr : ValidationErrorResults[VT]) : ValidationResults[VT] = {
    this match {
      case ValidationSucceeded(oref, oval) ⇒ ValidationFailed(oref, oval, vr)
      case ValidationFailed(oref, oval, oerrors) ⇒ ValidationFailed(oref, oval, Seq(oerrors, vr) : _*)
      case x : ValidationErrorResults[VT] ⇒ ValidationFailed(ref, value, Seq(x, vr) : _*)
    }
  }
  def errorMap : Map[ValidationLocation, Seq[ValidationResults[_]]]
}

/** What is returned when the validation succeeds */
case class ValidationSucceeded[VAL](ref : ValidationLocation, value : VAL) extends ValidationResults[VAL] {
  def isError = false
  override def msgBldr = { super.msgBldr.append("Validation succeeded") }
  def errorMap = Map.empty[ValidationLocation, Seq[ValidationResults[_]]]
  def jsonMessage = JsObject(Seq("form" → JsString(ref.location), "valid" → JsBoolean(value = true)))
}

/** Base class of the various kinds of error results */
trait ValidationErrorResults[VAL] extends ValidationResults[VAL] {
  def ref : ValidationLocation
  def isError = true
  def jsonMessage : JsObject = JsObject(Seq(ref.location → JsString(msgBldr.toString())))
  def errorMap : Map[ValidationLocation, Seq[ValidationResults[_]]] = { Map(ref -> Seq(this)) }
}

/** Validation failure consisting of other error results */
case class ValidationFailed[VAL, ET](ref : ValidationLocation, value : VAL, errors : ValidationErrorResults[_]*)
  extends ValidationErrorResults[VAL] {

  override def msgBldr : StringBuilder = {
    val s = super.msgBldr
    s.append("Failed to validate ").append(ref.location).append(": \n")
    for (err ← errors) {
      s.append(err.msgBldr).append("\n")
    }
    s
  }

  override def jsonMessage : JsObject = {
    val grouped = errors.groupBy { vr ⇒ vr.ref }
    JsObject(Seq(
      "location" → JsString(ref.location), "valid" → JsBoolean(value = false), "errors" → JsObject(
        grouped.map {
          case (ref, errs) ⇒ ref.location -> JsArray(errs.map { err ⇒ err.jsonMessage })
        }
      )
    ))
  }

  override def errorMap : Map[ValidationLocation, Seq[ValidationResults[_]]] = { errors.groupBy { vr ⇒ vr.ref } }
}

/** Validation failure with a simple error message */
case class ValidationError[VT](ref : ValidationLocation, value : VT, errMsg : String)
  extends ValidationErrorResults[VT] {
  override def msgBldr : StringBuilder = {
    super.msgBldr.append(errMsg)
  }
}

/** Validation failure with an exception */
case class ValidationException[VT](ref : ValidationLocation, value : VT, cause : Throwable)
  extends ValidationErrorResults[VT] {
  override def msgBldr : StringBuilder = {
    super.msgBldr.append(cause.getClass.getName).append(": ").append(cause.getMessage)
  }
}

/** The Validator of a type of thing
  *
  * @tparam VType The type of the thing being validated
  */
trait Validator[VType] {

  /** A Type alias for the ValidationResult, for brevity  */
  type VResult = ValidationResults[VType]

  /** Validate value of type VType with this validator
    *
    * @param ref The location at which the value occurs
    * @param value the VType to be validated
    * @return Any of the ValidationResults
    */
  def validate(ref : ValidationLocation, value : VType) : VResult

  protected def simplify(ref : ValidationLocation, value : VType, classes : String)(validator : (VType) ⇒ Option[String]) : VResult = {
    validator(value) match {
      case Some("") ⇒ wrongClass(ref, value, classes)
      case Some(msg : String) ⇒ ValidationError(ref, value, msg)
      case None ⇒ ValidationSucceeded(ref, value)
    }
  }

  protected def wrongClass(ref : ValidationLocation, value : VType, expected : String) : VResult = {
    ValidationError(ref, value, s"Expected value of type $expected but got ${value.getClass.getSimpleName} instead.")
  }
}

/** A Validator for a sequence (array, list, vector) of things
  * This traverses the sequence, delegates the validation of the sequence's elements, and collects the results
  * @tparam ET The element Type
  * @tparam ST
  */
trait SeqValidator[ET, ST] extends Validator[ST] {

  def toSeq(st : ST) : Seq[ET]

  def validateElement(ref : IndexedValidationLocation, v : ET) : ValidationResults[ET]

  def validate(ref : ValidationLocation, value : ST) : VResult = {
    var idx : Int = -1
    val errors : Seq[ValidationErrorResults[ET]] = {
      for (
        v ← toSeq(value);
        e = validateElement(ref.index({ idx += 1; idx }), v) if e.isError
      ) yield { e.asInstanceOf[ValidationErrorResults[ET]] }
    }
    if (errors.isEmpty)
      ValidationSucceeded(ref, value)
    else
      ValidationFailed[ST, ET](ref, value, errors : _*)
  }
}

trait MapValidator[KT, ET, MT] extends Validator[MT] {
  def elementValidator : Validator[ET]
  def toMap(mt : MT) : scala.collection.Map[KT, ET]
  def default : MT

  def validateElement(ref : SelectedValidationLocation, v : ET) : ValidationResults[ET]

  def validate(ref : ValidationLocation, value : MT) : VResult = {
    val errors : Seq[ValidationErrorResults[ET]] = {
      for (
        (k, v) ← toMap(value);
        e = validateElement(ref.select(k.toString), v) if e.isError
      ) yield { e.asInstanceOf[ValidationErrorResults[ET]] }
    }.toSeq

    if (errors.isEmpty)
      ValidationSucceeded(ref, value)
    else
      ValidationFailed[MT, ET](ref, value, errors : _*)
  }
}

trait JsArrayValidator extends SeqValidator[JsValue, JsArray] {

  override def toSeq(st : JsArray) : Seq[JsValue] = st.value

  def validateElement(ref : IndexedValidationLocation, v : JsValue) : ValidationResults[JsValue]
}

/** Generic Value Validator as a Function. You can apply these validations in other validations making them
  * composable.
  */
trait JsObjectValidator extends MapValidator[String, JsValue, JsObject] {

  override def toMap(mt : JsObject) : scala.collection.Map[String, JsValue] = mt.value

  def validateElement(ref : SelectedValidationLocation, v : JsValue) : ValidationResults[JsValue]
}
