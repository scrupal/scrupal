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
import scrupal.api.Html.ContentsArgs
import scrupal.api._

import scala.concurrent.Future

case class StaticNode(
  name : String,
  description : String,
  body : Html.Template,
  modified : Option[Instant] = Some(Instant.now),
  created : Option[Instant] = Some(Instant.now)
) extends Node {
  def args : ContentsArgs = Html.EmptyContentsArgs
  val mediaType : MediaType = MediaTypes.`text/html`
  def apply(context : Context) : Future[Response] = Future.successful {
    HtmlResponse(body.render(context, args), Successful)
  }
}
