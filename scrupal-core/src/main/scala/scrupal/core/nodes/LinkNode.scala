/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
 *                                                                                                                    *
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
 * with the License. You may obtain a copy of the License at                                                          *
 *                                                                                                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                                                                     *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
 * the specific language governing permissions and limitations under the License.                                     *
 **********************************************************************************************************************/

package scrupal.core.nodes

import java.net.URL

import org.joda.time.DateTime
import reactivemongo.bson.{ Macros, BSONDocument, BSONHandler, BSONObjectID }
import scalatags.Text.all._
import scrupal.core.api._
import scrupal.db.VariantReaderWriter
import spray.http.{ MediaType, MediaTypes }

import scala.concurrent.Future

/** Link Node
  * This node type contains a URL to a resource and generates a link to it.
  */
case class LinkNode(
  description : String,
  url : URL,
  modified : Option[DateTime] = Some(DateTime.now),
  created : Option[DateTime] = Some(DateTime.now),
  _id : BSONObjectID = BSONObjectID.generate,
  final val kind : Symbol = LinkNode.kind) extends Node {
  override val mediaType : MediaType = MediaTypes.`text/html`
  def apply(ctxt : Context) : Future[Result[_]] = Future.successful {
    HtmlResult(a(href := url.toString, description), Successful)
  }
}

object LinkNode {
  import scrupal.core.api.BSONHandlers._
  final val kind = 'Link
  object LinkNodeVRW extends VariantReaderWriter[Node, LinkNode] {
    implicit val LinkNodeHandler : BSONHandler[BSONDocument, LinkNode] = Macros.handler[LinkNode]
    override def fromDoc(doc : BSONDocument) : LinkNode = LinkNodeHandler.read(doc)
    override def toDoc(obj : Node) : BSONDocument = LinkNodeHandler.write(obj.asInstanceOf[LinkNode])
  }
  Node.variants.register(kind, LinkNodeVRW)
}

