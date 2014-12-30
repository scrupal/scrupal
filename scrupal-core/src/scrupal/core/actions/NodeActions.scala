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

package scrupal.core.actions

import reactivemongo.bson.{BSONString, BSONDocument, BSONObjectID}
import scrupal.core.api._

import scala.concurrent.{ExecutionContext, Future}

/** Action From Node
  *
  * This is an adapter that captures the context and a node and turns it into an action that invokes the
  * Generator function on the node to produce the action's result. This just allows a node to be used as an action.
  * @param context Context in which the action should occur
  * @param node The node that will produce the action's result
  */
case class NodeAction(context: Context, node: Node) extends Action {
  def apply() : Future[Result[_]] = {
    node(context)
  }
}

case class NodeIdAction(id: BSONObjectID, context: Context) extends Action {
  def apply() : Future[Result[_]] = {
    context.withSchema { (dbc, schema) ⇒
      context.withExecutionContext { implicit ec: ExecutionContext ⇒
        schema.nodes.fetch(id).flatMap {
          case Some(node) ⇒
            node(context)
          case None ⇒
            Future.successful( ErrorResult(s"Node at id '${id.toString}' not found.", NotFound) )
        }
      }
    }
  }
}

case class NodeAliasAction(path: String, context: Context) extends Action {
  def apply() : Future[Result[_]] = {
    val selector = BSONDocument("$eq" → BSONDocument("pathAlias" → BSONString(path)))
    context.withSchema { (dbc, schema) ⇒
      context.withExecutionContext { implicit ec: ExecutionContext ⇒
        schema.nodes.findOne(selector).flatMap {
          case Some(node) ⇒
            node(context)
          case None ⇒
            Future.successful( ErrorResult(s"Node at path '$path' not found.", NotFound) )
        }
      }
    }
  }
}

