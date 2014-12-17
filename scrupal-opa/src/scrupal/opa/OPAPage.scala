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

package scrupal.opa

import scrupal.api.{Context, PathOf}

import scalatags.Text.all._
import scalatags.Text.tags2

class OPAPage(val head_title: String, val head_description: String, val head_suffix: String) {

  val ng_controller : Attr = "ng-controller".attr
  val ng_show : Attr = "ng-show".attr
  val data_main : Attr = "data-main".attr
  val media: Attr = "media".attr

  def render(args: Map[String,String])(implicit context: Context) = {
    val module = args.getOrElse("module", "scrupal")

    "<!DOCTYPE html>\n" + html(
      head(
        tags2.title(head_title),
        meta(charset := "UTF-8"),
        meta(name := "viewport", content := "width=device-width, initial-scale=1.0"),
        meta(name := "description", content := head_description),
        link(rel := "shortcut icon", `type` := "image/x-icon", href := PathOf.favicon()),
        link(rel := "stylesheet", media := "screen", href := PathOf.theme(context.themeProvider, context.themeName)),
        link(rel := "stylesheet", href := PathOf.lib("font-awesome", "css/font-awesome.css"), media := "screen"),
        link(rel := "stylesheet", href := PathOf.css("scrupal"), media := "screen"),
        script(`type` := "text/javascript", src := PathOf.lib("jquery", "jquery.min.js")),
        script(`type` := "text/javascript", src := PathOf.lib("bootstrap", "js/bootstrap.min.js")),
        head_suffix
      ),
      body(ng_controller := "scrupal", ng_show := "page_is_ready",
        div(`class`:="container",
          div(id := module)
        ),
        script(lang := "javascript", "var scrupal_module_to_load='" + module + "';"),
        script(data_main := "require-data-main.js", `type` := "text/javascript",
          src := PathOf.lib("requirejs", "require.js")),
        script(`type` := "text/javascript", src := PathOf.lib("angularjs", "angular.js"))
      )
    )
  }
}
