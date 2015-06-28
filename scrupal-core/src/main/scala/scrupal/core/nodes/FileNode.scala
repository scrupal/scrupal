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
import scrupal.api._
import akka.http.scaladsl.model.{ MediaTypes, MediaType }

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
  name : String,
  description : String,
  file : File,
  override val mediaType : MediaType = MediaTypes.`text/html`,
  modified : Option[DateTime] = Some(DateTime.now),
  created : Option[DateTime] = Some(DateTime.now),
  final val kind : Symbol = FileNode.kind) extends Node {
  def apply(context: Context) : Future[Response] = {
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
    Future.successful(StreamResponse(new FileInputStream(file), mediaType))
  }
}

object FileNode {
  final val kind = 'File
}
