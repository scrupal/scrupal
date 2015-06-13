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

import java.io.{FileInputStream, File}

import org.joda.time.DateTime
import reactivemongo.bson.{Macros, BSONDocument, BSONHandler, BSONObjectID}
import scrupal.core.api.{Node, StreamResult, Result, Context}
import scrupal.db.VariantReaderWriter
import spray.http.{MediaTypes, MediaType}

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
case class FileNode (
  description: String,
  file: File,
  override val mediaType: MediaType = MediaTypes.`text/html`,
  modified: Option[DateTime] = Some(DateTime.now),
  created: Option[DateTime] = Some(DateTime.now),
  _id: BSONObjectID = BSONObjectID.generate,
  final val kind: Symbol = FileNode.kind
  ) extends Node {
  def apply(ctxt: Context): Future[Result[_]] = {
    val extension = {
      val name = file.getName
      name.lastIndexOf(".") match {
        case i: Int if i >= 0 ⇒ file.getName.substring(i + 1)
        case _ ⇒ ""
      }
    }
    val mediaType = MediaTypes.forExtension(extension) match {
      case Some(mt) ⇒ mt
      case None ⇒ MediaTypes.`application/octet-stream`
    }
    Future.successful( StreamResult(new FileInputStream(file), mediaType) )
  }
}

object FileNode {
  import scrupal.core.api.BSONHandlers._
  final val kind = 'File
  object FileNodeVRW extends VariantReaderWriter[Node,FileNode] {
    implicit val FileNodeHandler : BSONHandler[BSONDocument,FileNode] = Macros.handler[FileNode]
    override def fromDoc(doc: BSONDocument): FileNode = FileNodeHandler.read(doc)
    override def toDoc(obj: Node): BSONDocument = FileNodeHandler.write(obj.asInstanceOf[FileNode])
  }
  Node.variants.register(kind, FileNodeVRW)
}
