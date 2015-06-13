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

import org.joda.time.DateTime
import reactivemongo.bson.{ Macros, BSONDocument, BSONHandler, BSONObjectID }
import scrupal.core.api.{ Node, Context, Result, StringResult }
import scrupal.db.VariantReaderWriter
import spray.http.MediaTypes

import scala.concurrent.Future

case class StringNode(
  description : String,
  text : String,
  modified : Option[DateTime] = Some(DateTime.now),
  created : Option[DateTime] = Some(DateTime.now),
  _id : BSONObjectID = BSONObjectID.generate,
  final val kind : Symbol = StringNode.kind) extends Node {
  final val mediaType = MediaTypes.`text/plain`
  def apply(ctxt : Context) : Future[Result[_]] = Future.successful { StringResult(text) }
}

object StringNode {
  import scrupal.core.api.BSONHandlers._
  final val kind = 'String
  object StringNodeVRW extends VariantReaderWriter[Node, StringNode] {
    implicit val StringNodeHandler : BSONHandler[BSONDocument, StringNode] = Macros.handler[StringNode]
    override def fromDoc(doc : BSONDocument) : StringNode = StringNodeHandler.read(doc)
    override def toDoc(obj : Node) : BSONDocument = StringNodeHandler.write(obj.asInstanceOf[StringNode])
  }
  Node.variants.register(kind, StringNodeVRW)
}

