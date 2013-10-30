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

package scrupal.controllers

import play.api.mvc.{SimpleResult, Results}
import scrupal.views.html
import play.api.libs.json.{Json, JsString}

/** A simple trait to provide some helper methods for Controllers.
  * These "Rich"Results transform the usual Results.XXX values into method calls that produce rich results based on
  * view templates that provide helpful information back to the user.
  */
trait RichResults extends Results {

  def NotImplemented(what: String)(implicit context: Context) : SimpleResult = {
    NotImplemented(html.errors.NotImplemented(what))
  }

  def NotFound(what: String, causes: Seq[String] = Seq(), suggestions : Seq[String] = Seq())(
    implicit context: Context) : SimpleResult = {
    NotFound(html.errors.NotFound(what, causes, suggestions))
  }

  def Forbidden(what: String, why: String)(implicit context: Context) : SimpleResult = {
    Forbidden(html.errors.Forbidden(what, why))
  }

}

/** A simple trait to provide some helper methods for JSON based controllers.
  * These "Rich"JsonResults provide ways to send error messages back to the caller in Json format.
  */
trait RichJsonResults extends RichResults {

  def NotImplemented(what: JsString)(implicit context: Context) : SimpleResult = {
    Results.NotImplemented(JsString("NotImplemented: " + what) )
  }

  def NotFound[A](what: JsString)(implicit context: Context) : SimpleResult = {
    Results.NotFound(Json.obj( "error" -> "404: NOT_FOUND", "what" ->  what))
  }
}
