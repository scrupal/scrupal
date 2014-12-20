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

package scrupal.api.nodes

import java.net.URL

import org.joda.time.DateTime
import reactivemongo.bson.{BSONObjectID, Macros, BSONDocument, BSONHandler}
import scrupal.api.{Node, Context, Result, StreamResult}
import scrupal.db.VariantReaderWriter
import spray.http.{MediaType, MediaTypes}

import scala.concurrent.Future

case class URLNode(
  description: String,
  url: URL,
  mediaType: MediaType = MediaTypes.`text/html`,
  modified: Option[DateTime] = Some(DateTime.now),
  created: Option[DateTime] = Some(DateTime.now),
  _id: BSONObjectID = BSONObjectID.generate,
  final val kind : Symbol = URLNode.kind
) extends Node {
  def apply(ctxt: Context) : Future[Result[_]] = Future.successful {
    StreamResult(url.openStream(), mediaType)
  }
}

object URLNode {
  import scrupal.api.BSONHandlers._
  final val kind = 'URL
  object URLNodeVRW extends VariantReaderWriter[Node,URLNode] {
    implicit val URLNodeHandler : BSONHandler[BSONDocument,URLNode] = Macros.handler[URLNode]
    override def fromDoc(doc: BSONDocument): URLNode = URLNodeHandler.read(doc)
    override def toDoc(obj: Node): BSONDocument = URLNodeHandler.write(obj.asInstanceOf[URLNode])
  }
  Node.variants.register(kind, URLNodeVRW)
}

