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

import org.apache.commons.lang3.exception.ExceptionUtils

import java.io.InputStream

import akka.http.scaladsl.model.{MediaType, MediaTypes}
import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import scrupal.utils.Validation.Failure

import scala.concurrent.ExecutionContext

/** Encapsulation of an Action's Result
  *
  * Results are generated from an action processing a request. They encapsulate a payload and a disposition. The
  * disposition provides a quick summary of the result while the payload provides access to the actual resulting
  * information.
  */
trait Response {
  /** Disposition of a Result
    *
    * This provides a basic summary of the result using the Disposition enumeration. There are several ways to be
    * successful in responding to a request and several ways to fail. Each of these basic ways of responding are
    * encoded into the disposition as a simple enumeration value. This allows the receiver of the Result[P] to
    * quickly asses what should be done with the result.
    * @return The Disposition of the result
    */
  def disposition : Disposition

  /** Type Of Media Returned.
    *
    * This is a ContentType value form Akka-Http. It indicates what kind of media and character encoding is being
    * returned by the payload.
    * @return A ContentType corresponding to the content type of `content`
    */
  def mediaType : MediaType

  /** Convert Content of to Enumerator
    *
    * This allows the content of type RT to be converted into a standardized type for serialization to a stream.
    * It converts the content into an Enumerator of Array[Byte].
    * @return The content as an Enumerated byte array
    */
  def toEnumerator(implicit ec: ExecutionContext) : Enumerator[Array[Byte]]

}

object NoopResponse extends Response {
  def disposition = Unimplemented
  def toEnumerator(implicit ec: ExecutionContext) = Enumerator.empty[Array[Byte]]
  def mediaType = MediaTypes.`application/octet-stream`
}

/** Result with an InputStream.
  *
  * This kind of Result contains an InputStream for its payload that the client of the StreamResult can use to read
  * data. This is often a more convenient result than EnumeratorResult because Enumerator.fromStream(x) can be used to
  * turn the stream into an Enumerator; or, the client can just read the stream directly (and block!).
  * @param content The InputStream to be read
  * @param mediaType The ContentType of the InputStream
  * @param disposition The disposition of the result.
  */
case class StreamResponse(
  content : InputStream,
  mediaType : MediaType,
  disposition : Disposition = Successful) extends Response {
  def toEnumerator(implicit ec: ExecutionContext) = Enumerator.fromStream(content, 64 * 1024)
}

/** Result with an Array of Bytes.
  *
  * This kind of Result contains an array of data that the client of the OctetsResult can use.
  *
  * @param content The data of the result
  * @param mediaType The ContentType of the data
  * @param disposition The disposition of the result.
  */
case class OctetsResponse(
  content : Array[Byte],
  mediaType : MediaType,
  disposition : Disposition = Successful) extends Response {
  def toEnumerator(implicit ec: ExecutionContext) = Enumerator(content)
}

/** Result with a simple text string.
  *
  * This kind of result just encapsulates a String and defaults its ContentType to text/plain(UTF-8). That ContentType
  * should not be changed unless there is a significant need to as using UTF-8 as the base character encoding is
  * standard across Scrupal
  *
  * @param content The string content of the response
  * @param disposition The disposition of the result.
  */
case class StringResponse(
  content : String,
  disposition : Disposition = Successful) extends Response {
  val mediaType : MediaType = MediaTypes.`text/plain`
  def toEnumerator(implicit ec: ExecutionContext) = Enumerator(content.getBytes(utf8))
}

/** Result with an HTMLFormat payload.
  *
  * This kind of result just encapsulates a Scalatags Html result and defaults its ContentType to text/html.
  *
  * @param content The Html payload of the result.
  * @param disposition The disposition of the result.
  */
case class HtmlResponse(
  content : String,
  disposition : Disposition = Successful) extends Response {
  val mediaType : MediaType = MediaTypes.`text/html`
  def toEnumerator(implicit ec: ExecutionContext) = Enumerator(content.getBytes(utf8))
}


/** Result with a BSONDocument payload.
  *
  * This kind of result just encapsulates a MongoDB BSONDocument result. Note that the ContentType is not modifiable
  * here as it is hard coded to ScrupalMediaTypes.bson. BSON is not a "standard" media type so we invent our own.
  *
  * @param content The JsValue payload of the result.
  * @param disposition The disposition of the result.
  */
case class JsonResponse(
  content : JsValue,
  disposition : Disposition = Successful) extends Response {
  val mediaType : MediaType = MediaTypes.`application/json`
  def toEnumerator(implicit ec: ExecutionContext) = {
    Enumerator(Json.stringify(content).getBytes(utf8))
  }
}

/** Result with an Throwable payload.
  *
  * This kind of result just encapsulates an error embodied by a Throwable. This is how hard errors are returned from
  * a result. Note that the Disposition is always an Exception
  *
  * @param content The error that occurred
  */
case class ExceptionResponse(
  content : Throwable) extends Response {
  val disposition : Disposition = Exception
  val mediaType : MediaType = MediaTypes.`text/plain`
  def toText : String = {
    val bldr = new StringBuilder()
    bldr.append(ExceptionUtils.getMessage(content)).append(":\n")
    bldr.append(ExceptionUtils.getStackTrace(content)).append("caused by: ")
    bldr.append(ExceptionUtils.getRootCauseMessage(content)).append(":\n")
    bldr.append("\tat ").append(ExceptionUtils.getRootCauseStackTrace(content).mkString("\n\tat "))
    bldr.toString()
  }
  def toEnumerator(implicit ec: ExecutionContext) = {
    Enumerator(toText.getBytes(utf8))
  }
}

case class JsonExceptionResponse(content : Throwable) extends Response {
  val disposition : Disposition = Exception
  val mediaType = MediaTypes.`application/json`
  def toJson : JsObject = {
    JsObject(Seq(
      "$error" → JsString(ExceptionUtils.getMessage(content)),
      "$stack" → JsArray(content.getStackTrace.map { elem ⇒ JsString(elem.toString) })
    ))
  }

  def toJsonResult : JsonResponse = {
    JsonResponse(toJson, disposition)
  }

  def toEnumerator(implicit ec: ExecutionContext) = {
    Enumerator(Json.stringify(toJson).getBytes(utf8))
  }
}

/** Result with a simple error payload.
  *
  * This can be used when an error is detected that does not warrant an exception being thrown. Instead, just return
  * the ErrorResult. Note that Disposition is "Unspecified" but this is unlikely what you want so you should always
  * set the Disposition with an ErrorResult.
  * @param content The error message
  * @param disposition The disposition of the result
  */
case class ErrorResponse(
  content : String,
  disposition : Disposition = Unspecified) extends Response {
  val mediaType = MediaTypes.`text/plain`
  def formatted = s"Error: ${disposition.id.name}: $content"
  def toEnumerator(implicit ec: ExecutionContext) = {
    Enumerator(formatted.getBytes(utf8))
  }
}

case class FormErrorResponse(
  content : Failure[JsValue],
  disposition : Disposition = Unacceptable) extends Response {
  val mediaType : MediaType = MediaTypes.`application/json`
  def toEnumerator(implicit ec: ExecutionContext) = {
    Enumerator(Json.stringify(content.jsonMessage).getBytes(utf8))
  }
  def formatted : String = content.msgBldr.toString()
}
