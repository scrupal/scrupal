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
import scrupal.test.HTML5Validator

/** Test Suite for Forms */
class FormsSpec extends Specification {

  def throwRequirementFailed = throwA[IllegalArgumentException]("requirement failed")

  "Forms.TextField" should {
    "accept an AnyString_t" in {
      StringField("Foo", "Description", AnyString_t)(BSONDocument("Foo" → BSONString("foo"))) must beEqualTo(None)
    }
    "accept an empty string" in {
      StringField("Foo", "Description", AnyString_t)(BSONDocument("Foo" → BSONString(""))) must beEqualTo(None)
    }
    "reject an invalid string" in {
      val str = BSONDocument("Foo" → BSONString("###"))
      val field = StringField("Foo", "Description", DomainName_t)
      val result = field(str)
      result.isDefined must beTrue
      result.get.size must beEqualTo(1)
      result.get(0) must contain("does not match pattern")
    }
    "render correctly" in {
      val field = StringField("Foo", "Description", AnyString_t)
      val content = field.render(Forms.emptyForm).render
      content must beEqualTo("""<input type="text" name="Foo" title="Description" />""")
    }
  }

  "Forms.IntegerField" should {
    "accept a valid number" in {
      IntegerField("Foo", "Description", AnyInteger_t)(BSONDocument("Foo" → BSONInteger(42))) must beEqualTo(None)
    }
    "reject an invalid number" in {
      val result = IntegerField("F", "Description", AnyInteger_t)(BSONDocument("F" → BSONString("Foo")))
      result.isDefined must beTrue
      result.get.size must beEqualTo(1)
      result.get(0) must contain("Expected value of type BSONInteger")
    }
    "reject an out of range number" in {
      val result = IntegerField("F", "Description", TcpPort_t)(BSONDocument("F" → BSONLong(-42)))
      result.isDefined must beTrue
      result.get.size must beEqualTo(1)
      result.get(0) must contain("is out of range, below minimum of")
    }
    "render correctly" in {
      val field = IntegerField("F", "Description", TcpPort_t, minVal = 0, maxVal = 100)
      val content = field.render(Forms.emptyForm).render
      content must beEqualTo("""<input type="number" name="F" min="0.0" max="100.0" title="Description" />""")
    }
  }

  "Forms" should {
    "reject an empty FieldSet" in {
      FieldSet("Foo", "description", "title", Seq.empty[Forms.Field]) must throwRequirementFailed
    }
    "reject an empty Form" in {
      SimpleForm('Foo, "Foo", "Description", "/foo", Seq.empty[FieldItem]) must throwRequirementFailed
    }
    "accept a valid complex form" in {
      val form =
        SimpleForm('Foo2, "Foo", "Description", "/foo", Seq(
          FieldSet("This", "Description", "title", Seq( StringField("A", "An A", Identifier_t) ))
        ))

      form(
        BSONDocument("action" → "/foo", "fields" →
          BSONArray(
            BSONDocument("This" → BSONArray( BSONDocument( "A" → BSONString("foo") ) ) )
          )
        )
      ) must beEqualTo(None)
    }
    "render correctly" in {
      val form =
        SimpleForm('Foo3, "Foo", "Description", "/foo", Seq(
          FieldSet("This", "Description", "title", Seq(
            StringField("A", "An A", Identifier_t),
            PasswordField("P", "A P", Password_t),
            TextAreaField("TA", "A TA", AnyString_t),
            BooleanField("B", "A B", Boolean_t),
            IntegerField("I", "An I", AnyInteger_t, minVal = 0, maxVal = 100),
            RangeField("Rng", "A Rng", AnyReal_t),
            RealField("R", "A R", AnyReal_t, minVal = 0.0, maxVal = 100.0),
            SelectionField("S", "A S", UnspecificQuantity_t),
            TimestampField("T", "A T", AnyTimestamp_t)
          ))
        ))
      HTML5Validator.validate(form.render) must beTrue
    }
  }
}
