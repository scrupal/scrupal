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

import play.api.mvc.{AnyContent, AnyContentAsFormUrlEncoded, Request, RequestHeader}
import play.api.test.FakeRequest
import scrupal.api.Form._
import scrupal.utils.Validation.{Failure, Success}
import scrupal.test.{ScrupalSpecification, HTML5Validator}

import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration._

/** Test Suite for Forms */
class FormsSpec extends ScrupalSpecification("Forms") {

  def throwRequirementFailed = throwA[IllegalArgumentException]("requirement failed")

  "Forms.TextField" should {
    "accept an AnyString_t" in {
      val field = TextField("Foo", "Description", AnyString_t)
      val doc : Atom = "foo"
      field.validate(doc) must beEqualTo(Success[Atom](field.location,doc))
    }
    "accept an empty string" in {
      val field = TextField("Foo", "Description", AnyString_t)
      val doc : Atom = ""
      field.validate(doc) must beEqualTo(Success(field.location, doc))
    }
    "reject an invalid string" in {
      val str : Atom = "###"
      val field = TextField("Foo", "Description", DomainName_t)
      val result = field.validate(str)
      result.isError must beTrue
      result.isInstanceOf[Failure[Atom]] must beTrue
      val error = result.asInstanceOf[Failure[Atom]]
      error.message.toString must contain("does not match a domain name")
    }
    "render correctly" in {
      val field = TextField("Foo", "Description", AnyString_t, optional=true)
      val content = field.render(emptyForm).render
      content must beEqualTo(
        """<input type="text" name="Foo" title="Description" />""")
    }
  }

  "Forms.IntegerField" should {
    "accept a valid number" in {
      val field = IntegerField("Foo", "Description", AnyInteger_t)
      val doc : Atom = 42
      field.validate(doc) must beEqualTo(Success(field.location, doc))
    }
    "reject an invalid number" in {
      val field = IntegerField("F", "Description", AnyInteger_t)
      val doc : Atom = "Foo"
      val result = field.validate(doc)
      result.isError must beTrue
      result.isInstanceOf[Failure[Atom]] must beTrue
      val error = result.asInstanceOf[Failure[Atom]]
      error.message.toString must contain("is not convertible to a number")
    }
    "reject an out of range number" in {
      val field = IntegerField("F", "Description", TcpPort_t)
      val doc : Atom = -42L
      val result = field.validate(doc)
      result.isError must beTrue
      result.isInstanceOf[Failure[Atom]] must beTrue
      val error = result.asInstanceOf[Failure[Atom]]
      error.message.toString must contain("is out of range, below minimum of")
    }
    "render correctly" in {
      val field = IntegerField("F", "Description", TcpPort_t, minVal = 0, maxVal = 100)
      val content = field.render(emptyForm).render
      content must beEqualTo(
        """<input type="number" name="F" min="0.0" max="100.0" title="Description" required="required" />""")
    }
  }

  "Forms" should {

    "reject an empty FieldSet" in {
      FieldSet("Foo", "description", "title", Seq.empty[Form.Field]) must throwRequirementFailed
    }

    "reject an empty Form" in {
      Simple('Foo, "Foo", "Description", "/foo", Seq.empty[Form.Field]) must throwRequirementFailed
    }

    "validate a valid form" in {
      val form = Simple('Foo2, "Foo", "Description", "/foo", Seq(TextField("A", "An A", Identifier_t)))
      val doc : Map[String,Atom] = Map("A" → "foo")
      form.validate(doc) must beEqualTo(Success(form.location, doc))
    }

    "render correctly" in {
      val form =
        Simple('Foo3, "Foo", "Description", "/foo", Seq(
          TextField("A", "An A", Identifier_t),
          PasswordField("P", "A P", Password_t),
          TextAreaField("TA", "A TA", AnyString_t),
          BooleanField("B", "A B", Boolean_t),
          IntegerField("I", "An I", AnyInteger_t, minVal = 0, maxVal = 100),
          RangeField("Rng", "A Rng", AnyReal_t),
          RealField("R", "A R", AnyReal_t, minVal = 0.0, maxVal = 100.0),
          SelectionField("S", "A S", UnspecificQuantity_t),
          TimestampField("T", "A T", AnyTimestamp_t)
        ))
      HTML5Validator.validate(form.render) must beTrue
    }

    "accept content correctly" in {
      val form =
        Simple('Foo4, "Foo", "Description", "/foo", Seq(
          TextField("A", "An A", Identifier_t),
          PasswordField("P", "A P", Password_t),
          TextAreaField("TA", "A TA", AnyString_t),
          BooleanField("B", "A B", Boolean_t),
          IntegerField("I", "An I", AnyInteger_t, minVal = 0, maxVal = 100),
          RangeField("Rng", "A Rng", AnyReal_t),
          RealField("R", "A R", AnyReal_t, minVal = 0.0, maxVal = 100.0),
          SelectionField("S", "A S", UnspecificQuantity_t),
          TimestampField("T", "A T", AnyTimestamp_t)
        ))
      val acceptor : Form.AcceptForm = form.provideAcceptReactor("/foo")

      val context = Context(testScrupal)
      val data : Map[String,Seq[String]] = Map (
        "A" → Seq("foo"),
        "P" → Seq("AvbC123!@#"),
        "TA" → Seq(";lkjasdf;lkjasd;flkja;sdflkjas;ldfkja;lsdfkja;sdlkfja;sldkjf"),
        "B" → Seq("false"),
        "I" → Seq("42"),
        "Rng" → Seq("42.0"),
        "R" → Seq("42.0"),
        "S" → Seq("Some"),
        "T" → Seq("1435691518000")
      )

      val header : RequestHeader = FakeRequest("GET","/")
      val stimulus = Stimulus(context, Request[AnyContent](header, AnyContentAsFormUrlEncoded(data)))
      context.withExecutionContext { implicit ec : ExecutionContext ⇒
        val f = acceptor.apply(stimulus) map {
          case e: ErrorResponse ⇒
            println(e)
            e.disposition.isSuccessful must beTrue
          case t: ExceptionResponse ⇒
            println(t)
            t.disposition.isSuccessful must beTrue
          case s: StringResponse ⇒
            s.content.contains( "Submission of 'Foo' succeeded.") must beTrue
          case x: Response ⇒
            println(x)
            x.disposition must beEqualTo(Successful)
        }
        Await.result(f, 2.seconds)
      }
    }
    // TODO: Validate that forms reject each kind of data type correctly
  }
}
