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

import java.io.{StringWriter, File}

import akka.http.scaladsl.model.MediaTypes
import org.apache.commons.io.IOUtils
import scrupal.api._
import scrupal.test.{NodeTest, ScrupalSpecification}

/** Test Case For CommandNode */
class FileNodeSpec extends ScrupalSpecification("FileNode") with NodeTest {

  lazy val node = FileNode(specName,"File Node",
    new File("scrupal-core/src/test/resources/fakeAsset.txt"),MediaTypes.`text/plain`)

  s"$specName" should {
    "load a simple text file" in nodeTest(node) { r: Response ⇒
      r.mediaType must beEqualTo(MediaTypes.`text/plain`)
      r.disposition.isSuccessful must beTrue
      r match {
        case s: StreamResponse ⇒
          val writer = new StringWriter()
          IOUtils.copy(s.content, writer, utf8)
          val rendered = writer.toString
          rendered.startsWith("This") must beTrue
          rendered.contains("works or not.") must beTrue
          rendered.length must beEqualTo(80)
        case e: ErrorResponse ⇒ failure("Error: " + e.formatted)
        case x: ExceptionResponse ⇒ throw x.content
        case _ ⇒ throw new Exception("Unexpected result type")
      }
    }
  }
}
