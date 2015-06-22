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

package scrupal.test

import java.io.{ InputStream, ByteArrayInputStream }
import java.nio.charset.StandardCharsets

import nu.validator.htmlparser.dom.HtmlDocumentBuilder
import org.xml.sax.InputSource
import scrupal.api.Context
import scrupal.api.Html.{ Page, TagContent }
import scrupal.utils.ScrupalComponent

import scala.util.{ Failure, Success, Try }

object HTML5Validator extends ScrupalComponent {

  def validateFragment(document : String, contextElement : String = "div") : Boolean = {
    Try {
      val stream : InputStream = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8))
      try {
        val builder = new HtmlDocumentBuilder()
        val is = new InputSource(stream)
        val doc = builder.parseFragment(is, contextElement)
        doc.hasChildNodes
      } finally {
        stream.close()
      }
    } match {
      case Success(x) ⇒ x
      case Failure(x) ⇒
        log.warn("HTML5 validation failed:", x)
        false
    }
  }

  def validateDocument(document : String) : Boolean = {
    Try {
      val stream : InputStream = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8))
      try {
        val builder = new HtmlDocumentBuilder()
        val is = new InputSource(stream)
        val doc = builder.parse(is)
        doc.hasChildNodes
      } finally {
        stream.close()
      }
    } match {
      case Success(x) ⇒ x
      case Failure(x) ⇒
        log.warn("HTML5 validation failed:", x)
        false
    }
  }

  def validate(document : TagContent) : Boolean = validateFragment(document.render)

  def validate(document : Page, context : Context) : Boolean = validateDocument(document.render(context))

  /*
  val request = WS.url("http://html5.validator.nu/").
    withQueryString("out" -> "json").
    withHeaders(CONTENT_TYPE -> ContentTypes.HTML(Codec.utf_8))

  val response = Await.result(request.post(html), Duration.Inf)
  val messages = (response.json \ "messages").asOpt[JsArray]
  messages must beSome.which(_.value must haveSize(0))
*/
}
