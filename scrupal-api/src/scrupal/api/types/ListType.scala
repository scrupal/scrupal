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

package scrupal.api.types

import reactivemongo.bson.{BSONArray, BSONValue}
import scrupal.api._

/** A List type allows a non-exclusive list of elements of other types to be constructed
  *
  * @param id
  * @param description
  * @param elemType
  */
case class ListType  (
  id : Identifier,
  description : String,
  elemType : Type
  ) extends IndexableType {
  override type ScalaValueType = Seq[elemType.ScalaValueType]
  override def kind = 'List
  def apply(value: BSONValue) : ValidationResult = {
    value match {
      case a: BSONArray => validate(a.values, elemType)
      case x: BSONValue => wrongClass("BSONArray", x).map { s => Seq(s)}
    }
  }
}
