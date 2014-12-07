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

package scrupal

import org.joda.time.DateTime
import play.twirl.api.Html
import scrupal.api._
import scrupal.core.{AdminApp, MarkedDocNode, CoreModule, EchoEntity}
import shapeless.{::, HList, HNil}
import spray.routing.PathMatcher
import spray.routing.PathMatchers._

class WelcomeSite extends Site {
  def id: Symbol = 'WelcomeToScrupal
  val name: String = "Welcome To Scrupal"
  val description: String = "The default 'Welcome To Scrupal' site that is built in to Scrupal"
  val modified: Option[DateTime] = Some(DateTime.now)
  val created: Option[DateTime] = Some(new DateTime(2014,11,18,17,40))
  override val themeName = "cyborg"
  def host: String = ".*"
  final val key = ""
  val siteRoot: Node =
    HtmlNode (
      "Main index page for Welcome To Scrupal Site",
      WelcomeSite.WelcomePageTemplate,
      args = Map.empty[String,Html],
      modified=Some(DateTime.now),
      created=Some(new DateTime(2014, 11, 18, 18, 0))
  )

  object DocPathToDocs extends PathToNodeActionFunction(PathMatcher("doc")/Segments, {
    (list: ::[List[String],HNil], rest, ctxt) ⇒ new MarkedDocNode("doc","docs", list.head)
  }) {}


  def pathsToActions : Seq[PathMatcherToAction[_ <: HList]] = Seq(
    DocPathToDocs,
    PathToNodeAction(RestPath, siteRoot)
  )

  CoreModule.enable(this)
  EchoEntity.enable(this)
  AdminApp.enable(this)
  CoreModule.enable(EchoEntity)
  CoreModule.enable(AdminApp)
}

object WelcomeSite {
  lazy val WelcomePageTemplate =
    TwirlHtmlTemplate('WelcomePage, "The Welcome Page", scrupal.views.html.WelcomePage)
}
