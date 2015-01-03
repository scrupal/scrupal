/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.core.types

import reactivemongo.bson.{BSONBinary, BSONString, BSONValue}
import scrupal.core.api._

/** A BLOB type has a specified MIME content type and a minimum and maximum length
  *
  * @param id
  * @param description
  * @param mime
  * @param maxLen
  */
case class BLOBType  (
  id : Identifier,
  description : String,
  mime : String,
  maxLen : Long = Long.MaxValue
  ) extends Type {
  override type ScalaValueType = Array[Byte]
  assert(maxLen >= 0)
  assert(mime.contains("/"))
  def validate(value: BSONValue) : BVR = simplify(value, "BSONBinary") {
    case b: BSONBinary if b.value.size > maxLen => Some(s"BLOB of length ${b.value.size} exceeds maximum length of ${maxLen}")
    case b: BSONBinary => None
    case b: BSONString if b.value.length > maxLen => Some(s"BLOB of length ${b.value.size} exceeds maximum length of ${maxLen}")
    case b: BSONString => None
    case x: BSONValue => Some("")
  }
  override def kind = 'BLOB

}
