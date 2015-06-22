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

case class SelectionType(
  id : Identifier,
  description : String,
  choices : Seq[String])(implicit val scrupal : Scrupal) extends Type[String] {
  override type ValueType = String
  require(choices.nonEmpty)
  def validate(ref : Location, value : String) : VResult = {
    simplify(ref, value, "BSONString") {
      case s: String if !choices.contains(s) ⇒ Some(s"Invalid selection. Options are: ${choices.mkString(", ")}")
      case s: String ⇒ None
      case _ ⇒ Some("")
    }
  }
}

