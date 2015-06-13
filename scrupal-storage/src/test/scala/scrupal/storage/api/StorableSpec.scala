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

package scrupal.storage.api

import scrupal.test.ScrupalSpecification

class StorableSpec extends ScrupalSpecification("Storable") {

  case class Number(value: Int)

  object FourtyTwo extends Number(42)
  object FourtyOne extends Number(41)
  val n_42 = Number(42)
  val n_41 = Number(41)

  class NumberWithEquality(n: Int) extends Number(n) {
    override def equals(other: Any) : Boolean = {
      other match {
        case that: NumberWithEquality => value == that.value
        case _ => false
      }
    }
  }

  object FourtyTwoE extends NumberWithEquality(42)
  object FourtyOneE extends NumberWithEquality(41)

  "Storable" should {
    "not infer inequality erroneously" in {
      n_42.equals(FourtyTwo) must beTrue
      FourtyTwo.equals(FourtyTwo) must beTrue
    }
    "infer equality correctly" in {
      n_41.equals(FourtyTwo) must beFalse
      n_41.equals(FourtyOne) must beTrue
      FourtyTwo.equals(FourtyOne) must beFalse
    }
    "infer equality correctly with overriden equals method" in {
      FourtyTwoE.equals(FourtyOneE) must beFalse
    }
  }
}
