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

package scrupal.core.impl

import akka.http.scaladsl.model.{ HttpMethod, HttpMethods }
import akka.http.scaladsl.server.PathMatcher
import scrupal.api.{ Reaction, PathAndMethodActionExtractor, Context, Node }
import scrupal.core.actions.NodeReaction
import shapeless.HList

/** Title Of Thing.
  *
  * Description of thing
  */
case class NodeActionProducer[L <: HList](pm : PathMatcher[L], node : Node, method : HttpMethod = HttpMethods.GET)
  extends PathAndMethodActionExtractor[L] {
  def actionFor(list : L, c : Context) : Option[Reaction] = {
    Some(NodeReaction(c, node))
  }
}

case class FunctionalNodeActionProducer[L <: HList](
  pm : PathMatcher[L],
  nodeF : (L, Context) ⇒ Node,
  method : HttpMethod = HttpMethods.GET) extends PathAndMethodActionExtractor[L] {
  def actionFor(list : L, c : Context) : Option[Reaction] = {
    Some(NodeReaction(c, nodeF(list, c)))
  }
}
