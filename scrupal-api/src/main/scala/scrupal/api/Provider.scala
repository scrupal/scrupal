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

import play.api.mvc._
import scrupal.utils._

/** Provider Of Reactors
  *
  * Scrupal objects that mix in this trait participate in the routing of [[scrupal.api.Stimulus]]s to a
  * [[scrupal.api.Reactor]]. Providers work very much like Play's Router and can even use the SIRD DSL for matching
  * RequestHeader. The only difference is that a Provider is a partial function returning a Reactor instead of a
  * Play Action. The Reactor's Response is converted into a Play Result by the core module. To implemen ta
  * Provider, just implement the provide method as a PartialFunction[RequestHeader,Reaction]. For Example:
  * {{{
  *   def provide : ReactionRoutes = {
  *     case GET(p"/foo") => NodeIdReactor(23)
  *   }
  * }}}
  */
trait Provider { self ⇒

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

trait IdentifiableProvider extends Provider with Identifiable {
  override def toString : String = s"Provider(${id.name})"
}

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

trait SingularProvider extends IdentifiableProvider {
  /** The routes for the singular prefix case */
  def singularRoutes : ReactionRoutes

  /** Singular form of the entity's label */
  final val singularPrefix = makeKey(label)

  lazy val provide: ReactionRoutes = {
    withPrefix(singularRoutes, singularPrefix)
  }

  def isSingular(request: RequestHeader): Boolean = {
    request.path.startsWith(singularPrefix)
  }

  protected def withPrefix(routes: ReactionRoutes, prefix: String): ReactionRoutes = {
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
  protected def makeKey(name: String) = name.toLowerCase.replaceAll(Patterns.NotAllowedInUrl.pattern.pattern, "-")

}

trait PluralProvider extends SingularProvider {

  /** The routes for the plural prefix case */
  def pluralRoutes : ReactionRoutes

  /** Plural form of the entity's label */
  final val pluralPrefix = makeKey(Pluralizer.pluralize(label))

  override lazy val provide: ReactionRoutes = {
    val prefixedSingular = withPrefix(singularRoutes, singularPrefix)
    val prefixedPlural = withPrefix(pluralRoutes, pluralPrefix)
    prefixedPlural.orElse(prefixedSingular)
  }

  def isPlural(request: RequestHeader) : Boolean = {
    request.path.startsWith(pluralPrefix)
  }
}


/** A provide of NodeReactor
  *
  * This adapts a node to being a provide of a NodeReactor that just uses the node.
  */
class NodeProvider(nodeF: PartialFunction[RequestHeader, Node]) extends Provider {
  def provide : ReactionRoutes = nodeF.andThen { node : Node ⇒ NodeReactor(node) }
}

class SingleNodeProvider(node: Node) extends Provider {
  def provide : ReactionRoutes = { case request: RequestHeader ⇒ NodeReactor(node) }
}
