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

import java.io.{ FileInputStream, File }

import org.joda.time.DateTime
import reactivemongo.bson.{ Macros, BSONDocument, BSONHandler, BSONObjectID }
import scrupal.core.api.{ Node, StreamResult, Result, Context }
import scrupal.db.VariantReaderWriter
import spray.http.{ MediaTypes, MediaType }

import scala.concurrent.Future

/** File Node
  * This node type generates the content of an asset from a file typically bundled with the module. This is not
  * intended for use with delivering files uploaded to the server. Those should be handled by generating a link
  * and allowing CDN to deliver the content. You want to use a LinkNode for that.
  * @param description
  * @param file
  * @param modified
  * @param created
  */
case class FileNode(
  description : String,
  file : File,
  override val mediaType : MediaType = MediaTypes.`text/html`,
  modified : Option[DateTime] = Some(DateTime.now),
  created : Option[DateTime] = Some(DateTime.now),
  _id : BSONObjectID = BSONObjectID.generate,
  final val kind : Symbol = FileNode.kind) extends Node {
  def apply(ctxt : Context) : Future[Result[_]] = {
    val extension = {
      val name = file.getName
      name.lastIndexOf(".") match {
        case i : Int if i >= 0 ⇒ file.getName.substring(i + 1)
        case _ ⇒ ""
      }
    }
    val mediaType = MediaTypes.forExtension(extension) match {
      case Some(mt) ⇒ mt
      case None     ⇒ MediaTypes.`application/octet-stream`
    }
    Future.successful(StreamResult(new FileInputStream(file), mediaType))
  }
}

object FileNode {
  import scrupal.core.api.BSONHandlers._
  final val kind = 'File
  object FileNodeVRW extends VariantReaderWriter[Node, FileNode] {
    implicit val FileNodeHandler : BSONHandler[BSONDocument, FileNode] = Macros.handler[FileNode]
    override def fromDoc(doc : BSONDocument) : FileNode = FileNodeHandler.read(doc)
    override def toDoc(obj : Node) : BSONDocument = FileNodeHandler.write(obj.asInstanceOf[FileNode])
  }
  Node.variants.register(kind, FileNodeVRW)
}
