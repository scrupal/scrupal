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

import akka.http.scaladsl.model.MediaType
import scrupal.api._
import scrupal.utils.Validation.Location

/** A BLOB type has a specified MIME content type and a minimum and maximum length
  *
  * @param id
  * @param description
  * @param mediaType
  * @param maxLen
  */
case class BLOBType(
  id : Identifier,
  description : String,
  mediaType : MediaType,
  maxLen : Long = Long.MaxValue) extends Type[Array[Byte]] {
  assert(maxLen >= 0)
  def validate(ref : Location, value : Array[Byte]) : VResult = {
    simplify(ref, value, "Array[Byte]") {
      case b: Array[Byte] if b.length > maxLen ⇒ Some(s"BLOB of length ${b.length} exceeds maximum length of $maxLen")
      case b: Array[Byte] ⇒ None
      case _ ⇒ Some("")
    }
  }
  override def kind = 'BLOB

}
