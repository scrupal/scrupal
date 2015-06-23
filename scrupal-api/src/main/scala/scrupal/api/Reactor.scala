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

import scala.concurrent.Future

/** A function that generates content
  *
  * This is the basic characteristic of a Node. It is simply a function that receives a Context
  * and produces content as a Future Result. The Context provides the setting in which it is
  * generating the content. All dynamic content in Scrupal is generated through a Generator.
  * The Result embodies the notion of completing a request with some content and a disposition.
  */
trait Reaction extends ((Request) ⇒ Future[Response])

/** An Reaction To A Request That Produces A Response
  *
  * Reactions bring extensible behavior to Scrupal. A reaction object should be considered as the processing necessary
  * to convert a request into a response. A reaction:
  *
  * - is a function that returns a generic result (Result[_]) in the Future
  *
  * - contains the request that initiates it so the result can be run multiple times and invoked without arguments
  *
  * - can be extended to include other information or behavior peculiar to a given type of action
  *
  * A request indicates what should be done to which processing entity and in what context.
  *
  * @see [[scrupal.api.Request]]
  *
  */
trait Reactor extends ( () ⇒ Future[Response] ) with Reaction {

  /** The Request to which this Reaction reacts to
    *
    * Reactions capture their request so that a contains everything it needs to generate a response and the reaction
    * can be run without providing any further arguments.
    * @return The request
    */
  def request : Request

  /** The action part of an Action object.
    *
    * Objects mixing in this trait will define apply to implement the Action. Note that the result type is a generic
    * Result[_]. The only things you have to return are a Disposition, a ContentType and some sort of payload of
    * arbitrary type. Clients of the action should understand the actual type of result.
    *
    * @return The Result[_] yielded from executing the action.
    */
  def apply() : Future[Response] = this.apply(this.request)

  def apply(request: Request) : Future[Response]
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
