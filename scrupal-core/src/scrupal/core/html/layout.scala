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

package scrupal.core.html

import scrupal.core.api.Html._
import scrupal.core.api.Context

import scalatags.Text.all._

trait Layout extends Template {
  def compose(context: Context)
}

case class DefaultLayout(args: ContentsArgs)
  extends BasicPage('DefaultLayout, "DefaultLayout", "Default Layout Page")
{
  override val description = "Default layout page used when the expected layout could not be found"
  def bodyMain(context: Context, args: ContentsArgs = EmptyContentsArgs) : Contents = Seq(
    p(
      """A page defaultLayout was not selected for this information. As a result you are seeing the basic defaultLayout
        |which just lists the tag content down the page. This probably isn't what you want, but it's what you've got
        |until you create a defaultLayout for your pages.
      """.stripMargin),
    for ( (key, frag) ← args) {
      frag match {
        case t: Template ⇒
          Seq(h1("Template: ", key, " - ", t.id.name, " - ", t.description), div(t.generate(context,args)))
        case x: Generator ⇒
          Seq(h1("Generator"), div(x.generate(context,args)))
      }
    }
  )
}
