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

import org.specs2.mutable.Specification
import reactivemongo.bson._
import scrupal.api.Forms._

/** Test Suite for Forms */
class FormsSpec extends Specification {

  def throwRequirementFailed = throwA[IllegalArgumentException]("requirement failed")

  "Forms.Input" should {
    "reject NotAType, UnFoundType, and AnyType" in {
      Input("foo", "Foo", Type.NotAType) must throwRequirementFailed
      Input("foo", "Foo", UnfoundType('unfound)) must throwRequirementFailed
      Input("foo", "Foo", AnyType_t) must throwRequirementFailed
    }
    "accept a valid number" in {
      Input("foo", "Foo", AnyInteger_t)(BSONInteger(42)) must beEqualTo(None)
    }
    "reject an invalid number" in {
      val result = Input("f", "F", AnyInteger_t)(BSONString("Foo"))
      result.isDefined must beTrue
      result.get.size must beEqualTo(1)
      result.get(0) must contain("Expected value of type BSONInteger")
    }
  }

  "Forms" should {
    "reject an empty Section" in {
      Section("foo", "Foo", Seq.empty[Input]) must throwRequirementFailed
    }
    "reject an empty Page" in {
      Page("foo", "foo", Seq.empty[Section]) must throwRequirementFailed
    }
    "reject an empty Form" in {
      Form("foo",SubmitAction("foo",None), Seq.empty[Page]) must throwRequirementFailed
    }
    "accept a valid complex form" in {
      val form =
        Form("foo", SubmitAction("foo",None), Seq(
          Page("one", "One", Seq(
            Section("this","This", Seq(
              Input("a", "An A", Identifier_t)
            ))
          ))
        ))
      form(BSONDocument("_id" → "foo", "submit" → BSONDocument("_id" → "foo"), "pages" → BSONArray(
        BSONDocument("_id" → "one", "description" → "one", "sections" → BSONArray(
          BSONDocument("_id" → "this", "description" → "This", "inputs" → BSONArray(
            BSONDocument("_id" → "a", "description" → "An A", "typ" → "Identifier")
          ))
        ))
      ), "default" → BSONNull )) must beEqualTo(None)
    }
  }
}
