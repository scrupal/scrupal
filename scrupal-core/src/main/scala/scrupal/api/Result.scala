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

import akka.http.scaladsl.model.{ MediaType, MediaTypes }
import java.io.InputStream

import play.api.libs.iteratee.Enumerator
import play.api.libs.json._

import scrupal.api.Html.TagContent
import scrupal.utils.Validation.Failure

trait Resolvable extends (() ⇒ EnumeratorResult)

/** Encapsulation of an Action's Result
  *
  * Results are generated from an action processing a request. They encapsulate a payload and a disposition. The
  * disposition provides a quick summary of the result while the payload provides access to the actual resulting
  * information.
  * @tparam P The type of the resulting payload
  */
trait Result[P] extends Resolvable {
  /** Disposition of a Result
    *
    * This provides a basic summary of the result using the Disposition enumeration. There are several ways to be
    * successful in responding to a request and several ways to fail. Each of these basic ways of responding are
    * encoded into the disposition as a simple enumeration value. This allows the receiver of the Result[P] to
    * quickly asses what should be done with the result.
    * @return The Disposition of the result
    */
  def disposition : Disposition

  /** Payload Content of The Result.
    *
    * This is the actual result. It can be any Scala type but should correspond to the ContentType
    * @return
    */
  def payload : P

  /** Type Of Media Returned.
    *
    * This is a ContentType value from Spray. It indicates what kind of media and character encoding is being
    * returned by the payload.
    * @return A ContentType corresponding to the content type of `payload`
    */
  def mediaType : MediaType
}

trait ContainedResult[P] extends Result[P] {
  def body : Array[Byte]
  def apply() : EnumeratorResult = EnumeratorResult(Enumerator(body), mediaType, disposition)
}

/** Result with a data Enumerator.
  *
  * This kind of Result contains an Enumerator[Array[Byte]] for its payload. This allows clients of an action to receive
  * a result that can be used to asynchronously stream chunks of data as needed/pulled by the client.
  * @param payload The Enumerator of data
  * @param mediaType The ContentType of the `payload`
  * @param disposition The disposition of the result
  */
case class EnumeratorResult(
  payload : Enumerator[Array[Byte]],
  mediaType : MediaType,
  disposition : Disposition = Successful) extends Result[Enumerator[Array[Byte]]] {
  def apply() : EnumeratorResult = this
}

/** Result with an InputStream.
  *
  * This kind of Result contains an InputStream for its payload that the client of the StreamResult can use to read
  * data. This is often a more convenient result than EnumeratorResult because Enumerator.fromStream(x) can be used to
  * turn the stream into an Enumerator; or, the client can just read the stream directly (and block!).
  * @param payload The InputStream to be read
  * @param mediaType The ContentType of the InputStream
  * @param disposition The disposition of the result.
  */
case class StreamResult(
  payload : InputStream,
  mediaType : MediaType,
  disposition : Disposition = Successful) extends Result[InputStream] {
  def apply() : EnumeratorResult = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val enum = Enumerator.fromStream(payload, 64 * 1024) // ISSUE: What's the right size for the chunks?
    EnumeratorResult(enum, mediaType, disposition)
  }
}

/** Result with an Array of Bytes.
  *
  * This kind of Result contains an array of data that the client of the OctetsResult can use.
  *
  * @param payload The data of the result
  * @param mediaType The ContentType of the data
  * @param disposition The disposition of the result.
  */
case class OctetsResult(
  payload : Array[Byte],
  mediaType : MediaType,
  disposition : Disposition = Successful) extends ContainedResult[Array[Byte]] {
  val body = payload
}

/** Result with a simple text string.
  *
  * This kind of result just encapsulates a String and defaults its ContentType to text/plain(UTF-8). That ContentType
  * should not be changed unless there is a significant need to as using UTF-8 as the base character encoding is
  * standard across Scrupal
  *
  * @param payload The data of the result
  * @param disposition The disposition of the result.
  */
case class StringResult(
  payload : String,
  disposition : Disposition = Successful) extends ContainedResult[String] {
  val mediaType : MediaType = MediaTypes.`text/plain`
  val body = payload.getBytes(utf8)
}

/** Result with an HTMLFormat payload.
  *
  * This kind of result just encapsulates a Scalatags Html result and defaults its ContentType to text/html.
  *
  * @param payload The Html payload of the result.
  * @param disposition The disposition of the result.
  */
case class HtmlResult(
  payload : String,
  disposition : Disposition = Successful) extends ContainedResult[String] {
  val mediaType : MediaType = MediaTypes.`text/html`
  val body = payload.getBytes(utf8)
}

object HtmlResult {
  def apply(tag : TagContent, disposition : Disposition) = new HtmlResult(tag.toString(), disposition)
  def apply(contents : Html.Contents, disposition : Disposition) = {
    new HtmlResult(Html.renderContents(contents), disposition)
  }
}

/** Result with a BSONDocument payload.
  *
  * This kind of result just encapsulates a MongoDB BSONDocument result. Note that the ContentType is not modifiable
  * here as it is hard coded to ScrupalMediaTypes.bson. BSON is not a "standard" media type so we invent our own.
  *
  * @param payload The Html payload of the result.
  * @param disposition The disposition of the result.
  */
case class JsonResult(
  payload : JsObject,
  disposition : Disposition = Successful) extends Result[JsObject] {
  val mediaType : MediaType = MediaTypes.`application/json`
  def apply() : EnumeratorResult = {
    val buffer = Json.stringify(payload)
    EnumeratorResult(Enumerator(buffer.getBytes(utf8)), mediaType, disposition)
  }
}

/** Result with an Throwable payload.
  *
  * This kind of result just encapsulates an error embodied by a Throwable. This is how hard errors are returned from
  * a result. Note that the Disposition is always an Exception
  *
  * @param payload The error that occurred
  */
case class ExceptionResult(payload : Throwable) extends ContainedResult[Throwable] {
  val disposition : Disposition = Exception
  val mediaType = MediaTypes.`text/plain`
  def toJson : JsObject = {
    JsObject(Seq(
      "$error" → JsString(s"${payload.getClass.getName}: ${payload.getMessage}"),
      "$stack" → JsArray(payload.getStackTrace.map { elem ⇒ JsString(elem.toString) })
    ))
  }

  def toJsonResult : JsonResult = {
    JsonResult(toJson, disposition)
  }

  def body = Json.stringify(toJson).getBytes(utf8)

}

/** Result with a simple error payload.
  *
  * This can be used when an error is detected that does not warrant an exception being thrown. Instead, just return
  * the ErrorResult. Note that Disposition is "Unspecified" but this is unlikely what you want so you should always
  * set the Disposition with an ErrorResult.
  * @param payload The error message
  * @param disposition The disposition of the result
  */
case class ErrorResult(
  payload : String,
  disposition : Disposition = Unspecified) extends ContainedResult[String] {
  val mediaType = MediaTypes.`text/plain`
  def formatted = s"Error: ${disposition.id.name}: $payload"
  def body = formatted.getBytes(utf8)
}

case class FormErrorResult(
  payload : Failure[JsValue],
  disposition : Disposition = Unacceptable) extends Result[Failure[JsValue]] {
  val mediaType : MediaType = MediaTypes.`application/json`
  def apply() : EnumeratorResult = {
    val buffer = Json.stringify(payload.jsonMessage)
    EnumeratorResult(Enumerator(buffer.getBytes(utf8)), mediaType, disposition)
  }
  def formatted : String = payload.msgBldr.toString()
}
