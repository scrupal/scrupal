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
import reactivemongo.bson.{ BSONDocument, BSONHandler, BSONObjectID, Macros }
import scalatags.Text.all._
import scrupal.core.api._
import scrupal.db.VariantReaderWriter
import spray.http.{ MediaType, MediaTypes }

import scala.concurrent.{ ExecutionContext, Future }

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
case class ScalaNode(
  description : String,
  code : String,
  modified : Option[DateTime] = Some(DateTime.now()),
  created : Option[DateTime] = Some(DateTime.now()),
  _id : BSONObjectID = BSONObjectID.generate,
  final val kind : Symbol = ScalaNode.kind) extends Node {
  override val mediaType : MediaType = MediaTypes.`text/html`

  def apply(ctxt : Context) : Future[Result[_]] = ctxt.withExecutionContext {
    implicit ec : ExecutionContext ⇒
      Future {
        import javax.script.ScriptEngineManager
        val sem = new ScriptEngineManager()
        val e = sem.getEngineByName("scala")
        HtmlResult(span(e.eval(code).toString), Successful)
      }
  }
}

object ScalaNode {
  import scrupal.core.api.BSONHandlers._
  final val kind : Symbol = 'Scala
  object ScalaNodeBRW extends VariantReaderWriter[Node, ScalaNode] {
    implicit val ScalaNodeHandler : BSONHandler[BSONDocument, ScalaNode] = Macros.handler[ScalaNode]
    override def fromDoc(doc : BSONDocument) : ScalaNode = ScalaNodeHandler.read(doc)
    override def toDoc(obj : Node) : BSONDocument = ScalaNodeHandler.write(obj.asInstanceOf[ScalaNode])
  }
  Node.variants.register(kind, ScalaNodeBRW)
}

