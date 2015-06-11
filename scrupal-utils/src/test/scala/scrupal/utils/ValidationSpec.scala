/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
  *                                                                                                    *
  * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
  *                                                                                                    *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
  * with the License. You may obtain a copy of the License at                                                          *
  *                                                                                                    *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                     *
  *                                                                                                    *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
  * the specific language governing permissions and limitations under the License.                                     *
  * ********************************************************************************************************************
  */

package scrupal.utils

import scrupal.utils.Validation._

import scrupal.test.ScrupalSpecification

/** Test Spec for Validator */
class ValidationSpec extends ScrupalSpecification("Validator") {

  "ValidationLocation" should {
    "correctly derive indexable" in {
      val base = SimpleLocation("place")
      val sub_1 = base.index(1)
      sub_1.location must beEqualTo("place[1]")
    }
    "correctly derive selectable" in {
      val base = SimpleLocation("place")
      val sub_1 = base.select("foo")
      sub_1.location must beEqualTo("place.foo")
    }
    "handle multiple nestings" in {
      val base = SimpleLocation("place")
      val sub_1 = base.select("foo")
      val sub_2 = sub_1.index(42)
      val sub_3 = sub_2.select("bar")
      val sub_4 = sub_3.index(21)
      sub_4.location must beEqualTo("place.foo[42].bar[21]")
    }
  }

  "Results" should {
    "allow simple success of typed location" in {
      val s = Success(TypedLocation(13), 13)
      s.message must beEqualTo("Validation succeeded, at 13.")
    }
    "collect several results" in {
      val s = StringFailure(DefaultLocation, 13, "Unlucky number")
      s.add(StringFailure(DefaultLocation.index(0), 1, "First digit is 1"))
      s.add(StringFailure(DefaultLocation.index(1), 3, "Second digit is 3"))
      s.message must beEqualTo("Unlucky number, at somewhere.")
    }
  }

  class SmallIntValidator extends Validator[Int] {
    /** Validate value of type VType with this validator
      *
      * @param ref The location at which the value occurs
      * @param value the VType to be validated
      * @return Any of the Results
      */
    override def validate(ref : Location, value : Int) : VResult = {
      if (value < -10 || value > 10)
        StringFailure(ref, value, s"Value out of range [-10,10]")
      else
        Success(ref, value)
    }
  }

  object SmallIntValidator {
    implicit class SIVPimp(val value : Int) {
      val validator = new SmallIntValidator
      def validate : Results[Int] = validator.validate(TypedLocation(value), value)
    }
  }

  "Validator" should {
    "validate simple type" in {
      import SmallIntValidator._
      val vr1 = 13.validate
      val vr2 = 7.validate
      vr1.message must beEqualTo("Value out of range [-10,10], at 13.")
      vr2.message must beEqualTo("Validation succeeded, at 7.")
    }
  }
}
