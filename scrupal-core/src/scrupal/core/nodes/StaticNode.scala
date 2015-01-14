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

package scrupal.core.nodes

import org.joda.time.DateTime
import reactivemongo.bson.{Macros, BSONDocument, BSONHandler, BSONObjectID}
import scrupal.core.api.Html.ContentsArgs
import scrupal.core.api._
import scrupal.db.VariantReaderWriter
import spray.http.{MediaType, MediaTypes}

import scala.concurrent.Future

case class StaticNode(
  description: String,
  body: Html.Template,
  modified: Option[DateTime] = Some(DateTime.now),
  created: Option[DateTime] = Some(DateTime.now),
  _id: BSONObjectID = BSONObjectID.generate,
  final val kind: Symbol = StaticNode.kind
) extends Node {
  def args : ContentsArgs = Html.EmptyContentsArgs
  val mediaType: MediaType = MediaTypes.`text/html`
  def apply(ctxt: Context): Future[Result[_]] = Future.successful {
    HtmlResult(body.render(ctxt, args), Successful)
  }
}

object StaticNode {
  import scrupal.core.api.BSONHandlers._
  final val kind = 'Static
  object StaticNodeVRW extends VariantReaderWriter[Node,StaticNode] {
    implicit val StaticNodeHandler : BSONHandler[BSONDocument,StaticNode] = Macros.handler[StaticNode]
    override def fromDoc(doc: BSONDocument): StaticNode = StaticNodeHandler.read(doc)
    override def toDoc(obj: Node): BSONDocument = StaticNodeHandler.write(obj.asInstanceOf[StaticNode])
  }
  Node.variants.register(kind, StaticNodeVRW)
}
