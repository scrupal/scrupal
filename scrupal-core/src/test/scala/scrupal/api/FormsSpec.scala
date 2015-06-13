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

package scrupal.api

import org.specs2.mutable.Specification
import reactivemongo.bson._
import scrupal.api.types._
import scrupal.test.HTML5Validator

/** Test Suite for Forms */
class FormsSpec extends Specification {

  def throwRequirementFailed = throwA[IllegalArgumentException]("requirement failed")

  "Forms.TextField" should {
    "accept an AnyString_t" in {
      val field = TextFormField("Foo", "Description", AnyString_t)
      val doc = BSONString("foo")
      field.validate(doc) must beEqualTo(ValidationSucceeded(field,doc))
    }
    "accept an empty string" in {
      val field = TextFormField("Foo", "Description", AnyString_t)
      val doc = BSONString("")
      field.validate(doc) must beEqualTo(ValidationSucceeded(field, doc))
    }
    "reject an invalid string" in {
      val str = BSONString("###")
      val field = TextFormField("Foo", "Description", DomainName_t)
      val result = field.validate(str)
      result.isError must beTrue
      result.isInstanceOf[TypeValidationError[BSONValue,_]] must beTrue
      val error = result.asInstanceOf[TypeValidationError[BSONValue,_]]
      error.message.toString must contain("does not match pattern")
    }
    "render correctly" in {
      val field = TextFormField("Foo", "Description", AnyString_t, optional=true)
      val content = field.render(emptyForm).render
      content must beEqualTo(
        """<input type="text" name="Foo" title="Description" />""")
    }
  }

  "Forms.IntegerField" should {
    "accept a valid number" in {
      val field = IntegerFormField("Foo", "Description", AnyInteger_t)
      val doc = BSONInteger(42)
      field.validate(doc) must beEqualTo(ValidationSucceeded(field, doc))
    }
    "reject an invalid number" in {
      val field = IntegerFormField("F", "Description", AnyInteger_t)
      val doc = BSONString("Foo")
      val result = field.validate(doc)
      result.isError must beTrue
      result.isInstanceOf[TypeValidationError[BSONValue, _]] must beTrue
      val error = result.asInstanceOf[TypeValidationError[BSONValue, _]]
      error.message.toString must contain("is not convertible to a numeric")
    }
    "reject an out of range number" in {
      val field = IntegerFormField("F", "Description", TcpPort_t)
      val doc = BSONLong(-42)
      val result = field.validate(doc)
      result.isError must beTrue
      result.isInstanceOf[TypeValidationError[BSONValue,_]] must beTrue
      val error = result.asInstanceOf[TypeValidationError[BSONValue, _]]
      error.message.toString must contain("is out of range, below minimum of")
    }
    "render correctly" in {
      val field = IntegerFormField("F", "Description", TcpPort_t, minVal = 0, maxVal = 100)
      val content = field.render(emptyForm).render
      content must beEqualTo(
        """<input type="number" name="F" min="0.0" max="100.0" title="Description" required="required" />""")
    }
  }

  "Forms" should {
    "reject an empty FieldSet" in {
      FieldSet("Foo", "description", "title", Seq.empty[FormField]) must throwRequirementFailed
    }
    "reject an empty Form" in {
      SimpleForm('Foo, "Foo", "Description", "/foo", Seq.empty[FormItem]) must throwRequirementFailed
    }
    "accept a valid complex form" in {
      val form =
        SimpleForm('Foo2, "Foo", "Description", "/foo", Seq(
          FieldSet("This", "Description", "title", Seq( TextFormField("A", "An A", Identifier_t) ))
        ))
      val doc = BSONDocument("This" →  BSONDocument( "A" → BSONString("foo")  ) )
      form.validate( doc ) must beEqualTo(ValidationSucceeded(form, doc))
    }
    "render correctly" in {
      val form =
        SimpleForm('Foo3, "Foo", "Description", "/foo", Seq(
          FieldSet("This", "Description", "title", Seq(
            TextFormField("A", "An A", Identifier_t),
            PasswordFormField("P", "A P", Password_t),
            TextAreaFormField("TA", "A TA", AnyString_t),
            BooleanFormField("B", "A B", Boolean_t),
            IntegerFormField("I", "An I", AnyInteger_t, minVal = 0, maxVal = 100),
            RangeFormField("Rng", "A Rng", AnyReal_t),
            RealFormField("R", "A R", AnyReal_t, minVal = 0.0, maxVal = 100.0),
            SelectionFormField("S", "A S", UnspecificQuantity_t),
            TimestampFormField("T", "A T", AnyTimestamp_t)
          ))
        ))
      HTML5Validator.validate(form.render) must beTrue
    }
  }
}
