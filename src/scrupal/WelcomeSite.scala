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
import scrupal.core.{MarkedDocNode, MarkedDocument, CoreModule, EchoEntity}
import shapeless.{::, HList, HNil}
import spray.http.Uri
import spray.http.Uri.Path
import spray.routing.PathMatcher
import spray.routing.PathMatchers._

import scala.concurrent.Future

class WelcomeSite extends Site {
  def id: Symbol = 'WelcomeToScrupal
  val name: String = "Welcome To Scrupal"
  val description: String = "The default 'Welcome To Scrupal' site that is built in to Scrupal"
  val modified: Option[DateTime] = Some(DateTime.now)
  val created: Option[DateTime] = Some(new DateTime(2014,11,18,17,40))
  override val themeName = "amelia"
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

  object DocPathToDocs extends PathToAction(PathMatcher("doc") / Segments) {
    def apply(list: ::[List[String],HNil], rest: Path, context: Context): Action = {
      NodeAction(context, new MarkedDocNode(context.scrupal.assetsLocator, "doc", "docs", list.head))
    }
  }

  case class RootAction(context: Context) extends Action {
    def apply() : Future[Result[_]] = { siteRoot(context) }
  }

  object AnyPathToRoot extends PathToAction(RestPath) {
    def apply(matched: ::[Uri.Path,HNil], rest: Uri.Path, context: Context) : Action = RootAction(context)
  }

  def pathsToActions : Seq[PathToAction[_ <: HList]] = Seq(
    DocPathToDocs,
    AnyPathToRoot
  )

  CoreModule.enable(this)
  EchoEntity.enable(this)
  CoreModule.enable(EchoEntity)

  val roots = Seq("docs/api", "docs/core", "docs/db", "docs/http", "docs/utils", "docs/intro")

  val apiDoc = new MarkedDocument('api, "docs/api", roots) ;       apiDoc.enable(this)   ; CoreModule.enable(apiDoc)
  val coreDoc = new MarkedDocument('core, "docs/core", roots);     coreDoc.enable(this)  ; CoreModule.enable(coreDoc)
  val dbDoc = new MarkedDocument('db, "docs/db", roots);           dbDoc.enable(this)    ; CoreModule.enable(dbDoc)
  val httpDoc = new MarkedDocument('http, "docs/http", roots);     httpDoc.enable(this)  ; CoreModule.enable(httpDoc)
  val utilsDoc = new MarkedDocument('utils, "docs/utils", roots);  utilsDoc.enable(this) ; CoreModule.enable(utilsDoc)
  val introDoc = new MarkedDocument('intro, "docs/intro", roots);  introDoc.enable(this) ; CoreModule.enable(introDoc)
}

object WelcomeSite {
  lazy val WelcomePageTemplate =
    TwirlHtmlTemplate('WelcomePage, "The Welcome Page", scrupal.views.html.WelcomePage)
}
