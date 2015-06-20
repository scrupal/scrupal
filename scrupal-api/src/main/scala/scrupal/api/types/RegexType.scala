/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
 *                                                                                                                    *
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
 * with the License. You may obtain a copy of the License at                                                          *
 *                                                                                                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                                                                     *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
 * the specific language governing permissions and limitations under the License.                                     *
 **********************************************************************************************************************/

package scrupal.api.types

import java.util.regex.Pattern

import scrupal.utils.Validation.Location

import scrupal.api._

import scala.util.matching.Regex
import scala.util.{ Failure, Success, Try }

/** A type for a regular expression
  *
  * @param id The name of the Regex type
  * @param description A brief description of the regex type
  */
case class RegexType(
  id : Identifier,
  description : String) extends Type[String] {
  def validate(ref : Location, value : String) : VResult = {
    simplify(ref, value, "String") {
      case s: String ⇒ Try {
        Pattern.compile(s)
      } match {
        case Success(x) ⇒ None
        case Failure(x) ⇒ Some(s"Error in pattern: ${x.getClass.getName}: ${x.getMessage}")
      }
      case _ ⇒ Some("") // A signal to simplify that its the wrong class
    }
  }
}

object Regex_t
  extends RegexType('Regex, "Regular expression type")

