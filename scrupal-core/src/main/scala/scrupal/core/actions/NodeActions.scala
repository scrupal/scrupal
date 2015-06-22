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

package scrupal.core.actions

import scrupal.api._
import scrupal.storage.api.Collection

import scala.concurrent.{ ExecutionContext, Future }

/** Action From Node
  *
  * This is an adapter that captures the context and a node and turns it into an action that invokes the
  * Generator function on the node to produce the action's result. This just allows a node to be used as an action.
  * @param request Request for which the action should occur
  * @param node The node that will produce the action's result
  */
case class NodeReactor(request : Request, node : Node) extends Reactor {
  def apply(request: Request) : Future[Response] = {
    node(request)
  }
}

case class NodeIdReactor(id : Long, request : Request) extends Reactor {
  def apply(request: Request) : Future[Response] = {
    request.context.withSchema("core") { (storeContext, schema) ⇒
      request.context.withExecutionContext { implicit ec : ExecutionContext ⇒
        schema.withCollection("nodes") { nodes : Collection[Node] ⇒
          nodes.fetch(id).flatMap {
            case Some(node) ⇒
              node(request)
            case None ⇒
              Future.successful(ErrorResponse(s"Node at id '${id.toString}' not found.", NotFound))
          }
        }
      }
    }
  }
}

/* TODO: Reinstate NodeAliasAction if needed
case class NodeAliasAction(path : String, context : Context) extends Action {
  def apply() : Future[Result[_]] = {
    val selector = BSONDocument("$eq" → BSONDocument("pathAlias" → BSONString(path)))
    context.withSchema { (dbc, schema) ⇒
      context.withExecutionContext { implicit ec : ExecutionContext ⇒
        schema.nodes.findOne(selector).flatMap {
          case Some(node) ⇒
            node(context)
          case None ⇒
            Future.successful(ErrorResult(s"Node at path '$path' not found.", NotFound))
        }
      }
    }
  }
}
  */

