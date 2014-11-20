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

package scrupal.api

import play.twirl.api._
import scrupal.utils.{Registry, Registrable}
import spray.http.{MediaTypes, MediaType}

/** Arranger Function.
  *
  * An arranger is a function that does the essential layout arrangement for a Layout. It takes in a Context and a
  * tag mapping and produces an `Array[Byte]` result. The Layout trait extends this function so that its apply method
  * can be used to perform the arranging.
  *
  */
trait Arranger[P] extends ( (Map[String,(Node,Result[_])], Context) => Result[P] )

/** Abstract Layout
  *
  * A layout is a memory only function object. It
  */
trait Layout[P] extends Registrable[Layout[_]] with Describable with Arranger[P] {
  def mediaType : MediaType
  def registry = Layout
  def asT : this.type = this
}
case class TwirlHtmlLayout(
  id : Symbol,
  description : String = "",
  template: Layout.TwirlHtmlLayoutFunction
) extends Layout[Html] {
  val mediaType = MediaTypes.`text/html`
  def apply(args: Map[String,(Node,Result[_])], context: Context) : Result[Html] = {
    HtmlResult(template(args)(context), Successful)
  }
}

case class TwirlTxtLayout(
  id : Symbol,
  description : String = "",
  template: Layout.TwirlTxtLayoutFunction
) extends Layout[String] {
  val mediaType = MediaTypes.`text/plain`
  def apply(args: Map[String,(Node,Result[_])], context: Context) : Result[String] = {
    StringResult(template(args)(context).body)
  }
}

object Layout extends Registry[Layout[_]] {
  def registryName = "Layouts"
  def registrantsName = "layout"


  type TwirlHtmlLayoutFunction = { def apply(args: Map[String,(Node,Result[_])])(implicit ctxt: Context):Html }
  type TwirlTxtLayoutFunction = { def apply(args: Map[String,(Node,Result[_])])(implicit ctxt: Context):Txt }

  lazy val default = TwirlHtmlLayout('default, "Default Layout", scrupal.core.views.html.layout.defaultLayout)
}


