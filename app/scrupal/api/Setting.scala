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

import play.api.libs.json._
import play.api.libs.json.JsObject

/** A named value for configuring a Module */
case class Setting(
  override val name: Symbol,
  valueType : Type,
  override val description: String,
  override val id : Option[Long] = None
) extends Thing[Setting](name, description) {
  def forId(id : Long) = Setting(name, valueType, description, Some(id))
  def validate(value : JsValue) = valueType.validate(value)
}


/** A named group of settings.
  * This allows settings to be categorized and clustered for better managability.
  */
case class SettingsGroup(
  override val name: Symbol,
  override val description: String,
  settings : Seq[Setting],
  override val id : Option[Long] = None
) extends Thing[SettingsGroup](name, description) with ObjectValidator {
  def forId(id: Long) = SettingsGroup(name, description, settings, Some(id))
  def validate( value : JsObject ) : JsResult[Boolean] = {
    val result : JsResult[Boolean] = ( value \ name.name ) match {
      case a: JsObject => {
        val structure : Map[Symbol,Type] = settings map { s => (s.name, s.valueType) } toMap  ;
        validate(a, structure)
      }
      case x: JsUndefined => JsError("Entry " + name.name + " was not found")
      case x => JsError("JsArray was expected, not " + x.getClass.getSimpleName )
    }
    result
  }
}
