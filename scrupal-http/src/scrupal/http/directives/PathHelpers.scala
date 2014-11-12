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
      else
        segments.map {
          case (prefix, value) ⇒
            val provided : SegmentsResult[T] = HList(prefix,value)
            stringExtractionPair2PathMatcher(prefix, provided)
        }.reduceLeft(_ | _)
    }
    rawPathPrefix(matcher) hmap { x ⇒ x.head }
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
