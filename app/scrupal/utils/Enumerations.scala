/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

package scrupal.utils

import play.api.data.format.Formatter
import play.api.data.{FormError, Forms, Mapping}
import play.api.libs.json._
import play.api.libs.json.JsSuccess
import play.api.data.FormError
import play.api.libs.json.JsString
import scala.util.{Failure, Success, Try}


/** Utilities for making Enumerations easier to use with Forms.
  *
  */
object Enumerations {

  /** Produce a Json Reads object for an Enumeration
    * @param enum The enumeration for which values should be read
    * @tparam E The type of enumeration to read.
    * @return The JSON Reads object that will read the enumeration from a JsString
    */
  implicit def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = new Reads[E#Value] {
    def reads(jsonValue: JsValue): JsResult[E#Value] = {
      jsonValue match {
        case JsString(s) => {
          Try {
            JsSuccess(enum.withName(s))
          } match {
            case Success(e) => e
            case Failure(x) => JsError("No value " + s + " for enumeration '" + enum.getClass().getSimpleName() + ":" +
              x.getMessage())
          }
        }
        case _ => JsError("A string value for enumeration type '" + enum.getClass() + "' was expected")
      }
    }
  }

  /** Produce a Json Writes object for an Enumeration
    * @tparam E The type of enumeration to write
    * @return A JSON Writes object that will write the enumeration as a JsString
    */
  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
    def writes(v: E#Value): JsValue = JsString(v.toString)
  }

  /** A Json Format object for reading/writing Enumerations of a particular type
    * @param enum The enumeration object for which values should be read or written
    * @tparam E The type of the enumeration object
    * @return The JON Format object that will read/write the enumeration to and from a JsString
    */
  implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = Format(enumReads(enum), enumWrites)

  /** Define a new kind of form element using the play.api.data.Mapping class.
    * This lets us map an enumeration of our choice to a form field by just using the Enumeration object as an
    * argument to the enum function.
    * @param enum The enumeration you want to map
    * @tparam E The kind of enumeration you want to map, generally computed implicitly
    * @return The mapping for the enumeration's Value type.
    */
  def enum[E <: Enumeration](enum: E): Mapping[E#Value] = Forms.of(formatEnumeration(enum))

  /** How to format an enumeration of a given type.
    * We just convert them to strings.
    * @param enum
    * @tparam E
    * @return
    */
  def formatEnumeration[E <: Enumeration](enum: E): Formatter[E#Value] = {
    new Formatter[E#Value] {
      def bind(key: String, data: Map[String, String]) = {
        play.api.data.format.Formats.stringFormat.bind(key, data).right.flatMap { s =>
          scala.util.control.Exception.allCatch[E#Value]
            .either(enum.withName(s))
            .left.map(e => Seq(FormError(key, "error.enum", Nil)))
        }
      }
      def unbind(key: String, value: E#Value) = Map(key -> value.toString)
    }
  }

}
