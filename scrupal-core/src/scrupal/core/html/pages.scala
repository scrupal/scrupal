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

trait BasicPageGenerator extends PageGenerator {
  def headSuffix(context: Context, args: ContentsArgs) : Html.Contents = {
    implicit val ctxt : Context = context
    Seq(
      link(rel := "stylesheet", media := "screen", href := PathOf.theme(context.themeProvider, context.themeName)),
      link(rel := "stylesheet", href := PathOf.lib("font-awesome", "css/font-awesome.css"), media := "screen"),
      link(rel := "stylesheet", href := PathOf.css("scrupal"), media := "screen")
    )
  }

  def bodyPrefix(context: Context, args: ContentsArgs): Html.Contents = { display_alerts(context) }

  def bodySuffix(context: Context, args: ContentsArgs): Html.Contents = {
    if(Feature.enabled('DebugFooter, context.scrupal)){
      display_context_table(context)
    } else {
      Html.emptyContents
    }
  }
}

abstract class BasicPage(
  override val id: Symbol,
  override val title: String,
  override val description: String
) extends TemplatePage(id, title, description) with BasicPageGenerator


trait BootstrapPageGenerator extends BasicPageGenerator {
  override def headSuffix(context: Context, args: ContentsArgs) : Html.Contents = {
    super.headSuffix(context) ++ Seq(
      jslib("jquery", "jquery.js"),
      jslib("bootstrap", "js/bootstrap.js")
    )
  }

  def body_content(context: Context, args: ContentsArgs) : Contents = {
    Seq(span(em("OOPS!"), " You forgot to override body_content!"))
  }

  override def bodyMain(context: Context, args: ContentsArgs) : Contents = {
    Seq(div(cls:="container", body_content(context, args)))
  }
}

abstract class BootstrapPage(
  override val id: Symbol,
  override val title: String,
  override val description: String
) extends BasicPage(id, title, description) with BootstrapPageGenerator


trait MarkedPageGenerator extends BootstrapPageGenerator {
  override def headSuffix(context: Context, args: ContentsArgs) = {
    super.headSuffix(context, args) ++ Seq(
      jslib("marked","marked.js")
    )
  }

  override def bodyMain(context: Context, args: ContentsArgs) : Contents = {
    Seq(div(scalatags.Text.all.id:="marked", body_content(context, args)))
  }

  override def bodySuffix(context: Context, args: ContentsArgs) :Contents = {
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

abstract class MarkedPage(
  override val id: Symbol,
  override val title: String,
  override val description: String
) extends BootstrapPage(id, title, description) with MarkedPageGenerator


trait ForbiddenPageGenerator extends BasicPageGenerator {
  def what: String
  def why: String
  override val title = "Forbidden - " + what
  override val description = "Forbidden Error Page"

  def bodyMain(context: Context, args: ContentsArgs): Html.Contents = {
    danger(Seq(
      h1("Nuh Uh! I Can't Do That!"),
      p(em("Drat!"), s"Because $why, you can't $what. That's just the way it is."),
      p("You should try one of these options:"),
      ul(li("Type in another URL, or"), li("Try to get lucky with ",
        a(href := context.suggestURL.toString, "this suggestion")))
    ))()
  }
}

case class ForbiddenPage(
  override val id: Symbol,
  what: String,
  why: String
) extends Html.Template(id) with ForbiddenPageGenerator

trait NotFoundPageGenerator extends BasicPageGenerator {
  def what: String
  def causes: Seq[String]
  def suggestions: Seq[String]
  def title : String = "Not Found - " + what
  def description : String = "Not Found Error Page"
  def bodyMain(context: Context, args: ContentsArgs) : Contents = {
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

case class NotFoundPage(
  override val id: Symbol,
  what: String,
  causes: Seq[String] = Seq(),
  suggestions: Seq[String] =Seq()
) extends Html.Template(id) with NotFoundPageGenerator

trait PlainPageGenerator extends BasicPageGenerator {
  def content(context: Context, args: ContentsArgs) : Html.Contents
  def bodyMain(context: Context, args: ContentsArgs) : Contents = Seq(
    div(cls:="container", content(context, args), debug_footer(context))
  )
}

abstract class GenericPlainPage(_id: Symbol, title: String, description: String)
  extends BasicPage(_id, title, description) with PlainPageGenerator

case class PlainPage(
  override val id: Symbol,
  override val title: String,
  override val description: String,
  the_content: Html.Contents
) extends GenericPlainPage(id, title, description)
{
  def content(context: Context, args: ContentsArgs) : Html.Contents = the_content
}
