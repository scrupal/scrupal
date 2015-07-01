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

package scrupal.core.akkahttp

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.Uri.Path.Slash
import akka.http.scaladsl.server.{PathMatcher, Route, PathMatchers, Directive}
import shapeless.{HList, HNil}

/** Created by reidspencer on 11/10/14.
  */
trait PathHelpers {
  type SegmentsResult[T] = shapeless.::[String, shapeless.::[T, HNil]]
/*
  def rawPathPrefixWithMatch[T](segments : Map[String, T]) : Directive[SegmentsResult[T]] = {
    val matcher = {
      if (segments.isEmpty)
        PathMatchers.nothingMatcher
      else {
        val pairs = segments.map {
          case (prefix, value) ⇒
            val provided : SegmentsResult[T] = HList((prefix, value))
            prefix → provided
        }
        val matchers = pairs.toSeq.sortWith { case (l, r) ⇒ l._1.length > r._1.length } map { tuple ⇒
          stringExtractionPair2PathMatcher(tuple)
        }
        matchers.reduceLeft(_ | _)
      }
    }
    rawPathPrefix(matcher ~ Slash) hmap { x ⇒ x.head }
  }
  */

  /** Custom directive that uses a redirect to add a trailing slashe to segment
    * if the slash isn't present.
    * def directory[T <: HList](segment: String) = new Directive1[String] {
    * def happly(f: Directive1[String]) = {
    * pathPrefix(segment ~ PathEndNoSlash) {
    * redirect("/" + segment + "/", StatusCodes.MovedPermanently)
    * } ~
    * rawPathPrefix(segment).hmap { x ⇒ f(segment) }
    * }
    * }
    */
  /*
  def directories[T](segments : Map[String, T]) = new Directive[SegmentsResult[T]] {
    def happly(f : SegmentsResult[T] ⇒ Route) : Route = {
      // match a slash followed by any of the segments in the map
      rawPathPrefix(Slash) {
        rawPathPrefixWithMatch(segments).happly(f)
      }
    }
  }
  */
}

/** Spray's PathEnd matches trailing optional slashes... we can't have that
  * otherwise it will cause a redirect loop.
object PathEndNoSlash extends PathMatcher[HNil] {
  def apply(path : Path) = path match {
    case Path.Empty ⇒ PathMatcher.Matched.Empty
    case _ ⇒ PathMatcher.Unmatched
  }
}
  */
