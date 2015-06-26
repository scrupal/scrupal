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

import akka.http.scaladsl.model.MediaTypes
import org.joda.time.DateTime
import scrupal.api._
import scala.concurrent.Future

case class StringNode(
  name : String,
  description : String,
  text : String,
  modified : Option[DateTime] = Some(DateTime.now),
  created : Option[DateTime] = Some(DateTime.now),
  final val kind : Symbol = StringNode.kind) extends Node {
  final val mediaType = MediaTypes.`text/plain`
  def apply(request : DetailedRequest) : Future[Response] = Future.successful { StringResponse(text) }
}

object StringNode {
  final val kind = 'String
}

