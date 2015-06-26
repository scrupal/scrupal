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

import akka.http.scaladsl.model.{MediaTypes, MediaType}
import org.joda.time.DateTime
import scalatags.Text.all._
import scrupal.api._

import scala.concurrent.Future

/** Generate Content With Scrupalesy
  *
  * This gets to be fun. Scrupaleasy is a Scala DSL designed to be a very simple language to help generate some content
  * Unlike the SubstitutionNode, this is not a template but an actual program to generate content. Scrupaleasy should be
  * dead simple: designed for non-programmers. For compatibility with SubstitutionNode it can access any of the same
  * values
  * with @{...} syntax but these can then be processed as UTF-8 characters, e.g. such as filtering. Setting up a Unix
  * like piepeline should be its top construction, essentially { block1 } | { block2 } would compute output from
  * block1 which is given as input to block2 which then yields the result. All Scrupaleasy scripts are either consumers,
  * producers or filters. Consumers eat input but produce no output. Instead they can "throw" an error. Consumers are
  * used to stop content from happening. They usually come at the end of a pipe to validate content. Producers ignore
  * their input but produce output. They are usually the first thing in a pipeline. Filters read their input and
  * convert it to some output. They are usually in the middle of a pipeline. You can compose a processing pipeline like
  * this by invoking a Scrupaleasy thing from another. The DSL should provide:
  *
  * - Basic if/else and select-from-options logic flow
  *
  * - Functions to manipulate streams of characters (e.g. length, regex match, substring, beginsWith, etc.)
  *
  * - The infamous munge function for text editing of Mac lore
  *
  * - Insertions with a SubstitutionNode element: `@{foo(...)}`
  *
  * - Require statements: like Scala require and throws an exception to stop pipeline processing
  *
  * - A lot of fun and joy for Scrupal authors :)
  *
  * @param description
  * @param scrupalesy
  * @param modified
  * @param created
  */
case class ScrupalesyNode(
  name: String,
  description : String,
  scrupalesy : String,
  modified : Option[DateTime] = Some(DateTime.now()),
  created : Option[DateTime] = Some(DateTime.now()),
  final val kind : Symbol = ScrupalesyNode.kind) extends Node {
  override val mediaType : MediaType = MediaTypes.`text/html`

  def apply(request : DetailedRequest) : Future[Response] = Future.successful {
    // TODO: Implement ScrupaleasyNode
    HtmlResponse(Html.renderContents( Seq( span("Not Implemented"))), Unimplemented)
  }
}

object ScrupalesyNode {
  final val kind = 'Scrupalesy
}

