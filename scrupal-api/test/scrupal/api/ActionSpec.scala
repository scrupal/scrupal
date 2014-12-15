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

package scrupal.api

import java.util.concurrent.TimeUnit

import scrupal.test.{ScrupalSpecification, FakeContext}
import shapeless.{HList, HNil, ::}
import spray.http.Uri
import spray.routing.PathMatcher
import spray.routing.PathMatchers._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration


/** Test Suite for Actions and Related Traits */
class ActionSpec extends ScrupalSpecification("ActionSpec") {

  case class Fixture(name: String) extends FakeContext(name) {
    val int_p2a = new PathToAction(PathMatcher("foo")/IntNumber) {
      def apply(list: ::[Int,HNil], rest: Uri.Path, ctxt: Context) : Action = {
        new Action {
          val context = ctxt
          def apply() : Future[Result[_]] = {
            Future.successful(StringResult(list.head.toString))
          }
        }
      }
    }
    val empty_p2a = new PathToAction(PathMatcher("foo")) {
      def apply(list: HNil, rest: Uri.Path, ctxt: Context) : Action = {
        new Action { val context = ctxt; def apply() : Future[Result[_]] = Future.successful(StringResult("")) }
      }
    }
    val provider0 = new PathMatcherToActionProvider with TerminalActionProvider {
      val id = 'p0
      override def pathsToActions = Seq.empty[PathToAction[_ <: HList]]
    }
    val provider1 = new PathMatcherToActionProvider with TerminalActionProvider {
      val id = 'p1
      override def pathsToActions = Seq(int_p2a)
    }
    val provider2 = new PathMatcherToActionProvider with TerminalActionProvider {
      val id = 'p2
      override def pathsToActions = Seq(empty_p2a, int_p2a)
    }
  }

  "Action" should {
    "perform some tests" in {
      pending
    }
  }



  "PathToAction" should {
    "map integer to string" in Fixture("p2a1") { fix : Fixture ⇒
      val matched = fix.int_p2a.matches(Uri.Path("foo/42"), fix) match {
        case Some(action) ⇒
          val f = action().map { r: Result[_] ⇒ r.asInstanceOf[StringResult].payload  }
          Await.result(f, Duration(1,TimeUnit.SECONDS))
        case None ⇒ "0"
      }
      matched must beEqualTo ("42")
    }

    "map empty to nothing" in Fixture("p2a2") { fix : Fixture ⇒
      val matched = fix.empty_p2a.matches(Uri.Path("foo"), fix) match {
        case Some(action) ⇒
          val f2 = action().map { r: Result[_] ⇒ r.asInstanceOf[StringResult].payload }
          Await.result(f2, Duration(1, TimeUnit.SECONDS))
        case None ⇒ "Nope"
      }
      matched.isEmpty must beTrue
    }
  }

  "ActionProvider" should {
    "find None with an empty pathsToActions" in Fixture("ap1") { fix: Fixture ⇒
      fix.provider0.actionFor("", "foo", fix) must beEqualTo(None)
    }
    "find the matching one" in Fixture("ap2") { fix: Fixture ⇒
      val result = fix.provider1.actionFor("", "foo/42", fix)
      val str = result match {
        case Some(action) ⇒
          val f = action().map { r: Result[_] ⇒ r.asInstanceOf[StringResult].payload  }
          Await.result(f, Duration(1,TimeUnit.SECONDS))
        case None ⇒ ""
      }
      str must beEqualTo("42")
    }
    "find the first one that matches" in Fixture("api3") { fix : Fixture ⇒
      val result = fix.provider2.actionFor("", "foo", fix)
      val str = result match {
        case Some(action) ⇒
          val f = action().map { r: Result[_] ⇒ r.asInstanceOf[StringResult].payload  }
          Await.result(f, Duration(1,TimeUnit.SECONDS))
        case None ⇒ "Nope"
      }
      str must beEqualTo("")
      val r2 = fix.provider2.actionFor("", "foo/42", fix)
      val s2 = result match {
        case Some(action) ⇒
          val f = action().map { r: Result[_] ⇒ r.asInstanceOf[StringResult].payload  }
          Await.result(f, Duration(1,TimeUnit.SECONDS))
        case None ⇒ "Nope"
      }
      s2 must beEqualTo("")
    }
  }
}
