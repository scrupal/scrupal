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

package scrupal.opa.views

import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID
import scrupal.api._
import scrupal.opa.OPAPage
import shapeless.HList
import spray.http.{ContentTypes, MediaTypes}
import spray.routing.PathMatchers.RestPath

import scala.concurrent.{ExecutionContext, Future}

case class OnePageApp(
  override val id : Identifier,
  name: String,
  description: String,
  modified : Option[DateTime] = None,
  created : Option[DateTime] = None
) extends Application(id) {
  final val kind = 'OnePageApp

  val opaPage = new OPAPage(name, description, "" )

  case class OPANode(
    description: String,
    modified: Option[DateTime] = Some(DateTime.now),
    created: Option[DateTime] = Some(DateTime.now),
    _id: BSONObjectID = BSONObjectID.generate,
    final val kind : Symbol = 'OPANode
  ) extends Node {
    final val mediaType = MediaTypes.`text/html`
    def apply(context: Context) : Future[Result[_]] = {
      context.withExecutionContext { implicit ec: ExecutionContext ⇒
        Future {
          val page = opaPage.render(Map("module" → "scrupal"))(context)
          OctetsResult(page.getBytes(utf8), MediaTypes.`text/html`, Successful)
        }
      }
    }
  }

  val theOnePage: Node = OPANode(description, Some(DateTime.now()), Some(DateTime.now()))

  override def pathsToActions : Seq[PathMatcherToAction[_ <: HList]] = Seq(
    PathToNodeAction(RestPath, theOnePage)
  )

}
