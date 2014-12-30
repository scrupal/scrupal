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

import scrupal.core.api.Html.Contents

import scalatags.Text.all._
import scrupal.core.api._
import scrupal.core.api.Html._

abstract class BasicPage(the_title: String, the_description: String) extends Html.Page(the_title, the_description) {
  def headSuffix(context: Context) : Html.Contents = {
    implicit val ctxt : Context = context
    Seq(
      link(rel := "stylesheet", media := "screen", href := PathOf.theme(context.themeProvider, context.themeName)),
      link(rel := "stylesheet", href := PathOf.lib("font-awesome", "css/font-awesome.css"), media := "screen"),
      link(rel := "stylesheet", href := PathOf.css("scrupal"), media := "screen")
    )
  }

  def bodyPrefix(context: Context): Html.Contents = { display_alerts(context) }

  def bodySuffix(context: Context): Html.Contents = {
    if(Feature.enabled('DebugFooter, context.scrupal)){
      display_context_table(context)
    } else {
      Html.emptyContents
    }
  }
}

abstract class BootstrapPage(the_title: String, the_description: String)
  extends BasicPage(the_title, the_description)
{
  override def headSuffix(context: Context) : Html.Contents = {
    super.headSuffix(context) ++ Seq(
      jslib("jquery", "jquery.js"),
      jslib("bootstrap", "js/bootstrap.js")
    )
  }

  def body_content(context: Context) : Contents = {
    Seq(span(em("OOPS!"), " You forgot to override body_content!"))
  }

  override def bodyMain(context: Context) : Contents = {
    Seq(div(cls:="container", body_content(context)))
  }
}

abstract class MarkedPage(the_title: String, the_description: String)
  extends BootstrapPage(the_title, the_description)
{
  override def headSuffix(context: Context) = {
    super.headSuffix(context) ++ Seq(
      jslib("marked","marked.js")
    )
  }

  override def bodyMain(context: Context) : Contents = {
    Seq(div(scalatags.Text.all.id:="marked", body_content(context)))
  }

  override def bodySuffix(context: Context) :Contents = {
    Seq(js(
      """marked.setOptions({
        |  renderer: new marked.Renderer(),
        |  gfm: true,
        |  tables: true,
        |  breaks: true,
        |  pedantic: false,
        |  sanitize: false,
        |  smartLists: true,
        |  smartypants: false
        |});
        |var elem = document.getElementById('marked');
        |elem.innerHTML = marked(elem.innerHTML);""".stripMargin
    ))
  }
}

case class ForbiddenPage(what: String, why: String)
  extends BasicPage("Forbidden - " + what, "Forbidden Error Page") {
  val description = "A page for displaying an HTTP Forbidden error"

  def bodyMain(context: Context): Html.Contents = {
    danger(Seq(
      h1("Nuh Uh! I Can't Do That!"),
      p(em("Drat!"), s"Because $why, you can't $what. That's just the way it is."),
      p("You should try one of these options:"),
      ul(li("Type in another URL, or"), li("Try to get lucky with ",
        a(href := context.suggestURL.toString, "this suggestion")))
    ))()
  }
}

case class NotFoundPage(
  what: String,
  causes: Seq[String] = Seq(),
  suggestions: Seq[String] =Seq()
) extends BasicPage("Not Found - " + what, "Not Found Error Page") {
  def bodyMain(context: Context) : Contents = {
    warning(Seq(
      h1("There's A Hole In THe Fabrice Of The InterWebz!"),
      p(em("Oops!"), "We couldn't find ", what, ". That might be because:"),
      ul({for (c ← causes) yield {li(c)}},
        li("you used an old bookmark for which the resource is no longer available"),
        li("you mis-typed the web address.")
      ),
      p("You can try one of these options:"),
      ul({for (s ← suggestions) yield {li(s)}},
        li("type in another URL, or "),
        li("Try to get lucky with ", a(href := context.suggestURL.toString, "this suggestion"))
      )
    ))()
  }
}

abstract class GenericPlainPage(title: String, description: String) extends BasicPage(title, description) {
  def content(context: Context) : Html.Contents
  def bodyMain(context: Context) : Contents = Seq(
    div(cls:="container", content(context), debug_footer(context))
  )
}
case class PlainPage(title: String, description: String, the_content: Html.Contents)
  extends GenericPlainPage(title, description) {
  def content(context: Context) : Html.Contents = the_content
}
