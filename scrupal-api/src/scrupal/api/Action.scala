/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/
package scrupal.api

import shapeless.HList
import spray.http.Uri
import spray.routing.{PathMatcher}
import spray.routing.PathMatcher.{Matched, Unmatched}

import scala.concurrent.Future

/** An Invokable Action
  *
  * Actions bring extensible behavior to Scrupal. An action object should be considered as a request to perform some
  * action in some context. To that end, you will note that an Action:
  *
  * - is a function that takes no arguments and returns a generic result (Result[_])
  *
  * - has a context that provides the invocation circumstances of the action
  *
  * - can be extended to include other information or behavior peculiar to a given type of action
  *
  * Action objects are how Scrupal represents an action taken from an external application. Actions are processed by
  * the ActionProcessor actor. Extensions of Action represent actual requests by adding parametric data to the Action
  * and implementing the `apply` function.
  */
trait Action extends (() => Future[Result[_]]) {
  /** The action part of an Action object.
    *
    * Objects mixing in this trait will define apply to implement the Action. Note that the result type is a generic
    * Result[_]. The only things you have to return are a Disposition, a ContentType and some sort of payload of
    * arbitrary type. Clients of the action should understand the actual type of result.
    * @return The Result[_] yielded from executing the action.
    */
  def apply() : Future[Result[_]]

  /** The context in which the Action is invoked.
    *
    * Every action executes within some context. The abstract Context class has many variants but a consistent interface
    * to allow actions to comprehend and manipulate the context in which they are executing.
    * @see [[Context]]
    * @return
    */
  def context : Context

  /** Convenience function to dispatch the action
    *
    * Dispatching of actions is done by the scrupal object, but since that is contained within this action's Context, we
    * can provide a shortcut for dispatching this action directly.
    * @return
    */
  def dispatch : Future[Result[_]] = {
    context.scrupal.dispatch(this)
  }
}

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

/** Mapping Of PathMatcher To Action
  *
  * This Function1 subclass is used to declare that a particular shape of HList can be converted into an Action and
  * provides the means to do so
  * @tparam L
  */
trait PathMatcherToAction[L <: HList] extends ( (L, Uri.Path, Context) ⇒ Action ) {
  def pm: PathMatcher[L]
  /** A convenience function for converting a path and context into an action using this PathToAction's
    * apply function. This just bypasses the need to apply the Uri.Path to the PathMatcher and process its
    * Matched or Unmatched result. Instead, it returns None for an Unmatched result, or the Action provided by
    * this PathToAction
    * @param path The path to match
    * @param c The context in which to match the path
    * @return None if the path did not match the PathMatcher, Some(Action) if it did
    */
  def matches(path: Uri.Path, c: Context) : Option[Action] = {
    pm(path) match {
      case Matched(rest, value) ⇒ Some(this.apply(value, rest, c))
      case Unmatched ⇒ None
    }
  }
  def apply(list: L, rest: Uri.Path, c: Context) : Action
}

abstract class PathToAction[L <:HList](val pm: PathMatcher[L]) extends PathMatcherToAction[L]

case class PathToNodeAction[L <:HList](pm: PathMatcher[L], node: Node) extends PathMatcherToAction[L] {
  def apply(list: L, rest: Uri.Path, c: Context) : Action = {
    NodeAction(c,node)
  }
}

case class PathToNodeActionFunction[L <:HList](pm: PathMatcher[L], nodeF: (L, Uri.Path, Context) ⇒ Node )
  extends PathMatcherToAction[L] {
  def apply(list: L, rest: Uri.Path, c: Context) : Action = {
    NodeAction(c,nodeF(list,rest,c))
  }
}
