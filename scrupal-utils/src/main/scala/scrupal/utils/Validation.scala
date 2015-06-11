/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
  *                                                                                                                    *
  * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
  *                                                                                                           *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
  * with the License. You may obtain a copy of the License at                                                          *
  *                                                                                                                    *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                         *
  *                                                                                                                    *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
  * the specific language governing permissions and limitations under the License.                                     *
  * ********************************************************************************************************************
  */

package scrupal.utils

import play.api.libs.json._

object Validation {

  /** Base class of a location where validation can occur.
    * A location is just a name of something but permits member selection and index subscripting for nested locations.
    */
  sealed trait Location {
    def location : String

    def index(i : Int) : IndexedLocation = IndexedLocation(this, i)

    def select(s : String) : SelectedLocation = SelectedLocation(this, s)
  }

  /** The default valication location */
  object DefaultLocation extends Location { def location = "somewhere" }

  /** A Simple, non-nsted validation location */
  case class SimpleLocation(location : String) extends Location

  case class TypedLocation[VT](value : VT) extends Location {
    def location = value.toString
  }

  /** An index subscripted validation location */
  case class IndexedLocation(parent : Location, index : Int) extends Location {
    def location : String = s"${parent.location}[$index]"
  }

  /** A member selected valication location */
  case class SelectedLocation(parent : Location, key : String) extends Location {
    def location : String = s"${parent.location}.$key"
  }

  case class Exception[VR](result : Results[VR]) extends java.lang.Exception(result.message)

  /** The most abstract kind of validation result.
    *
    * Note that results can be accumulated with the add(vr)
    */
  sealed trait Results[VT] {
    def ref : Location

    def value : VT

    def isError : Boolean

    def tossOnError = {
      if (isError)
        throw new Exception(this)
    }

    def msgBldr : StringBuilder = { new StringBuilder }

    def message : String = {
      val bldr = msgBldr
      bldr.append(", at ").append(ref.location).append(".")
      bldr.toString()
    }

    def jsonMessage : JsObject

    def add(vr : Failure[VT]) : Results[VT] = {
      this match {
        case Success(oref, oval) ⇒ Failures(oref, oval, vr)
        case Failures(oref, oval, oerrors) ⇒ Failures(oref, oval, Seq(oerrors, vr) : _*)
        case x : Failure[VT] ⇒ Failures(ref, value, Seq(x, vr) : _*)
      }
    }

    def errorMap : Map[Location, Seq[Results[_]]]
  }

  /** What is returned when the validation succeeds */
  case class Success[VAL](ref : Location, value : VAL) extends Results[VAL] {
    def isError = false

    override def msgBldr = { super.msgBldr.append("Validation succeeded") }

    def errorMap = Map.empty[Location, Seq[Results[_]]]

    def jsonMessage = JsObject(Seq("form" → JsString(ref.location), "valid" → JsBoolean(value = true)))
  }

  /** Base class of the various kinds of error results */
  trait Failure[VAL] extends Results[VAL] {
    def ref : Location

    def isError = true

    def jsonMessage : JsObject = JsObject(Seq(ref.location → JsString(msgBldr.toString())))

    def errorMap : Map[Location, Seq[Results[_]]] = { Map(ref -> Seq(this)) }
  }

  /** Validation failure consisting of other error results */
  case class Failures[VAL, ET](ref : Location, value : VAL, errors : Failure[_]*) extends Failure[VAL] {

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

    override def errorMap : Map[Location, Seq[Results[_]]] = { errors.groupBy { vr ⇒ vr.ref } }
  }

  /** Validation failure with a simple error message */
  case class StringFailure[VT](ref : Location, value : VT, errMsg : String) extends Failure[VT] {
    override def msgBldr : StringBuilder = {
      super.msgBldr.append(errMsg)
    }
  }

  /** Validation failure with an exception */
  case class ThrowableFailure[VT](ref : Location, value : VT, cause : java.lang.Throwable) extends Failure[VT] {
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
    type VResult = Results[VType]

    /** Validate value of type VType with this validator
      *
      * @param ref The location at which the value occurs
      * @param value the VType to be validated
      * @return Any of the ValidationResults
      */
    def validate(ref : Location, value : VType) : VResult

    protected def simplify(ref : Location, value : VType, classes : String)(validator : (VType) ⇒ Option[String]) : VResult = {

      validator(value) match {
        case Some("") ⇒ wrongClass(ref, value, classes)
        case Some(msg : String) ⇒ StringFailure(ref, value, msg)
        case None ⇒ Success(ref, value)
      }
    }

    protected def wrongClass(ref : Location, value : VType, expected : String) : VResult = {
      StringFailure(ref, value, s"Expected value of type $expected but got ${value.getClass.getSimpleName} instead.")
    }
  }

  /** A Validator for a sequence (array, list, vector) of things
    * This traverses the sequence, delegates the validation of the sequence's elements, and collects the results
    * @tparam ET The element Type
    * @tparam ST
    */
  trait SeqValidator[ET, ST] extends Validator[ST] {

    def toSeq(st : ST) : Seq[ET]

    def validateElement(ref : IndexedLocation, v : ET) : Results[ET]

    def validate(ref : Location, value : ST) : VResult = {
      var idx : Int = -1
      val errors : Seq[Failure[ET]] = {
        for (
          v ← toSeq(value);
          e = validateElement(ref.index({ idx += 1; idx }), v) if e.isError
        ) yield { e.asInstanceOf[Failure[ET]] }
      }
      if (errors.isEmpty)
        Success(ref, value)
      else
        Failures[ST, ET](ref, value, errors : _*)
    }
  }

  trait MapValidator[KT, ET, MT] extends Validator[MT] {
    def toMap(mt : MT) : scala.collection.Map[KT, ET]

    def validateElement(ref : SelectedLocation, v : ET) : Results[ET]

    def validate(ref : Location, value : MT) : VResult = {
      val errors : Seq[Failure[ET]] = {
        for (
          (k, v) ← toMap(value);
          e = validateElement(ref.select(k.toString), v) if e.isError
        ) yield { e.asInstanceOf[Failure[ET]] }
      }.toSeq

      if (errors.isEmpty)
        Success(ref, value)
      else
        Failures[MT, ET](ref, value, errors : _*)
    }
  }

  trait StringMapValidator[ET] extends MapValidator[String, ET, Map[String, ET]]

  trait JsArrayValidator extends SeqValidator[JsValue, JsArray] {

    override def toSeq(st : JsArray) : Seq[JsValue] = st.value

    def validateElement(ref : IndexedLocation, v : JsValue) : Results[JsValue]
  }

  /** Generic Value Validator as a Function. You can apply these validations in other validations making them
    * composable.
    */
  trait JsObjectValidator extends MapValidator[String, JsValue, JsObject] {

    override def toMap(mt : JsObject) : scala.collection.Map[String, JsValue] = mt.value

    def validateElement(ref : SelectedLocation, v : JsValue) : Results[JsValue]
  }

}
