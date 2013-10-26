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

import scrupal.views.html
import play.api.mvc._
import play.api.http.Writeable
import play.api.templates.Html
import scrupal.api.{Module, Type}
import play.api.libs.json.{Json, JsString}
import play.api.mvc.SimpleResult
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

/** One line sentence description here.
  * Further description here.
  */
trait ScrupalController extends Controller with ContextProvider {

  def notImplemented(what: JsString)(implicit writable: Writeable[JsString], request: RequestHeader) : SimpleResult = {
    NotImplemented(JsString("NotImplemented: " + what) )
  }

  def notImplemented(what:String)(implicit writable: Writeable[Html], request: RequestHeader ) : SimpleResult = {
    NotImplemented(html.errors.NotImplemented(spaces2underscores(what)))
  }

  def notFound(what:String, causes: Seq[String] = Seq(), suggestions : Seq[String] = Seq() )(implicit
      writable: Writeable[Html], request: RequestHeader) : SimpleResult = {
    NotFound(html.errors.NotFound(what, causes, suggestions))
  }

  def notFound(what: JsString)(implicit writable: Writeable[Html], request: RequestHeader) : SimpleResult = {
    NotFound(Json.obj( "error" -> "404: NOT_FOUND", "what" ->  what))
  }

  def movedPermanently(where: String)(implicit writable: Writeable[Html], request: RequestHeader) : SimpleResult = {
     MovedPermanently(where)
  }

  def forbidden(what: String, why: String)(implicit writable: Writeable[Html], request: RequestHeader): SimpleResult = {
    Forbidden(html.errors.Forbidden(what, why))
  }

  def spaces2underscores(what: String) = what.replaceAll(" ","_")

  def modules = Module.all
  def moduleNames : Seq[String]  = Module.all map { module: Module => module.label }
  def moduleTypeNames(mod:Module)  : Seq[String] = mod.types map { typ => typ.label }

  def types       : Seq[Type]    = Module.all flatMap { module => module.types }
  def typeNames   : Seq[String]  = types map { typ : Type => typ.label }

  def dateStr(millis: Long) : String = new DateTime(millis).toString(ISODateTimeFormat.dateTime)

}
