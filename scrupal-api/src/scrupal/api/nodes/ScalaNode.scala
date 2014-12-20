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

import scala.concurrent.{ExecutionContext, Future}

/** Generate content with Scala code
  *
  * For the professional programmer maintaining a site, this is essentially an Enumeratee that reads a stream of
  * bytes (potentially empty) and produces a stream as output. The output is the generated content. The full power of
  * Scala is at your fingertips with REPL like simplicity. The code is dynamically compiled and executed to produce
  * the filter. Like Scrupaleasy, these can be producers, consuemrs or filters and are intented to execute stand-alone or
  * in a pipeline.
  * @param description
  * @param code
  * @param modified
  * @param created
  */
case class ScalaNode (
  description: String,
  code: String,
  modified: Option[DateTime] = Some(DateTime.now()),
  created: Option[DateTime] = Some(DateTime.now()),
  _id: BSONObjectID = BSONObjectID.generate,
  final val kind: Symbol = ScalaNode.kind
) extends Node {
  override val mediaType: MediaType = MediaTypes.`text/html`

  def apply(ctxt: Context): Future[Result[_]] = ctxt.withExecutionContext {
    implicit ec: ExecutionContext ⇒
      Future {
        import javax.script.ScriptEngineManager
        val sem = new ScriptEngineManager()
        val e = sem.getEngineByName("scala")
        HtmlResult(span(e.eval(code).toString), Successful)
      }
  }
}

object ScalaNode {
  import scrupal.api.BSONHandlers._
  final val kind : Symbol = 'Scala
  object ScalaNodeBRW extends VariantReaderWriter[Node,ScalaNode] {
    implicit val ScalaNodeHandler : BSONHandler[BSONDocument,ScalaNode] = Macros.handler[ScalaNode]
    override def fromDoc(doc: BSONDocument): ScalaNode = ScalaNodeHandler.read(doc)
    override def toDoc(obj: Node): BSONDocument = ScalaNodeHandler.write(obj.asInstanceOf[ScalaNode])
  }
  Node.variants.register(kind, ScalaNodeBRW)
}

