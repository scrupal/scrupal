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

package scrupal.models

import play.api.libs.json.{Json, JsObject}
import scrupal.models.db.{AlertKind, Alert}


/**
 * A context for the views so they can obtain various bits of information without a large number of parameters.
 */
case class Context(obj: JsObject = Json.obj())
{
  def alerts : Seq[Alert] = Seq()
  val appName : String = "Scrupal"
  val theme: String = "default"
}

/**
 * An instance of the Context with sensible defaults so the views can have a default object to use as the Context.
 */
object Context
{
  var active : Context = new Context() {
    val k = AlertKind.Note
  }
}
