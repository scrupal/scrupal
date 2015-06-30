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

import scala.concurrent.Future

/** Message Node
  *
  * This is a very simple node that simply renders a standard Boostrap message. It is used for generating error messages
  * during node substitution so the result is visible to the end user.
  */
case class MessageNode(
  name : String,
  description : String,
  css_class : String,
  message : String,
  modified : Option[Instant] = Some(Instant.now),
  created : Option[Instant] = Some(Instant.now),
  final val kind : Symbol = MessageNode.kind) extends Node {
  final val mediaType : MediaType = MediaTypes.`text/html`
  def apply(context: Context) : Future[Response] = Future.successful {
    val text = div(cls := css_class, message)
    HtmlResponse(Html.renderContents(Seq(text)), Successful)
  }
}

object MessageNode {
  final val kind = 'Message
}

