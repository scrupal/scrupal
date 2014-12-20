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

import org.joda.time.DateTime
import reactivemongo.bson.{Macros, BSONDocument, BSONHandler, BSONObjectID}
import scalatags.Text.all._
import scrupal.api._
import scrupal.db.VariantReaderWriter
import spray.http.{MediaType, MediaTypes}

import scala.concurrent.Future

/** Message Node
  *
  * This is a very simple node that simply renders a standard Boostrap message. It is used for generating error messages
  * during node substitution so the result is visible to the end user.
  */
case class MessageNode(
  description: String,
  css_class: String,
  message: String,
  modified: Option[DateTime] = Some(DateTime.now),
  created: Option[DateTime] = Some(DateTime.now),
  _id: BSONObjectID = BSONObjectID.generate,
  final val kind : Symbol = MessageNode.kind
) extends Node {
  final val mediaType: MediaType = MediaTypes.`text/html`
  def apply(ctxt: Context): Future[Result[_]] = Future.successful {
    val text = div(cls:=css_class, message)
    HtmlResult(text, Successful)
  }
}

object MessageNode {
  import scrupal.api.BSONHandlers._
  final val kind = 'Message
  object MessageNodeVRW extends VariantReaderWriter[Node,MessageNode] {
    implicit val MessageNodeHandler : BSONHandler[BSONDocument,MessageNode] = Macros.handler[MessageNode]
    override def fromDoc(doc: BSONDocument): MessageNode = MessageNodeHandler.read(doc)
    override def toDoc(obj: Node): BSONDocument = MessageNodeHandler.write(obj.asInstanceOf[MessageNode])
  }
  Node.variants.register(kind, MessageNodeVRW)
}

