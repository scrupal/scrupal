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
import reactivemongo.bson.{BSONDocument, BSONHandler, BSONObjectID, Macros}
import scalatags.Text.all._
import scrupal.api._
import scrupal.db.VariantReaderWriter
import spray.http.{MediaType, MediaTypes}

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
case class CommandNode (
  description: String,
  command: String,
  modified: Option[DateTime] = Some(DateTime.now()),
  created: Option[DateTime] = Some(DateTime.now()),
  _id: BSONObjectID = BSONObjectID.generate,
  final val kind: Symbol = CommandNode.kind
) extends Node {
  override val mediaType: MediaType = MediaTypes.`text/html`

  def apply(ctxt: Context): Future[Result[_]] = Future.successful {
    // TODO: implement CommandNode
    HtmlResult(span("Not Implemented"), Unimplemented)
  }
}

object CommandNode {
  import scrupal.api.BSONHandlers._
  final val kind = 'Command
  object CommandNodeBRW extends VariantReaderWriter[Node,CommandNode] {
    implicit val CommandNodeHandler : BSONHandler[BSONDocument,CommandNode] = Macros.handler[CommandNode]
    override def fromDoc(doc: BSONDocument): CommandNode = CommandNodeHandler.read(doc)
    override def toDoc(obj: Node): BSONDocument = CommandNodeHandler.write(obj.asInstanceOf[CommandNode])
  }
  Node.variants.register(kind, CommandNodeBRW)
}

