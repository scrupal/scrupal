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

package scrupal.api.html

import scrupal.api.Context
import scrupal.api.Html._

import scalatags.Text.all._

trait Layout extends Template {
  def compose(context : Context)
}

case class DefaultLayout(args : ContentsArgs)
  extends BasicPage('DefaultLayout, "DefaultLayout", "Default Layout Page") {
  override val description = "Default layout page used when the expected layout could not be found"
  def bodyMain(context : Context, args : ContentsArgs = EmptyContentsArgs) : Contents = Seq(
    p(
      """A page defaultLayout was not selected for this information. As a result you are seeing the basic defaultLayout
        |which just lists the tag content down the page. This probably isn't what you want, but it's what you've got
        |until you create a defaultLayout for your pages.
      """.stripMargin),
    for ((key, frag) ← args) {
      frag match {
        case t : Template ⇒
          Seq(h1("Template: ", key, " - ", t.id.name, " - ", t.description), div(t.generate(context, args)))
        case x : Generator ⇒
          Seq(h1("Generator"), div(x.generate(context, args)))
      }
    }
  )
}
