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

package scrupal.api

import akka.http.scaladsl.server.{PathMatcher, PathMatchers}
import play.api.mvc._
import scrupal.utils._

import scala.concurrent.{ExecutionContext, Future}

/** Provider Of Reactors
  *
  * Scrupal objects that mix in this trait participate in the dispatching of [[scrupal.api.Stimulus]]s. Providers get a chance to
  * indicate their interest in particular kinds of [[scrupal.api.Stimulus]]s by returning a [[Reactor]] that will
  * yield a [[Response]]. Note that a provider is both a Play Controller and a Play Router. However, it must implement
  * the reactions method rather than the routes method as the Response is converted automatically to a Play Result.
  */
trait Provider { self ⇒

  def resultFrom[CT](context: Context, request : Request[AnyContent], reactor : Reactor) : Future[Result] = {
    context.withExecutionContext { implicit ec: ExecutionContext ⇒
      val stimulus: Stimulus = Stimulus(context, request)
      reactor(stimulus) map { response : Response ⇒
        val d = response.disposition
        val status = d.toStatusCode.intValue()
        val reason = Some(s"HTTP($status): ${d.id.name}(${d.code}): ${d.msg}")
        val header = ResponseHeader(status, reasonPhrase = reason)
        Result(header, response.toEnumerator)
      }
    }
  }

  type ReactionRoutes = PartialFunction[RequestHeader,Reactor]

  def provide : ReactionRoutes

  def reactorFor(request: RequestHeader) : Option[Reactor] = {
    provide.lift(request)
  }

  def withPrefix(prefix: String): Provider = {
    if (prefix == "/") {
      self
    } else {
      new Provider {
        def provide = {
          val p = if (prefix.endsWith("/")) prefix else prefix + "/"
          val prefixed: PartialFunction[RequestHeader, RequestHeader] = {
            case rh: RequestHeader if rh.path.startsWith(p) =>
              rh.copy(path = rh.path.drop(p.length))
          }
          Function.unlift(prefixed.lift.andThen(_.flatMap(self.provide.lift)))
        }
        override def withPrefix(prefix: String) : Provider = self.withPrefix(prefix)
      }
    }
  }
}

object Provider {
  val empty = new Provider {
    def provide: ReactionRoutes = PartialFunction.empty
  }
}

trait IdentifiableProvider extends Provider with Identifiable

/** Delegating Provider of Reactors
  *
  * This Reactor Provider just contains a set of delegates to which it delegates the job of providing the Reactors
  */
trait DelegatingProvider extends Provider {

  def delegates : Iterable[Provider]

  def isTerminal : Boolean = delegates.isEmpty

  override def provide : ReactionRoutes = {
    delegates.foldLeft(Provider.empty.provide) { case (accum,next) ⇒ accum.orElse(next.provide) }
  }
}

trait EnablementProvider[T <: EnablementProvider[T]] extends DelegatingProvider with Enablement[T] with Enablee {
  def delegates : Iterable[Provider] = forEach[Provider] { e : Enablee ⇒
    e.isInstanceOf[Provider] && isEnabled(e, this)
  } { e : Enablee ⇒
    e.asInstanceOf[Provider]
  }
}

trait SiteProvider[T <: SiteProvider[T]] extends EnablementProvider[T]

trait PluralityProvider extends IdentifiableProvider {

  /** The routes for the singular prefix case */
  def singularRoutes : ReactionRoutes

  /** The routes for the plural prefix case */
  def pluralRoutes : ReactionRoutes

  /** Singular form of the entity's label */
  final val singularPrefix = makeKey(label)

  /** Plural form of the entity's label */
  final val pluralPrefix = makeKey(Pluralizer.pluralize(label))

  lazy val provide : ReactionRoutes = {
    val prefixedSingular = withPrefix(singularRoutes, singularPrefix)
    val prefixedPlural = withPrefix(pluralRoutes, pluralPrefix)
    prefixedPlural.orElse(prefixedSingular)
  }

  val singularMatcher = PathMatchers.Slash ~ PathMatcher(singularPrefix)
  val pluralMatcher = PathMatchers.Slash ~ PathMatcher(pluralPrefix)

  val matcher = singularMatcher | pluralMatcher

  def isPlural(request: RequestHeader) : Boolean = {
    request.path.startsWith(pluralPrefix)
  }

  protected def withPrefix(routes: ReactionRoutes, prefix: String) : ReactionRoutes = {
    val p = if (prefix.startsWith("/")) prefix else "/" + prefix
    val prefixed: PartialFunction[RequestHeader, RequestHeader] = {
      case header: RequestHeader if header.path.startsWith(p) =>
        header.copy(path = header.path.drop(p.length))
    }
    Function.unlift(prefixed.lift.andThen(_.flatMap(routes.lift)))
  }


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
  protected def makeKey(name : String) = name.toLowerCase.replaceAll(Patterns.NotAllowedInUrl.pattern.pattern, "-")
}


/** A provide of NodeReactor
  *
  * This adapts a node to being a provide of a NodeReactor that just uses the node.
  */
case class FunctionalNodeReactorProvider(nodeF : PartialFunction[RequestHeader,Node]) extends Provider {
  def provide : ReactionRoutes = nodeF.andThen { node : Node ⇒ NodeReactor(node) }
}

case class NodeReactorProvider(node : Node) extends Provider {
  def provide : ReactionRoutes = { case request: RequestHeader ⇒ node }
}

/* TODO: Reinstate NodeProvider if needed
object NodeProvider extends { val id : Symbol = 'Node; val segment = id.name } with TerminalActionProvider {
  override def provideAction(matchingSegment : String, context : Context) : Option[Action] = {
    if (matchingSegment == singularKey) {
      val path = context.request.unmatchedPath

      {
        ScrupalPathMatchers.BSONObjectIdentifier(path) match {
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
*/

/* TODO: Reinstate ActionExtractorToActionProvider if needed
  * An ActionProvider that uses PathMatcherToAction instances for its matching actions
  *
  * PathMatcherToActionProviders convert an path into an action by searching a list of PathMatcherToAction instances.
  * PathMatchersToAction use a PathMatcher to implement a matches method that matches a Path against a PathMatcher. If
  * the match succeeds, the corresponding Action is returned. Because the PathMatcherToAction instances are searched
  * sequentially, this is not highly performant and the order of the PathMatcherToAction instances matters.
  *
  * trait ActionExtractorToActionProvider extends ActionProvider {
  *
  * /** The Acceptable Matches
  *
  * This method should return a set of PathMatcherToAction instances that translate the matched path to an Action.
  * Be sure to list the longest patterns first as the first one that matches any prefix will win. So if you want
  * to match `/path/to/42` and `/path` then put the longer one first or else /path will get recognized first.
  * @return A Seq of PathMatcherToAction
  * */
  * def extractors: Seq[ActionExtractor] = Seq.empty[ActionExtractor]
  *
  * /** Resolve An Action
  *
  * Give a key, a path and a context, find the matching PathToAction and then invoke it to yield the corresponding
  * Action.
  *
  * @param key The key used to select this ActionProvider
  * @param path The path to use to match the PathToAction function
  * @param context The context to use to match the PathToAction function
  * @return
  * */
  * override def actionFor(key: String, path: Uri.Path, context: Context) : Option[Action] = {
  * for (p2a ← extractors ; action = p2a.matches(path, context) if action != None) { return action }
  * None
  * }
  * }
  *
  * trait EnablementActionExtractorToActionProvider[T <: EnablementActionExtractorToActionProvider[T]]
  * extends EnablementActionProvider[T] with ActionExtractorToActionProvider {
  * override def actionFor(key: String, path: Uri.Path, context: Context) : Option[Action] = {
  * for (p2a ← extractors ; action = p2a.matches(path, context) if action != None) { return action }
  * super.actionFor(key, path, context)
  * }
  * }
  */

