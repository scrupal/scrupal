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

import java.net.URL

import org.joda.time.DateTime
import reactivemongo.bson.{Macros, BSONDocument, BSONHandler, BSONObjectID}
import scalatags.Text.all._
import scrupal.core.api._
import scrupal.db.VariantReaderWriter
import spray.http.{MediaType, MediaTypes}

import scala.concurrent.Future

/** Link Node
  * This node type contains a URL to a resource and generates a link to it.
  */
case class LinkNode (
  description: String,
  url: URL,
  modified: Option[DateTime] = Some(DateTime.now),
  created: Option[DateTime] = Some(DateTime.now),
  _id: BSONObjectID = BSONObjectID.generate,
  final val kind: Symbol = LinkNode.kind
) extends Node {
  override val mediaType: MediaType = MediaTypes.`text/html`
  def apply(ctxt: Context): Future[Result[_]] = Future.successful {
    HtmlResult(a(href:=url.toString,description), Successful)
  }
}

object LinkNode {
  import scrupal.core.api.BSONHandlers._
  final val kind = 'Link
  object LinkNodeVRW extends VariantReaderWriter[Node,LinkNode] {
    implicit val LinkNodeHandler : BSONHandler[BSONDocument,LinkNode] = Macros.handler[LinkNode]
    override def fromDoc(doc: BSONDocument): LinkNode = LinkNodeHandler.read(doc)
    override def toDoc(obj: Node): BSONDocument = LinkNodeHandler.write(obj.asInstanceOf[LinkNode])
  }
  Node.variants.register(kind, LinkNodeVRW)
}

