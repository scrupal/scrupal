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

import org.specs2.mutable.Specification
import reactivemongo.bson._
import scrupal.core.api.Forms._
import scrupal.core.types._

/** Test Suite for Forms */
class FormsSpec extends Specification {

  def throwRequirementFailed = throwA[IllegalArgumentException]("requirement failed")

  "Forms.TextField" should {
    "accept an AnyString_t" in {
      StringField("foo", "Foo", "Description", AnyString_t)(BSONString("foo")) must beEqualTo(None)
    }
    "accept an empty string" in {
      StringField("foo", "Foo", "Description", AnyString_t)(BSONString("")) must beEqualTo(None)
    }
    "reject an invalid string" in {
      val str = BSONString("###")
      val field = StringField("foo", "Foo", "Description", DomainName_t)
      val result = field(str)
      result.isDefined must beTrue
      result.get.size must beEqualTo(1)
      result.get(0) must contain("does not match pattern")
    }
  }

  "Forms.IntegerField" should {
    "accept a valid number" in {
      IntegerField("foo", "Foo", "Description", AnyInteger_t)(BSONInteger(42)) must beEqualTo(None)
    }
    "reject an invalid number" in {
      val result = IntegerField("f", "F", "Description", AnyInteger_t)(BSONString("Foo"))
      result.isDefined must beTrue
      result.get.size must beEqualTo(1)
      result.get(0) must contain("Expected value of type BSONInteger")
    }
    "reject an out of range number" in {
      val result = IntegerField("f", "F", "Description", TcpPort_t)(BSONLong(-42))
      result.isDefined must beTrue
      result.get.size must beEqualTo(1)
      result.get(0) must contain("is out of range, below minimum of")
    }
  }

  "Forms" should {
    "reject an empty FieldSet" in {
      FieldSet("foo", "Foo", "description", "title", Seq.empty[Forms.Field]) must throwRequirementFailed
    }
    "reject an empty Form" in {
      Form("foo", SubmitAction("foo",None), Seq.empty[FieldItem], Settings.Empty) must throwRequirementFailed
    }
    "accept a valid complex form" in {
      val form =
        Form("foo", SubmitAction("foo",None), Seq(
          FieldSet("this","This", "Description", "title", Seq(StringField("a", "A", "An A", Identifier_t)))
        ), Settings.Empty)

      form(BSONDocument("_id" → "foo", "submit" → BSONDocument("_id" → "foo"), "fields" → BSONArray(
        BSONDocument("_id" → "this", "name" → "This", "description" → "Description", "title" → "Title",
          "inputs" → BSONArray(
            BSONDocument("_id" → "a", "name" → "A", "description" → "An A", "typ" → "Identifier")
        ))
      ), "defaults" → BSONDocument() )) must beEqualTo(None)
    }
  }
}
