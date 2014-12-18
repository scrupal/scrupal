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

package scrupal.api.html

import scrupal.api.{Feature, Context, PathOf}

import scalatags.Text.all._

abstract class BasicPage(the_title: String, the_description: String) extends Page(the_title, the_description) {
  def headSuffix(context: Context): Seq[Modifier] = {
    implicit val ctxt = context
    Seq(
      link(rel := "stylesheet", media := "screen", href := PathOf.theme(context.themeProvider, context.themeName)),
      link(rel := "stylesheet", href := PathOf.lib("font-awesome", "css/font-awesome.css"), media := "screen"),
      link(rel := "stylesheet", href := PathOf.css("scrupal"), media := "screen"),
      script(`type` := "text/javascript", src := PathOf.lib("jquery", "jquery.min.js")),
      script(`type` := "text/javascript", src := PathOf.lib("bootstrap", "js/bootstrap.min.js"))
    )
  }

  def bodyPrefix(context: Context): Seq[Modifier] = { display_alerts.contents(context) }

  def bodySuffix(context: Context): Seq[Modifier] = {
    if(Feature.enabled('DebugFooter, context.scrupal)){
      Seq(display_context_table.contents(context))
    } else {
      Seq.empty[Modifier]
    }
  }
}

case class MarkedPage(the_title: String, the_description: String, bodyContent: (Context) ⇒ Seq[Modifier])
  extends BasicPage(the_title, the_description)
{
  override def headSuffix(context: Context) = {
    super.headSuffix(context) ++ Seq(
      script(src:=PathOf.lib("marked","marked.js")(context))
    )
  }

  override def bodyMain(context: Context) = {
    Seq(div(id:="marked", bodyContent(context)))
  }

  override def bodySuffix(context: Context) = {
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

case class OPAPage(the_title: String, the_description: String, module: String)
  extends BasicPage(the_title, the_description)
{
  val data_main : Attr = "data-main".attr
  val media: Attr = "media".attr

  def bodyMain(context: Context) : Seq[Modifier] = Seq(
    div(`class`:="container",
      div(id := module)
    )
  )

  override def bodyTag(context: Context) : TagContent = {
    body(ng.controller := "scrupal", ng.show:= "page_is_ready",
      bodyPrefix(context),
      bodyMain(context),
      script(lang := "javascript", "var scrupal_module_to_load='" + module + "';"),
      script(data("main"):="require-data-main.js", `type`:="text/javascript",
        src:=PathOf.lib("requirejs","require.js")(context)),
      script(`type` := "text/javascript", src := PathOf.lib("angularjs", "angular.js")(context)),
      bodySuffix(context)
    )
  }
}

class ForbiddenPage(what: String, why: String)
  extends BasicPage("Forbidden - " + what, "Forbidden Error Page")
{
  def bodyMain(context: Context) : Modifiers = Seq(
    danger(Tags(Seq(
      h1("Nuh Uh! I Can't Do That!"),
      p(em("Drat!"), s"Because $why, you can't $what. That's just the way it is."),
      p("You should try one of these options:"),
      ul(li("Type in another URL, or"), li("Try to get lucky with ",
        a(href:=context.suggestURL.toString,"this suggestion")))
    ))).contents(context)
  )
}

class NotFoundPage(what: String, causes: Seq[String] = Seq(), suggestions: Seq[String] = Seq())
  extends BasicPage("Not Found - " + what, "Not Found Error Page")
{
  def bodyMain(context: Context) : Modifiers = Seq(
    warning(Tags(Seq(
      h1("There's A Hole In THe Fabrice Of The InterWebz!"),
      p(em("Oops!"), "We couldn't find ", what, ". That might be because:"),
      ul({ for (c ← causes) yield { li(c) } },
        li("you used an old bookmark for which the resource is no longer available"),
        li("you mis-typed the web address.")
      ),
      p("You can try one of these options:"),
      ul( { for (s ← suggestions) yield { li(s) } },
        li("type in another URL, or "),
        li("Try to get lucky with ", a(href:=context.suggestURL.toString,"this suggestion"))
      )
    ))).contents(context)
  )
}
