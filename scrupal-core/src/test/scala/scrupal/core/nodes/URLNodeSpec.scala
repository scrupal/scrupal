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

package scrupal.core.nodes

import java.io.StringWriter
import java.net.URL

import akka.http.scaladsl.model.MediaTypes
import org.apache.commons.io.IOUtils
import scrupal.api._
import scrupal.test.{NodeTest, ScrupalSpecification}

/** Test Case For CommandNode */
class URLNodeSpec extends ScrupalSpecification("MessageNode") with NodeTest {

  lazy val node = URLNode(specName,specName, new URL("http://scrupal.github.io/"), MediaTypes.`text/html`)

  s"$specName" should {
    "handle ..." in nodeTest(node) { r: Response ⇒
      r.mediaType must beEqualTo(MediaTypes.`text/html`)
      r.disposition.isSuccessful must beTrue
      r.isInstanceOf[StreamResponse] must beTrue
      val expected = "<title>Welcome by scrupal</title>"
      r match {
        case s: StreamResponse ⇒
          val writer = new StringWriter()
          IOUtils.copy(s.content, writer, utf8)
          val rendered = writer.toString
          rendered.startsWith("<!DOCTYPE html>") must beTrue
          rendered.endsWith("</html>") must beTrue
          rendered.contains(expected) must beTrue
        case e: ErrorResponse ⇒ failure("Error: " + e.formatted)
        case x: ExceptionResponse ⇒ throw x.content
        case _ ⇒ throw new Exception("Unexpected result type")
      }
    }
  }
}
