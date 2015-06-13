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
import scrupal.core.api.{Node, Context, Result, StringResult}
import scrupal.db.VariantReaderWriter
import spray.http.MediaTypes

import scala.concurrent.Future

case class StringNode(
  description: String,
  text: String,
  modified: Option[DateTime] = Some(DateTime.now),
  created: Option[DateTime] = Some(DateTime.now),
  _id: BSONObjectID = BSONObjectID.generate,
  final val kind : Symbol = StringNode.kind
) extends Node {
  final val mediaType = MediaTypes.`text/plain`
  def apply(ctxt: Context) : Future[Result[_]] = Future.successful { StringResult(text) }
}

object StringNode {
  import scrupal.core.api.BSONHandlers._
  final val kind = 'String
  object StringNodeVRW extends VariantReaderWriter[Node,StringNode] {
    implicit val StringNodeHandler : BSONHandler[BSONDocument,StringNode] = Macros.handler[StringNode]
    override def fromDoc(doc: BSONDocument): StringNode = StringNodeHandler.read(doc)
    override def toDoc(obj: Node): BSONDocument = StringNodeHandler.write(obj.asInstanceOf[StringNode])
  }
  Node.variants.register(kind, StringNodeVRW)
}

