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
import scrupal.core.api.Html.ContentsArgs
import scrupal.core.api._
import scrupal.db.VariantReaderWriter
import spray.http.{ MediaType, MediaTypes }

import scala.concurrent.Future

case class StaticNode(
  description : String,
  body : Html.Template,
  modified : Option[DateTime] = Some(DateTime.now),
  created : Option[DateTime] = Some(DateTime.now),
  _id : BSONObjectID = BSONObjectID.generate,
  final val kind : Symbol = StaticNode.kind) extends Node {
  def args : ContentsArgs = Html.EmptyContentsArgs
  val mediaType : MediaType = MediaTypes.`text/html`
  def apply(ctxt : Context) : Future[Result[_]] = Future.successful {
    HtmlResult(body.render(ctxt, args), Successful)
  }
}

object StaticNode {
  import scrupal.core.api.BSONHandlers._
  final val kind = 'Static
  object StaticNodeVRW extends VariantReaderWriter[Node, StaticNode] {
    implicit val StaticNodeHandler : BSONHandler[BSONDocument, StaticNode] = Macros.handler[StaticNode]
    override def fromDoc(doc : BSONDocument) : StaticNode = StaticNodeHandler.read(doc)
    override def toDoc(obj : Node) : BSONDocument = StaticNodeHandler.write(obj.asInstanceOf[StaticNode])
  }
  Node.variants.register(kind, StaticNodeVRW)
}
