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

package scrupal.core.api

import java.util.concurrent.TimeUnit

import scrupal.test.{ScrupalSpecification, FakeContext}
import shapeless.{::, HList, HNil}
import spray.http.Uri
import spray.routing.PathMatcher
import spray.routing.PathMatcher.{Unmatched, Matched}
import spray.routing.PathMatchers._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration


/** Test Suite for Actions and Related Traits */
class ActionSpec extends ScrupalSpecification("ActionSpec") {

  case class Fixture(name: String, path: String) extends FakeContext(name,path) {

    class TestActionProducer[L <: HList](pm: PathMatcher[L]) extends ActionProducer(pm) {
      def actionFor(list: L, ctxt: Context) : Option[Action] = {
        Some(new Action {
          val context = ctxt
          def apply() : Future[Result[_]] = {
            Future.successful(StringResult(list.toString))
          }
        })
      }
    }

    val int_p2a = new TestActionProducer(PathMatcher("foo")/IntNumber)

    val empty_p2a = new ActionProducer(PathMatcher("bar")) {
      def actionFor(list: HNil, ctxt: Context): Option[Action] = {
        Some(new Action {
          val context = ctxt;
          def apply(): Future[Result[_]] = Future {
            StringResult("")
          }
        })
      }
    }
    val provider0 = new ActionProvider {
      val segment = "p0"
      override def delegates = Seq.empty[ActionExtractor]
    }
    val provider1 = new ActionProvider {
      val segment = "p1"
      override def delegates = Seq(int_p2a)
    }
    val provider2 = new ActionProvider {
      val segment = "p2"
      override def delegates = Seq(empty_p2a, int_p2a)
    }
  }

  "PathMatcher" should {
    "match with and without trailing /" in {
      val pm : PathMatcher[::[String,HNil]] =
        PathMatcher(Uri.Path("foo"),"foo"::HNil) |
          PathMatcher(Uri.Path("foo"),"foo"::HNil) ~ Slash
      val path1 = Uri.Path("foo")
      val match1 = pm(path1) match {
        case Matched(pathRest, extractions) ⇒ extractions.head
        case Unmatched ⇒ "bar"
      }
      match1 must beEqualTo("foo")
      val path2 = Uri.Path("foo/")
      val match2 = pm(path1) match {
        case Matched(pathRest, extractions) ⇒ extractions.head
        case Unmatched ⇒ "bar"
      }
      match2 must beEqualTo("foo")
    }
  }

  "Action" should {
    "perform some tests" in {
      pending
    }
  }



  "PathToAction" should {
    "map integer to string" in Fixture("p2a1", "foo/42") { fix : Fixture ⇒
      val matched = fix.int_p2a.extractAction(fix) match {
        case Some(action) ⇒
          val f = action().map { r: Result[_] ⇒ r.asInstanceOf[StringResult].payload  }
          Await.result(f, Duration(1,TimeUnit.SECONDS))
        case None ⇒ "0"
      }
      matched must beEqualTo ("42 :: HNil")
    }

    "map empty to nothing" in Fixture("p2a2", "bar") { fix : Fixture ⇒
      val matched = fix.empty_p2a.extractAction(fix) match {
        case Some(action) ⇒
          val f2 = action().map { r: Result[_] ⇒ r.asInstanceOf[StringResult].payload }
          Await.result(f2, Duration(1, TimeUnit.SECONDS))
        case None ⇒ "Nope"
      }
      matched.isEmpty must beTrue
    }
  }

  "ActionProvider" should {
    "find None with an empty pathsToActions" in Fixture("ap1", "foo") { fix: Fixture ⇒
      fix.provider0.actionFor("", fix) must beEqualTo(None)
    }
    "find the matching one" in Fixture("ap2", "foo/42") { fix: Fixture ⇒
      val result = fix.provider1.actionFor("", fix)
      val str = result match {
        case Some(action) ⇒
          val f = action().map { r: Result[_] ⇒ r.asInstanceOf[StringResult].payload  }
          Await.result(f, Duration(1,TimeUnit.SECONDS))
        case None ⇒ ""
      }
      str must beEqualTo("42 :: HNil")
    }
    "match second item in provider2" in Fixture("api3", "bar") { fix: Fixture ⇒
      val result = fix.provider2.actionFor("", fix)
      val str = result match {
        case Some(action) ⇒
          val f = action().map { r: Result[_] ⇒ r.asInstanceOf[StringResult].payload}
          Await.result(f, Duration(1, TimeUnit.SECONDS))
        case None ⇒ "Nope"
      }
      str must beEqualTo("")
    }
    "match first item in provider2" in Fixture("api3", "foo/42") { fix: Fixture ⇒
      val r2 = fix.provider2.actionFor("", fix)
      val s2 = r2 match {
        case Some(action) ⇒
          val f = action().map { r: Result[_] ⇒ r.asInstanceOf[StringResult].payload  }
          Await.result(f, Duration(1,TimeUnit.SECONDS))
        case None ⇒ "Nope"
      }
      s2 must beEqualTo("42 :: HNil")
    }
  }
}
