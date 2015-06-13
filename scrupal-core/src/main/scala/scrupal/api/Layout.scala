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

import reactivemongo.bson.{BSONString, BSONHandler}
import scrupal.api.Html._
import scrupal.core.html.{BasicPage, DefaultLayout}
import scrupal.utils.{Registry, Registrable}
import spray.http.{MediaTypes, MediaType}

import scalatags.Text.all._

/** Arranger Function.
  *
  * An arranger is a function that does the essential layout arrangement for a Layout. It takes in a Context and a
  * tag mapping and produces an `Array[Byte]` result. The Layout trait extends this function so that its apply method
  * can be used to perform the arranging.
  *
  */
trait Arranger extends ( (ContentsArgs, Context) => Array[Byte] )

/** Abstract Layout
  *
  * A layout is a memory only function object. It
  */
trait Layout extends Registrable[Layout] with Describable with Arranger {
  def mediaType : MediaType
  def registry = Layout
  def asT : this.type = this
}

case class HtmlLayout(
  id : Symbol,
  description : String = "",
  template: Html.Template
) extends Layout {
  val mediaType = MediaTypes.`text/html`
  def apply(args: ContentsArgs, context: Context) : Array[Byte] = {
    template.render(context,args).getBytes(utf8)
  }
}

object Layout extends Registry[Layout] {
  def registryName = "Layouts"

  def registrantsName = "layout"

  object DefaultLayoutTemplate extends Html.Template('DefaultLayoutTemplate) {
    def description = "Default layout page used when the expected layout could not be found"
    def apply(context : Context, args : ContentsArgs) : Contents = {
      Seq(
        p(
          """A page defaultLayout was not selected for this information. As a result you are seeing the basic defaultLayout
            |which just lists the tag content down the page. This probably isn't what you want, but it's what you've got
            |until you create a defaultLayout for your pages.
          """.stripMargin),
        for ((key, frag) ← args) {
          frag match {
            case t: Html.Template ⇒
              Seq(h1("Template: ", key, " - ", t.id.name, " - ", t.description), div(t.generate(context,args)))
            case x: Html.Generator ⇒
              Seq(h1("Generator"), div(x.generate(context,args)))
          }
        }
      )
    }
  }

  lazy val default = HtmlLayout('default, "Default Layout", DefaultLayoutTemplate)

  class BSONHandlerForLayout[T <: Layout] extends BSONHandler[BSONString,T] {
    override def write(t: T): BSONString = BSONString(t.id.name)
    override def read(bson: BSONString): T = Layout.as(Symbol(bson.value))
  }
}
