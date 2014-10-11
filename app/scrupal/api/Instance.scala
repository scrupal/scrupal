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

package scrupal.api

import org.joda.time.DateTime
import play.api.libs.json.{Json, JsObject}
import scrupal.utils.Jsonic

/** The basic unit of storage and operation in Scrupal
  * Further description here.
  */
case class Instance(
  override val id : Identifier,
  override val name: Symbol,
  override val description: String,
  entityId: Identifier,
  payload: JsObject,
  override val modified : Option[DateTime] = None,
  override val created : Option[DateTime] = None
) extends Thing with Jsonic {
  def fromJson(js: play.api.libs.json.JsObject): Unit = ???
  def toJson: play.api.libs.json.JsObject = ???

}

object Instance {
  implicit val Instance_Formats = Json.format[Instance]
}

