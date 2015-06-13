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

import akka.http.scaladsl.model.Uri.Path.Slash
import akka.http.scaladsl.model.{ HttpMethod, HttpMethods, Uri }
import akka.http.scaladsl.server.{PathMatchers, PathMatcher}
import akka.http.scaladsl.server.PathMatcher._
import akka.http.scaladsl.server.util.Tuple
import scrupal.core.actions.NodeAction
import scrupal.utils.{ ScrupalComponent, Pluralizer, Patterns }
import shapeless.{ ::, HNil, HList }

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
trait Action extends (() ⇒ Future[Result[_]]) {
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
    * @see [[scrupal.api.Context]]
    * @return
    */
  def context : Context

  /** Convenience function to dispatch the action
    *
    * Dispatching of actions is done by the Scrupal object, but since that is contained within this action's Context, we
    * can provide a shortcut for dispatching this action directly.
    * @return
    */
  def dispatch : Future[Result[_]] = {
    context.scrupal.dispatch(this)
  }
}

/** Extract An Action From A Context
  *
  * Objects of this type optionally extract an Action from a Context. Since the Context contains the RequestContext
  * and possibly the site and user, such objects participate in request decoding to determine out how to process the
  * request. If the object cannot extract an action it should return None so other alternatives can be attempted.
  */
trait ActionExtractor {
  def extractAction(context : Context) : Option[Action]
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
  def delegateAction(context : Context) : Option[Action] = {
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

  def extractAction(context : Context) : Option[Action] = {
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
  def actionFor(list : L, context : Context) : Option[Action]
}

/** Extract An Action From Context For PathMatcher And Method
  *
  * This trait encapsulates the extraction of an Action from a [[scrupal.api.Context]] by using a
  * [[akka.http.scaladsl.server.PathMatcher]] and an [[akka.http.scaladsl.model.HttpMethod]]. The context is used to obtain the method and the
  * unmatchedPath which is then matched against the `pm` and `method` members of this. If a match is made then the
  * `actionFor` method is invoked to convert the extracted values, a [[shapeless.HList]], the remaining path and
  * the context into an action.
  * particular shape of [[shapeless.HList]] and an [[scrupal.api.Action]]. If the [[akka.http.scaladsl.server.PathMatcher]]
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
  override def extractAction(context : Context) : Option[Action] = {
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

  //implicit val ev : akka.http.scaladsl.server.util.Tuple[shapeless.::[String,shapeless.HNil]]

  /** PathMatcher that matches the singular form of the keyword and extracts it. */
  def pm : PathMatcher[::[String, HNil]] = {
    PathMatcher(Uri.Path(singularKey), singularKey :: HNil) ~ PathMatchers.Slash |
      PathMatcher(Uri.Path(singularKey), singularKey :: HNil)
  }

  /** Final Implementation of [[scrupal.api.PathAndMethodActionExtractor.actionFor()]]
    * This implementation just delegates to a more precise implementation that provides the keyword that was
    * extracted from the path.
    * @param list The shapeless HList extracted from the PathMatcher
    * @param context The new context
    * @return The Action resulting from the delegated call to [[actionFor()]]
    */
  final def actionFor(list : ::[String, HNil], context : Context) : Option[Action] = {
    actionFor(list.head, context)
  }

  /** Extract the Action
    * This method is intended to be defined by subclasses to extract the Action corresponding to the extraction of
    * the keyword from this Extractor and the context.
    * @param keyword The keyword used that matched this extractor
    * @param context The context of the request
    * @return The Action to process that corresponds to the context
    */
  def actionFor(keyword : String, context : Context) : Option[Action]
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

