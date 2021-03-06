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

import java.time.Instant

import akka.http.scaladsl.model.{MediaTypes, MediaType}

import scalatags.Text.all._
import scrupal.api._
import scrupal.utils.ScrupalUtilsInfo

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
  name : String,
  description : String,
  code : String,
  modified : Option[Instant] = Some(Instant.now()),
  created : Option[Instant] = Some(Instant.now())
) extends Node {
  override val mediaType : MediaType = MediaTypes.`text/html`

  def apply(context: Context) : Future[Response] = {
    context.withExecutionContext { implicit ec : ExecutionContext ⇒
        Future {
          HtmlResponse(Html.renderContents( Seq( span(ScalaNode.engine.eval(code).toString) )), Successful)
        }
    }
  }
}

object ScalaNode {
  import javax.script._
  val engine = {
    val manager = new ScriptEngineManager(getClass.getClassLoader)
    val e = manager.getEngineByName("scala")
    val settings = e.asInstanceOf[scala.tools.nsc.interpreter.IMain].settings
    settings.embeddedDefaults[ScalaNode]
    val bindings = e.createBindings()
    bindings.put("scrupalVersion", ScrupalUtilsInfo.version)
    e.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
    e
  }
}
