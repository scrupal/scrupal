/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
 *                                                                                                                    *
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
 * with the License. You may obtain a copy of the License at                                                          *
 *                                                                                                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                                                                     *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
 * the specific language governing permissions and limitations under the License.                                     *
 **********************************************************************************************************************/

package scrupal.api

import akka.http.scaladsl.model.{MediaType, MediaTypes}
import scrupal.api.Html._
import scrupal.utils.{Registrable, Registry}

import scalatags.Text.all._

/** Arranger Function.
  *
  * An arranger is a function that does the essential layout arrangement for a Layout. It takes in a Context and a
  * tag mapping and produces an `Array[Byte]` result. The Layout trait extends this function so that its apply method
  * can be used to perform the arranging.
  *
  */
trait Arranger extends ((ContentsArgs, Context) ⇒ Array[Byte])

/** Abstract Layout
  *
  * A layout is a memory only function object. It
  */
trait Layout extends Registrable[Layout] with Describable with Arranger {
  def mediaType : MediaType
  def registry = Layout
}

case class HtmlLayout(
  id : Symbol,
  description : String = "",
  template : Html.Template) extends Layout {
  val mediaType = MediaTypes.`text/html`
  def apply(args : ContentsArgs, context : Context) : Array[Byte] = {
    template.render(context, args).getBytes(utf8)
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
            case t : Html.Template ⇒
              Seq(h1("Template: ", key, " - ", t.id.name, " - ", t.description), div(t.generate(context, args)))
            case x : Html.Generator ⇒
              Seq(h1("Generator"), div(x.generate(context, args)))
          }
        }
      )
    }
  }

  lazy val default = HtmlLayout('default, "Default Layout", DefaultLayoutTemplate)

}
