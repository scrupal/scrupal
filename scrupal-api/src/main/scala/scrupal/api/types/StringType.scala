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

import scrupal.api._
import scrupal.utils.Validation.Location

import scala.util.matching.Regex

/** A String type constrains a string by defining its content with a regular expression and a maximum length.
  *
  * @param id THe name of the string type
  * @param description A brief description of the string type
  * @param regex The regular expression that specifies legal values for the string type
  * @param maxLen The maximum length of this string type
  */
case class StringType(
  id : Identifier,
  description : String,
  regex : Regex,
  maxLen : Int = Int.MaxValue,
  patternName : String = "pattern"
)(implicit val scrupal : Scrupal) extends Type[String] {
  require(maxLen >= 0)
  def validate(ref : Location, value : String) = {
    simplify(ref, value, "String") {
      case s : String if s.length > maxLen ⇒
        Some(s"String of length ${s.length} exceeds maximum of $maxLen.")
      case s : String if !regex.pattern.matcher(s).matches() ⇒
        Some(s"'$s' does not match $patternName.")
      case s : String ⇒
        None
      case _ ⇒
        Some("")
    }
  }
  override def kind = 'String
}

