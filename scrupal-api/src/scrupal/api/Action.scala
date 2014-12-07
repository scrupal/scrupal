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

import scrupal.utils.{Enablement, Enablee, Pluralizer, Patterns}
import shapeless.HList
import spray.http.Uri
import spray.routing.PathMatcher
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

/** Something That Provides Actions.
  *
  * ActionProviders convert an path into an action by using a PathMatcher. PathMatchers are a general matching and
  * extraction facility based on shapeless.HList and provided by spray.routing. This can be used generally, without
  * Spray, or nicely in combination with the support in scrupal-http which is based on spray.
  */
abstract class ActionProvider extends Enablee {

  /** Key For Identifying This Provider
    *
    * When matching a path, it is helpful to quickly identify which ActionProvider to apply to a given path. To that
    * end, the key provides a constant path segment value that identifies this ActionProvider. For example, if
    * your path was /foo/bar/doit then foo and bar are potential keys as they might separately identify
    * an ActionProvider "foo" that contains an ActionProvider "bar". The "doit" suffix is not a candidate for an
    * ActionProvider's key because it is not / terminated. Keys are path segments and must occur only between slashes.
    *
    * Strings returned by key will be URL sanitized. They should therefore match the regular expression for URL
    * path characters ( [-A-Za-z0-9_~]+ ).  Any characters not matching the regular expression will be converted
    * to a dash
    *
    * @return The constant string used to identify this ActionProvider
    */

  def makeKey(name: String) = name.toLowerCase.replaceAll(Patterns.NotAllowedInUrl.pattern.pattern,"-")

  lazy val singularKey = makeKey ( id.name )

  lazy val pluralKey = makeKey( Pluralizer.pluralize(id.name) )

  /** List The Acceptable Matches
    *
    * This method should return a set of PathToAction instances that translate the matched path to an Action. Be sure
    * to list the longest patterns first as the first one that matches any prefix will win. So if you want to match
    * `/path/to/42` and `/path` then put the longer one first or else /path will get recognized first.
    * @return
    */
  def pathsToActions: Seq[PathMatcherToAction[_ <: HList]]

  /** Resolve An Action
    *
    * Give a path and a context, find the matching PathToAction and then invoke it to yield the corresponding Action.
    *
    * @param path The path to use to match the PathToAction function
    * @param context The context to use to match the PathToAction function
    * @return
    */
  def matchingAction(key: String, path: Uri.Path, context: Context) : Option[Action] = {
    for (p2a ← pathsToActions ; action = p2a.matches(path, context) if action != None) { return action }
    None
  }

  /** Resolve An Action
    *
    * Same as the Uri.Path variant but takes a String path.
    * @param path The path to use to match the PathToAction function
    * @param context The context to use to match the PathToAction function
    * @return
    */
  def matchingAction(key: String, path: String, context: Context) : Option[Action] = {
    matchingAction(key, Uri.Path(path), context)
  }

  type ActionProviderMap = Map[String,ActionProvider]
  def subordinateActionProviders: ActionProviderMap

  def isTerminal : Boolean = subordinateActionProviders.isEmpty
}

trait TerminalActionProvider extends ActionProvider {
  final override val subordinateActionProviders = Map.empty[String,ActionProvider]
  override val isTerminal = true
}

trait EnablementActionProvider[T <: EnablementActionProvider[T]]
  extends ActionProvider with Enablement[T] with Enablee
{
  def actionProviders = forEach[ActionProvider] { e: Enablee ⇒
    e.isInstanceOf[ActionProvider] && isEnabled(e, this)
  } { e: Enablee ⇒
    e.asInstanceOf[ActionProvider]
  }

  def subordinateActionProviders : ActionProviderMap = {
    for (ap ← actionProviders ; name ← Seq(ap.singularKey, ap.pluralKey)) yield {
      name → ap
    }
  }.toMap
}
