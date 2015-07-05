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

package scrupal.api.html

import play.api.libs.json._
import scrupal.api.Html
import scrupal.api.Html.{SimpleGenerator, Contents, ContentsArgs, Generator}
import scrupal.test.ScrupalSpecification

import scalatags.Text.all._

/** Title Of Thing.
  *
  * Description of thing
  */
class utilsSpec extends ScrupalSpecification("html-utils") {

  case class EasyGen(content: Contents) extends SimpleGenerator { def apply() : Contents = content }
  lazy val theArgs : ContentsArgs = Map(
    "a" → EasyGen(Seq(span("a"),span("1"))),
    "b" → EasyGen(Seq(span("b"),span("2")))
  )
  def generatorTest(gen: Generator, content: Contents) = {
    gen.render(context, theArgs) must beEqualTo(Html.renderContents(content))
    // gen.generate(context, theArgs) must beEqualTo(content) FIXME: This should be true!
    for (tag ← theArgs.keys) {
      gen.tag(tag, context, theArgs).nonEmpty must beTrue
    }
    success
  }

  "utils" should {
    "have a danger generator" in {
      generatorTest(danger(Seq(span("foo"))), Seq(div(cls:="bg-danger",span("foo"))))
    }
    "have a warning generator" in {
      generatorTest(warning(Seq(span("foo"))), Seq(div(cls:="bg-warning",span("foo"))))
    }
    "have a success generator" in {
      generatorTest(_root_.scrupal.api.html.success(Seq(span("foo"))), Seq(div(cls:="bg-success",span("foo"))))
    }
    "have a json document panel" in {
      val panel = json_document_panel("Test Panel",
        JsObject(Map(
          "string" → JsString("foo"),
          "number" → JsNumber(42),
          "boolean" → JsBoolean(true),
          "null" → JsNull,
          "object" → JsObject(Map("a" → JsString("a"))),
          "array" → JsArray(Seq(JsNumber(42), JsString("fourty-two")))
        ))
      )
      val content = panel.generate(context, Html.EmptyContentsArgs).toString()
      content must contain("<div class=\"panel panel-primary\"")
      content must contain("<div class=\"panel-body\"")
      content must contain("Test Panel")
      content must contain("&quot;foo&quot;")
      content must contain("42")
      content must contain("true")
      content must contain("[42, &quot;fourty-two&quot;, ]")
    }
    "have a copyright generator" in {
      val content = reactific_copyright.toString()
      content must contain("Copyright")
      content must contain("Reactific Software LLC")
    }
  }

}
