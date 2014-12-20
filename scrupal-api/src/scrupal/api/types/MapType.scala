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

import scrupal.api._

/** A Map is a set whose elements are named with an arbitrary string
  *
  * @param id
  * @param description
  * @param elemType
  */
case class MapType  (
  override val id : Identifier,
  description : String,
  elemType : Type
) extends DocumentType {
  override type ScalaValueType = Map[Identifier, elemType.ScalaValueType]
  override def kind = 'Map
  def validatorFor(id: String) : Option[Type] = Some(elemType)
  def fieldNames: Seq[String] = Seq.empty[String]
}
