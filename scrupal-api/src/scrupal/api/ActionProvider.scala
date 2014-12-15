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

package scrupal.api

import reactivemongo.bson.{BSONObjectID, BSONString, BSONDocument}
import scrupal.utils._
import shapeless.HList
import spray.http.Uri
import spray.routing.PathMatcher.{Unmatched, Matched}
import spray.routing.PathMatchers._

import scala.concurrent.{ExecutionContext, Future}

/** Generic object that provides Actions
  *
  * This trait is mixed in to classes that provide actions in conjunction with a key, path and context triplet.
  * ActionProviders participate in the routing process by finding the action that corresponds to a particular path
  * and context. This generic action provider makes no assumptions about how the triplet is matches and defers that
  * to its subclass.
  */
trait ActionProvider extends Identifiable {

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
    * to a dash.
    *
    * @return The constant string used to identify this ActionProvider
    */

  def makeKey(name: String) = name.toLowerCase.replaceAll(Patterns.NotAllowedInUrl.pattern.pattern,"-")

  lazy val singularKey = makeKey ( id.name )

  lazy val pluralKey = makeKey( Pluralizer.pluralize(id.name) )

  /** A mapping of key to ActionProvider
    * This map type is used to select the ActionProvider that pertains to a particular key in the path.
    */
  type ActionProviderMap = Map[String,ActionProvider]

  /** The suborindate ActionProviders of this one.
    *
    * Action providers form a hierarchy that correspond to the initial segments of the path they match. This member
    * provides the next level in the hierarchy.
    *
    * @return
    */
  def subordinates: ActionProviderMap

  /** Indicates whether this ActionProvider is at the bottom of the hierarchy.
    *
    * Action providers form a hierarchy that correspond to the initial segments of the path they match. This method
    * indicates whether this ActionProvider is at the leaf of the hierarchy (i.e. it has no subordinates).
    * @return
    */
  def isTerminal : Boolean = subordinates.isEmpty

  /** Resolve an Action
    *
    * Given a path and a context, find the matching PathToAction and then invoke it to yield the corresponding Action.
    * A subclass must implement this method.
    *
    * @param key The key used to select this ActionProvider
    * @param path The path to use to match the PathToAction function
    * @param context The context to use to match the PathToAction function
    * @return
    */
  def actionFor(key: String, path: Uri.Path, context: Context) : Option[Action]

  /** Resolve An Action
    *
    * Same as the Uri.Path variant but takes a String path.
    *
    * @param key The key used to select this ActionProvider
    * @param path The path to use to match the PathToAction function
    * @param context The context to use to match the PathToAction function
    * @return
    */
  def actionFor(key: String, path: String, context: Context) : Option[Action] = {
    actionFor(key, Uri.Path(path), context)
  }
}

trait DelegatingActionProvider extends ActionProvider {
  def actionFor(key: String, path: Uri.Path, context: Context) : Option[Action] = {
    if (key == pluralKey || key == singularKey) {
      Segments(path) match {
        case Matched(pathRest, extractions) ⇒
          val segments = extractions.head
          if (segments.isEmpty)
            None
          else {
            val nextKey = segments.head
            subordinates.get(nextKey).flatMap { ap: ActionProvider ⇒
              val restOfPath = Uri.Path(segments.tail.mkString("/")) ++ pathRest
              ap.actionFor(nextKey, restOfPath, context)
            }
          }
        case Unmatched ⇒ None
      }
    } else {
      None
    }
  }
}

/** ActionProvider with no subordinates
  *
  * Classes mixing this trait in are leaf nodes in the hierarchy of action providers. They have no subordinates and
  * return true for the isTerminal method.
  */
trait TerminalActionProvider extends ActionProvider {
  final override val subordinates = Map.empty[String,ActionProvider]
  override val isTerminal = true
}

trait EnablementActionProvider[T <: EnablementActionProvider[T]]
  extends DelegatingActionProvider with Enablement[T] with Enablee
{
  def actionProviders = forEach[ActionProvider] { e: Enablee ⇒
    e.isInstanceOf[ActionProvider] && isEnabled(e, this)
  } { e: Enablee ⇒
    e.asInstanceOf[ActionProvider]
  }

  def subordinates : ActionProviderMap = {
    for (ap ← actionProviders ; name ← Seq(ap.singularKey, ap.pluralKey)) yield {
      name → ap
    }
  }.toMap
}

/** An ActionProvider that uses PathMatcherToAction instances for its matching actions
  *
  * PathMatcherToActionProviders convert an path into an action by searching a list of PathMatcherToAction instances.
  * PathMatchersToAction use a PathMatcher to implement a matches method that matches a Path against a PathMatcher. If
  * the match succeeds, the corresponding Action is returned. Because the PathMatcherToAction instances are searched
  * sequentially, this is not highly performant and the order of the PathMatcherToAction instances matters.
  */
trait PathMatcherToActionProvider extends ActionProvider {

  /** The Acceptable Matches
    *
    * This method should return a set of PathMatcherToAction instances that translate the matched path to an Action.
    * Be sure to list the longest patterns first as the first one that matches any prefix will win. So if you want
    * to match `/path/to/42` and `/path` then put the longer one first or else /path will get recognized first.
    * @return A Seq of PathMatcherToAction
    */
  def pathsToActions: Seq[PathMatcherToAction[_ <: HList]] = Seq.empty[PathMatcherToAction[_ <: HList]]

  /** Resolve An Action
    *
    * Give a key, a path and a context, find the matching PathToAction and then invoke it to yield the corresponding
    * Action.
    *
    * @param key The key used to select this ActionProvider
    * @param path The path to use to match the PathToAction function
    * @param context The context to use to match the PathToAction function
    * @return
    */
  override def actionFor(key: String, path: Uri.Path, context: Context) : Option[Action] = {
    for (p2a ← pathsToActions ; action = p2a.matches(path, context) if action != None) { return action }
    None
  }
}

trait EnablementPathMatcherToActionProvider[T <: EnablementPathMatcherToActionProvider[T]]
  extends EnablementActionProvider[T] with PathMatcherToActionProvider {
  override def actionFor(key: String, path: Uri.Path, context: Context) : Option[Action] = {
    for (p2a ← pathsToActions ; action = p2a.matches(path, context) if action != None) { return action }
    super.actionFor(key, path, context)
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

object NodeProvider extends { val id: Symbol = 'Node } with TerminalActionProvider {
  def actionFor(key: String, path: Uri.Path, context: Context) : Option[Action] = {
    if (key == singularKey || key == pluralKey) {
      {
        PathMatchers.BSONObjectIdentifier(path) match {
          case Matched(pathRest, extractions) ⇒
            if (pathRest.isEmpty) {
              val id = extractions.head
              Some(NodeIdAction(id, context))
            } else {
              None
            }
          case Unmatched ⇒ None
        }
      } match {
        case Some(action) ⇒ Some(action)
        case None ⇒
          Segments(path) match {
            case Matched(pathRest, extractions) ⇒
              val path = extractions.head.mkString("/")
              Some(NodeAliasAction(path, context))
            case Unmatched ⇒
              None
          }
      }
    } else {
      None
    }
  }
}
