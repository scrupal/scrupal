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

package scrupal.http.directives

import shapeless.{HList, HNil}
import spray.http.Uri.Path
import spray.routing._
import spray.routing.Directives._

/**
 * Created by reidspencer on 11/10/14.
 */
trait PathHelpers {
  type SegmentsResult[T] = shapeless.::[String,shapeless.::[T,HNil]]

  def rawPathPrefixWithMatch[T](segments: Map[String,T]) : Directive[SegmentsResult[T]] = {
    val matcher = {
      if (segments.isEmpty)
        PathMatchers.nothingMatcher
      else {
        val pairs = segments.map {
          case (prefix, value) ⇒
            val provided: SegmentsResult[T] = HList((prefix, value))
            prefix → provided
        }
        val matchers = pairs.toSeq.sortWith { case (l,r) ⇒ l._1.length > r._1.length } map { tuple ⇒
          stringExtractionPair2PathMatcher(tuple)
        }
        matchers.reduceLeft(_ | _)
      }
    }
    rawPathPrefix(matcher ~ Slash) hmap { x ⇒ x.head }
  }


  /**
   * Custom directive that uses a redirect to add a trailing slashe to segment
   * if the slash isn't present.
     def directory[T <: HList](segment: String) = new Directive1[String] {
    def happly(f: Directive1[String]) = {
      pathPrefix(segment ~ PathEndNoSlash) {
        redirect("/" + segment + "/", StatusCodes.MovedPermanently)
      } ~
      rawPathPrefix(segment).hmap { x ⇒ f(segment) }
    }
  }
   */
  def directories[T](segments: Map[String,T]) = new Directive[SegmentsResult[T]] {
    def happly(f: SegmentsResult[T] ⇒ Route) : Route = {
      // match a slash followed by any of the segments in the map
      rawPathPrefix(Slash) {
        rawPathPrefixWithMatch(segments).happly(f)
      }
    }
  }
}

/**
 * Spray's PathEnd matches trailing optional slashes... we can't have that
 * otherwise it will cause a redirect loop.
 */
object PathEndNoSlash extends PathMatcher[HNil] {
  def apply(path: Path) = path match {
    case Path.Empty ⇒ PathMatcher.Matched.Empty
    case _          ⇒ PathMatcher.Unmatched
  }
}
