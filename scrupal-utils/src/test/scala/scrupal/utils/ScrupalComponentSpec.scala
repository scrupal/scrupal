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

package scrupal.utils

import org.specs2.mutable.Specification

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/** Test Suite for ScrupalComponent
  */
class ScrupalComponentSpec extends Specification {
  "ScrupalComponent" should {
    "supply a logger" in {
      case object test1 extends ScrupalComponent
      test1.log.info("Logger created")
      success
    }
    "produce value when awaiting without timeout" in {
      case object test1a extends ScrupalComponent
      test1a.await( Future { 42 }, FiniteDuration(5,"seconds"), "test await") match {
        case Success(x: Int) ⇒
          (x must beEqualTo(42)).toResult
        case Failure(x) ⇒
          failure("Got exception $x but expecting success")
      }
    }
    "produce decent exception on await timeout" in {
      case object test1a extends ScrupalComponent
      test1a.await( Future { 42 }, FiniteDuration(0,"seconds"), "test await") match {
        case x: Success[Int] ⇒ failure
        case Failure(x) ⇒
          x.getMessage must contain("while waiting")
          (x.getMessage must contain("test await")).toResult
      }
    }
  }

  "ScrupalException" should {
    "construct with a cause" in {
      case object test2 extends ScrupalComponent
      val xcptn = ScrupalException(test2, "foo", new IllegalArgumentException("foo"))
      xcptn.isInstanceOf[Throwable]
    }
    "construct without a cause" in {
      case object test3 extends ScrupalComponent
      val xcptn = new ScrupalException(test3, "foo")
      xcptn.isInstanceOf[Throwable]
    }
    "provide an acceptable message" in {
      case object test4 extends ScrupalComponent
      val xcptn = new ScrupalException(test4, "foo")
      xcptn.getMessage must contain("foo")
      xcptn.getMessage must contain("test4")
    }
  }
}
