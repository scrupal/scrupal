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

import scrupal.api.{Provider, Request, Reactor, Node}
import scrupal.core.actions.NodeReactor

/** A provide of NodeReactor
  *
  * This adapts a node to being a provide of a NodeReactor that just uses the node.
  */
case class NodeReactorProvider(node : Node) extends Provider {
  def canProvide(request : Request) : Boolean = true
  def provide (request : Request) : Option[Reactor] = {
    Some(NodeReactor(request, node))
  }
}

case class FunctionalNodeReactorProvider(nodeF : (Request) ⇒ Node) extends Provider {
  def canProvide(request : Request) : Boolean = true
  def provide(request : Request) : Option[Reactor] = {
    Some(NodeReactor(request, nodeF(request)))
  }
}
