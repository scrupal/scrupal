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

import play.api.mvc.{AnyContent, Request, ResponseHeader, Result}
import scrupal.storage.api.{Collection, Queries, Query, StoreContext}

import scala.concurrent.{ExecutionContext, Future}

/** A function that produces a future response from a stimulus.
  *
  * This is the most fundamental kind of action in Scrupal. A Reaction is simply a function that maps
  * a [[scrupal.api.Stimulus]] into a [[scala.concurrent.Future]] of a [[scrupal.api.Response]].
  * All dynamic content in Scrupal is produced eventually through the use of a Reaction. The Response embodies the
  * notion of completing a request with some content and a disposition on the processing.
  * @see [[scrupal.api.Reactor]]
  */
trait Reaction extends ((Stimulus) ⇒ Future[Response])

/** An Named, Described Reaction
  *
  * Reactors bring extensible behavior to Scrupal. A Reactor should be considered as the processing necessary
  * to convert a stimulus into a response. Some Reactor types are storable in the database and they are all
  * self-describing. A Reactor:
  *
  * - is a function that converts a Stimulus to a Response in the Future, since it inherits Reaction
  *
  * - can be extended to include other information or behavior peculiar to a given type of action
  *
  * @see [[scrupal.api.Stimulus]]
  * @see [[scrupal.api.Response]]
  *
  */
trait Reactor extends Reaction with Nameable with Describable { self ⇒

  def apply(request: Stimulus) : Future[Response]

  def resultFrom[CT](context: Context, request : Request[AnyContent]) : Future[Result] = {
    context.withExecutionContext { implicit ec: ExecutionContext ⇒
      val stimulus: Stimulus = Stimulus(context, request)
      apply(stimulus) map { response : Response ⇒
        val d = response.disposition
        val status = d.toStatusCode.intValue()
        val reason = Some(s"HTTP($status): ${d.id.name}(${d.code}): ${d.msg}")
        val header = ResponseHeader(status, reasonPhrase = reason)
        Result(header, response.toEnumerator)
      }
    }
  }
}

case class UnimplementedReactor(what: String) extends Reactor {
  val name = "NotImplementedReactor"
  val description = "A Reactor that returns a not-implemented response"

  def apply(stimulus: Stimulus): Future[Response] = {
    Future.successful {UnimplementedResponse(what)}
  }
}

/** Reactor From A Node
  *
  * This is an adapter that captures a request and a node and turns it into a reactor that invokes the
  * Reaction function on the node to produce the reactor's result. This just allows a node to be used as an action.
  * @param node The node that will produce the action's result
  */
case class NodeReactor(node : Node) extends Reactor {
  val name = "NodeReactor"
  val description = "A Reactor that returns the content of a provided Node."
  def apply(stimulus : Stimulus) : Future[Response] = {
    node(stimulus.context)
  }
}

/** Reactor From A Stored Node
  *
  * This provides a Reactor from a stored node. It loads the node from the database and invokes the node's
  * Reaction function to generate a Response or, if the node is not found, it generates an error response.
  * @param id The primary id of the node
  */
case class NodeIdReactor(id : Long) extends Reactor {
  val name = "NodeIdReactor"
  val description = "A Reactor that returns the content of a node having a specific ID"
  def apply(stimulus: Stimulus) : Future[Response] = {
    val context = stimulus.context
    context.withSchema("core") { (storeContext, schema) ⇒
      context.withExecutionContext { implicit ec : ExecutionContext ⇒
        schema.withCollection("nodes") { nodes : Collection[Node] ⇒
          nodes.fetch(id).flatMap {
            case Some(node) ⇒
              node(context)
            case None ⇒
              Future.successful(ErrorResponse(s"Node at id '${id.toString}' not found.", NotFound))
          }
        }
      }
    }
  }
}

/** A Query to Find Nodes by their alias path
  *     val selector = BSONDocument("$eq" → BSONDocument("pathAlias" → BSONString(path)))
  */
trait NodeQueries extends Queries[Node] {
  def byAlias(alias: String) : Query[Node]
}

case class NodeAliasReactor(alias : String) extends Reactor {
  val name = "NodeAlias"
  val description = "A Reactor that returns the node found at a path alias"
  def apply(stimulus: Stimulus): Future[Response] = {
    val context = stimulus.context
    context.withStoreContext { sc: StoreContext ⇒
      context.withExecutionContext { implicit ec: ExecutionContext ⇒
        sc.withCollection("core", "nodes") { coll: Collection[Node] ⇒
          val queries : NodeQueries = coll.queriesFor[NodeQueries]
          coll.findOne(queries.byAlias(alias)).flatMap {
            case Some(node) ⇒
              node(context)
            case None ⇒
              Future.successful(ErrorResponse(s"Node alias '$name' for path '$alias' could not found.", NotFound))
          }
        }
      }
    }
  }
}



/* TODO: Remove extractor stuff if not needed
/** Extract An Action From A Context
  *
  * Objects of this type optionally extract an Action from a Context. Since the Context contains the RequestContext
  * and possibly the site and user, such objects participate in request decoding to determine out how to process the
  * request. If the object cannot extract an action it should return None so other alternatives can be attempted.
  */
trait ActionExtractor {
  def extractAction(context : Context) : Option[Reaction]
  def describe : String = ""
}

trait DelegatingActionExtractor extends ScrupalComponent {
  /** The ActionExtractors this one delegates to */
  def delegates : Seq[ActionExtractor]

  /** A method to do the delegation
    *
    * @param context The context of the request
    * @return Some(Action) if one is found, None if none are found, and throws an error if multiple are found.
    */
  def delegateAction(context : Context) : Option[Reaction] = {
    val candidates = {
      for (ap ← delegates; action = ap.extractAction(context) if action.isDefined) yield { action.get }
    }
    if (candidates.isEmpty)
      None
    else if (candidates.size == 1)
      Some(candidates.head)
    else
      toss(s"Ambiguous Actions ($candidates) for context ($context).")
  }
}

trait PathMatcherActionExtractor[L <: HList] extends ActionExtractor {
  /** The PathMatcher to match against the context */
  def pm : PathMatcher[L]

  override def describe : String = pm.toString()

  def matches(path : Uri.Path) : Boolean = {
    pm(path) match {
      case Matched(rest, value) ⇒ true
      case Unmatched ⇒ false
      case _ ⇒ false
    }
  }

  def extractAction(context : Context) : Option[Reaction] = {
    pm(context.request.unmatchedPath) match {
      case Matched(rest, value) ⇒
        val newRequestContext = context.request.mapUnmatchedPath { path ⇒ rest }
        val newContext = context.withNewRequest(newRequestContext)
        actionFor(value, newContext)
      case Unmatched ⇒ None
    }
  }

  /** Produce The Matching Action
    *
    * Subclasses must implement this method to produce Some(Action) if there is a corresponding one, otherwise
    * None.
    * @param list The extracted values from the PathMatcher
    * @param context The context of the request
    * @return Some(Action) if the correponding context matches, or None if it doesn't
    */
  def actionFor(list : L, context : Context) : Option[Reaction]
}

/** Extract An Action From Context For PathMatcher And Method
  *
  * This trait encapsulates the extraction of an Action from a [[scrupal.api.Context]] by using a
  * [[akka.http.scaladsl.server.PathMatcher]] and an [[akka.http.scaladsl.model.HttpMethod]]. The context is used to obtain the method and the
  * unmatchedPath which is then matched against the `pm` and `method` members of this. If a match is made then the
  * `actionFor` method is invoked to convert the extracted values, a [[shapeless.HList]], the remaining path and
  * the context into an action.
  * particular shape of [[shapeless.HList]] and an [[scrupal.api.Reaction]]. If the [[akka.http.scaladsl.server.PathMatcher]]
  * successfully matches a [[akka.http.scaladsl.model.Uri.Path]] then this trait's `apply` method is invoked, with a
  * [[scrupal.api.Context]], to obtain the Action.
  *
  * @tparam L The HList that the PathMatcher produces
  */
trait PathAndMethodActionExtractor[L <: HList] extends PathMatcherActionExtractor[L] {

  /** The method to match against the context */
  def method : HttpMethod

  /** Return Matching Action
    * Match the context against the `pm` and the `method` and invoke `actionFor` to yield the corresponding Action.
    *
    * @param context The context of the request
    * @return Some(Action) if the corresponding context matches, or None if it doesn't
    */
  override def extractAction(context : Context) : Option[Reaction] = {
    if (context.request.request.method == method) {
      super.extractAction(context)
    } else {
      None
    }
  }
}

trait SingularActionExtractor extends PathMatcherActionExtractor[::[String, HNil]] {

  /** The path segment to match */
  def segment : String

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
  def makeKey(name : String) = name.toLowerCase.replaceAll(Patterns.NotAllowedInUrl.pattern.pattern, "-")

  /** Singular form of the keyword */
  lazy val singularKey = makeKey(segment)

  implicit val ev : akka.http.scaladsl.server.util.Tuple[shapeless.::[String,shapeless.HNil]]

  /** PathMatcher that matches the singular form of the keyword and extracts it. */
  def pm : PathMatcher[::[String, HNil]] = {
    PathMatcher(Uri.Path(singularKey), singularKey :: HNil) ~ Slash |
      PathMatcher(Uri.Path(singularKey), singularKey :: HNil)
  }

  /** Final Implementation of [[scrupal.api.PathAndMethodActionExtractor.actionFor()]]
    * This implementation just delegates to a more precise implementation that provides the keyword that was
    * extracted from the path.
    * @param list The shapeless HList extracted from the PathMatcher
    * @param context The new context
    * @return The Action resulting from the delegated call to [[actionFor()]]
    */
  final def actionFor(list : ::[String, HNil], context : Context) : Option[Reaction] = {
    actionFor(list.head, context)
  }

  /** Extract the Action
    * This method is intended to be defined by subclasses to extract the Action corresponding to the extraction of
    * the keyword from this Extractor and the context.
    * @param keyword The keyword used that matched this extractor
    * @param context The context of the request
    * @return The Action to process that corresponds to the context
    */
  def actionFor(keyword : String, context : Context) : Option[Reaction]
}

trait PluralActionExtractor extends SingularActionExtractor {
  /** Plural form of the keyword */
  lazy val pluralKey = makeKey(Pluralizer.pluralize(segment))

  /** PathMatcher that matches either the singular or plural form of the keyword and extracts it. */
  override def pm : PathMatcher[::[String, HNil]] = {
    super.pm | PathMatcher(Uri.Path(pluralKey), pluralKey :: HNil)
  }
}

abstract class ActionProducer[L <: HList](val pm : PathMatcher[L]) extends PathMatcherActionExtractor[L]

*/
