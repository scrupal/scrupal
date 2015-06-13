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
import scalatags.Text.all._
import scrupal.api.Node
import akka.http.scaladsl.model.{ MediaType, MediaTypes }

import scala.concurrent.Future

/** Generate content With an operating system command
  *
  * This will invoke a local operating system command to generate content. As this forks the VM it should be
  * restricted to administrative users. However, for those cases where python, bash, perl or a simple grep/awk/sed
  * pipeline is the best thing, this is the tool to use. It simply passes the string to the local operating system's
  * command processor for interpretation and execution. Whatever it generates is Streamed as a result to this node.
  *
  * @param description
  * @param command
  * @param modified
  * @param created
  */
case class CommandNode(
  description : String,
  command : String,
  modified : Option[DateTime] = Some(DateTime.now()),
  created : Option[DateTime] = Some(DateTime.now()),
  _id : BSONObjectID = BSONObjectID.generate,
  final val kind : Symbol = CommandNode.kind) extends Node {
  override val mediaType : MediaType = MediaTypes.`text/html`

  def apply(ctxt : Context) : Future[Result[_]] = Future.successful {
    // TODO: implement CommandNode
    HtmlResult(span("Not Implemented"), Unimplemented)
  }
}

object CommandNode {
  import scrupal.core.api.BSONHandlers._
  final val kind = 'Command
  object CommandNodeBRW extends VariantReaderWriter[Node, CommandNode] {
    implicit val CommandNodeHandler : BSONHandler[BSONDocument, CommandNode] = Macros.handler[CommandNode]
    override def fromDoc(doc : BSONDocument) : CommandNode = CommandNodeHandler.read(doc)
    override def toDoc(obj : Node) : BSONDocument = CommandNodeHandler.write(obj.asInstanceOf[CommandNode])
  }
  Node.variants.register(kind, CommandNodeBRW)
}

