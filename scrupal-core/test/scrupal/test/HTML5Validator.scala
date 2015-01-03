/**********************************************************************************************************************
 * Copyright © 2015 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.test

import java.io.{InputStream, ByteArrayInputStream}
import java.nio.charset.StandardCharsets

import nu.validator.htmlparser.dom.HtmlDocumentBuilder
import org.xml.sax.InputSource
import scrupal.core.api.Context
import scrupal.core.api.Html.{Page, TagContent}
import scrupal.utils.ScrupalComponent

import scala.util.{Failure, Success, Try}

object HTML5Validator extends ScrupalComponent {

  def validateFragment(document: String, contextElement: String = "div") :Boolean = {
    Try {
      val stream: InputStream = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8))
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
        log.warn("HTML5 validation failed:",x)
        false
    }
  }

  def validateDocument(document: String) : Boolean = {
    Try {
      val stream: InputStream = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8))
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
        log.warn("HTML5 validation failed:",x)
        false
    }
  }

  def validate(document: TagContent) : Boolean = validateFragment(document.render)

  def validate(document: Page, context: Context) : Boolean = validateDocument(document.render(context))

  /*
  val request = WS.url("http://html5.validator.nu/").
    withQueryString("out" -> "json").
    withHeaders(CONTENT_TYPE -> ContentTypes.HTML(Codec.utf_8))

  val response = Await.result(request.post(html), Duration.Inf)
  val messages = (response.json \ "messages").asOpt[JsArray]
  messages must beSome.which(_.value must haveSize(0))
*/
}
