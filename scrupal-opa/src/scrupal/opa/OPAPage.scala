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

import scrupal.core.api.{Html, Context}
import scrupal.core.api.Html._
import scrupal.core.html.BasicPage

import scalatags.Text.all._

/** Top Level Page for One Page Applications
  *
  * This page provides the standard page for loading an AngularJS One Page Application.
  *
  * @param the_title
  * @param the_description
  * @param module
  */
case class OPAPage(the_title: String, the_description: String, module: String)
  extends BasicPage(the_title, the_description)
{
  val data_main : Attr = "data-main".attr
  val media: Attr = "media".attr

  def bodyMain(context: Context) : Contents = Seq(
    div(`class`:="container",
      div(scalatags.Text.all.id := module)
    )
  )

  override def bodyTag(context: Context) : Html.TagContent = {
    body(
      bodyPrefix(context),
      bodyMain(context),
      js("""var require = {
           |  paths: {
           |    'domReady'              : [ '/assets/lib/requirejs-domready/domReady'],
           |    'marked'                : [ '/assets/lib/marked/marked' ],
           |    'angular'               : [ '/assets/lib/angularjs/angular' ],
           |    'ngRoute'               : [ '/assets/lib/angularjs/angular-route'],
           |    'ngDragAndDrop'         : [ '/assets/lib/angular-dragdrop/draganddrop' ],
           |    'ngMultiSelect'         : [ '/assets/lib/angular-multi-select/angular-multi-select'],
           |    'ng.ui'                 : [ '/assets/lib/angular-ui/angular-ui'],
           |    'ng.ui.bootstrap'       : [ '/assets/lib/angular-ui-bootstrap/ui-bootstrap'],
           |    'ng.ui.bootstrap.tpls'  : [ '/assets/lib/angular-ui-bootstrap/ui-bootstrap-tpls'],
           |    'ng.ui.calendar'        : [ '/assets/lib/angular-ui-calendar/calendar'],
           |    'ng.ui.router'          : [ '/assets/lib/angular-ui-router/angular-ui-router'],
           |    'ng.ui.utils'           : [ '/assets/lib/angular-ui-utils/ui-utils']
           |},
           |  shim: {
           |    'marked'    : { exports: 'marked' },
           |    'angular'   : { exports: 'ng' },
           |    'ngRoute'   : { exports: 'ngRoute' }
           |  }
           |}
           |""".stripMargin),
      jslib("requirejs", "require.js"),
      jslib("angularjs", "angular.js"),
      jslib("scrupal", "scrupal-opa-fastOpt.js"),
      jslib("scrupal", "scrupal-opa-launcher.js"),
      bodySuffix(context)
    )
  }
}

