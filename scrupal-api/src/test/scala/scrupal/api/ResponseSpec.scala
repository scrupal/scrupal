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


import akka.http.scaladsl.model.{MediaTypes, MediaType}
import play.api.libs.iteratee.Enumerator
import scrupal.test.ScrupalSpecification

import scala.concurrent.ExecutionContext

class ResponseSpec extends ScrupalSpecification("Response") {

  "Response" should {
    "have payload, mediaType and disposition" in {
      val response = new Response {
        def disposition: Disposition = Successful
        def mediaType: MediaType = MediaTypes.`application/octet-stream`
        def content : Array[Byte] = Array.empty[Byte]
        def toEnumerator(implicit ec: ExecutionContext) = Enumerator.empty[Array[Byte]]
      }
      success
    }
  }
  "NoopResponse" should {
    "be unimplemented" in {
      NoopResponse.disposition must beEqualTo(Unimplemented)
    }
    "have application/octet-stream media type" in {
      NoopResponse.mediaType must beEqualTo(MediaTypes.`application/octet-stream`)
    }
  }

  "StreamResponse" should {
    "have some tests" in { pending }
  }

  "OctetsResponse" should {
    "have some tests" in { pending }
  }

  "StringResponse" should {
    "have some tests" in { pending }
  }

  "HtmlResponse" should {
    "have some tests" in { pending }
  }

  "JsonResponse" should {
    "have some tests" in { pending }
  }

  "ExceptionResponse" should {
    "have some tests" in { pending }
  }

  "ErrorResponse" should {
    "have some tests" in { pending }
  }

  "FormErrorResponse" should {
    "have some tests" in { pending }
  }
}
