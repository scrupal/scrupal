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

package scrupal.core.html

import scrupal.api.Html.Contents

import scalatags.Text.all._
import scrupal.api._
import scrupal.api.Html._

trait BasicPageGenerator extends PageGenerator {
  def headSuffix(context : Context, args : ContentsArgs) : Html.Contents = {
    implicit val ctxt : Context = context
    Seq(
      link(rel := "stylesheet", media := "screen", href := PathOf.theme(context.themeProvider, context.themeName)),
      link(rel := "stylesheet", href := PathOf.lib("font-awesome", "css/font-awesome.css"), media := "screen"),
      link(rel := "stylesheet", href := PathOf.css("scrupal"), media := "screen")
    )
  }

  def bodyPrefix(context : Context, args : ContentsArgs) : Html.Contents = { display_alerts(context) }

  def bodySuffix(context : Context, args : ContentsArgs) : Html.Contents = {
    if (context.scrupal.Features.enabled('DebugFooter, context.scrupal)) {
      display_context_table(context)
    } else {
      Html.emptyContents
    }
  }
}

abstract class BasicPage(
  override val id : Symbol,
  override val title : String,
  override val description : String) extends TemplatePage(id, title, description) with BasicPageGenerator

trait BootstrapPageGenerator extends BasicPageGenerator {
  override def headSuffix(context : Context, args : ContentsArgs) : Html.Contents = {
    super.headSuffix(context) ++ Seq(
      jslib("jquery", "jquery.js"),
      jslib("bootstrap", "js/bootstrap.js")
    )
  }

  def body_content(context : Context, args : ContentsArgs) : Contents = {
    Seq(span(em("OOPS!"), " You forgot to override body_content!"))
  }

  override def bodyMain(context : Context, args : ContentsArgs) : Contents = {
    Seq(div(cls := "container", body_content(context, args)))
  }
}

abstract class BootstrapPage(
  override val id : Symbol,
  override val title : String,
  override val description : String) extends BasicPage(id, title, description) with BootstrapPageGenerator

trait MarkedPageGenerator extends BootstrapPageGenerator {
  override def headSuffix(context : Context, args : ContentsArgs) = {
    super.headSuffix(context, args) ++ Seq(
      jslib("marked", "marked.js")
    )
  }

  override def bodyMain(context : Context, args : ContentsArgs) : Contents = {
    Seq(div(scalatags.Text.all.id := "marked", body_content(context, args)))
  }

  override def bodySuffix(context : Context, args : ContentsArgs) : Contents = {
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
  override val id : Symbol,
  override val title : String,
  override val description : String) extends BootstrapPage(id, title, description) with MarkedPageGenerator

trait ForbiddenPageGenerator extends BasicPageGenerator {
  def what : String
  def why : String
  override val title = "Forbidden - " + what
  override val description = "Forbidden Error Page"

  def bodyMain(context : Context, args : ContentsArgs) : Html.Contents = {
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
  override val id : Symbol,
  what : String,
  why : String) extends Html.Template(id) with ForbiddenPageGenerator

trait NotFoundPageGenerator extends BasicPageGenerator {
  def what : String
  def causes : Seq[String]
  def suggestions : Seq[String]
  def title : String = "Not Found - " + what
  def description : String = "Not Found Error Page"
  def bodyMain(context : Context, args : ContentsArgs) : Contents = {
    warning(Seq(
      h1("There's A Hole In THe Fabrice Of The InterWebz!"),
      p(em("Oops!"), "We couldn't find ", what, ". That might be because:"),
      ul({ for (c ← causes) yield { li(c) } },
        li("you used an old bookmark for which the resource is no longer available"),
        li("you mis-typed the web address.")
      ),
      p("You can try one of these options:"),
      ul({ for (s ← suggestions) yield { li(s) } },
        li("type in another URL, or "),
        li("Try to get lucky with ", a(href := context.suggestURL.toString, "this suggestion"))
      )
    ))()
  }
}

case class NotFoundPage(
  override val id : Symbol,
  what : String,
  causes : Seq[String] = Seq(),
  suggestions : Seq[String] = Seq()) extends Html.Template(id) with NotFoundPageGenerator

trait PlainPageGenerator extends BasicPageGenerator {
  def content(context : Context, args : ContentsArgs) : Html.Contents
  def bodyMain(context : Context, args : ContentsArgs) : Contents = Seq(
    div(cls := "container", content(context, args), debug_footer(context))
  )
}

abstract class GenericPlainPage(_id : Symbol, title : String, description : String)
  extends BasicPage(_id, title, description) with PlainPageGenerator

case class PlainPage(
  override val id : Symbol,
  override val title : String,
  override val description : String,
  the_content : Html.Contents) extends GenericPlainPage(id, title, description) {
  def content(context : Context, args : ContentsArgs) : Html.Contents = the_content
}
