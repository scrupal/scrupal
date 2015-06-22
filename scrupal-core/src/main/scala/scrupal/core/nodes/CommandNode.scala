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
import scrupal.api._
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
  id : Identifier,
  description : String,
  command : String,
  modified : Option[DateTime] = Some(DateTime.now()),
  created : Option[DateTime] = Some(DateTime.now()),
  final val kind : Symbol = CommandNode.kind) extends Node {
  override val mediaType : MediaType = MediaTypes.`text/html`

  def apply(ctxt : Request) : Future[Response] = Future.successful {
    // TODO: implement CommandNode
    HtmlResponse(span("Not Implemented").toString(), Unimplemented)
  }
}

object CommandNode {
  final val kind = 'Command
}

