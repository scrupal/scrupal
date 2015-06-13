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
import reactivemongo.bson.{ BSONObjectID, Macros, BSONDocument, BSONHandler }
import scrupal.core.api.{ Node, Context, Result, StreamResult }
import scrupal.db.VariantReaderWriter
import spray.http.{ MediaType, MediaTypes }

import scala.concurrent.Future

case class URLNode(
  description : String,
  url : URL,
  mediaType : MediaType = MediaTypes.`text/html`,
  modified : Option[DateTime] = Some(DateTime.now),
  created : Option[DateTime] = Some(DateTime.now),
  _id : BSONObjectID = BSONObjectID.generate,
  final val kind : Symbol = URLNode.kind) extends Node {
  def apply(ctxt : Context) : Future[Result[_]] = Future.successful {
    StreamResult(url.openStream(), mediaType)
  }
}

object URLNode {
  import scrupal.core.api.BSONHandlers._
  final val kind = 'URL
  object URLNodeVRW extends VariantReaderWriter[Node, URLNode] {
    implicit val URLNodeHandler : BSONHandler[BSONDocument, URLNode] = Macros.handler[URLNode]
    override def fromDoc(doc : BSONDocument) : URLNode = URLNodeHandler.read(doc)
    override def toDoc(obj : Node) : BSONDocument = URLNodeHandler.write(obj.asInstanceOf[URLNode])
  }
  Node.variants.register(kind, URLNodeVRW)
}

