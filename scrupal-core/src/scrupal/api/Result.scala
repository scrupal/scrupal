/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
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

package scrupal.api

import java.io.InputStream

import play.api.libs.iteratee.Enumerator
import play.twirl.api.{Html, Txt}
import reactivemongo.bson.{BSONArray, BSONDocument, BSONString}
import spray.http.{ContentType, ContentTypes, MediaType, MediaTypes}

/** Result with a data Enumerator.
  *
  * This kind of Result contains an Enumerator[Array[Byte]] for its payload. This allows clients of an action to receive
  * a result that can be used to asynchronously stream chunks of data as needed/pulled by the client.
  * @param payload The Enumerator of data
  * @param contentType The ContentType of the `payload`
  * @param disposition The disposition of the result
  */
case class EnumeratorResult(
  payload: Enumerator[Array[Byte]],
  contentType: ContentType,
  disposition: Disposition = Successful
) extends Result[Enumerator[Array[Byte]]]

/** Result with an InputStream.
  *
  * This kind of Result contains an InputStream for its payload that the client of the StreamResult can use to read
  * data. This is often a more convenient result than EnumeratorResult because Enumerator.fromStream(x) can be used to
  * turn the stream into an Enumerator; or, the client can just read the stream directly (and block!).
  * @param payload The InputStream to be read
  * @param contentType The ContentType of the InputStream
  * @param disposition The disposition of the result.
  */
case class StreamResult(
  payload: InputStream,
  contentType: ContentType,
  disposition: Disposition = Successful
) extends Result[InputStream]

/** Result with an Array of Bytes.
  *
  * This kind of Result contains an array of data that the client of the OctetsResult can use.
  *
  * @param payload The data of the result
  * @param contentType The ContentType of the data
  * @param disposition The disposition of the result.
  */
case class OctetsResult(
  payload: Array[Byte],
  contentType: ContentType,
  disposition: Disposition = Successful
) extends Result[Array[Byte]]

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
  payload: String,
  disposition: Disposition = Successful
) extends Result[String] {
  val contentType: ContentType = ContentTypes.`text/plain(UTF-8)`
}

/** Result with a Twirl TxtFormat paylaod.
  *
  * This kind of result just encapsulates a Twirl Txt and defaults its ContentType to text/plain(UTF-8).
  *
  * @param payload The data of the result
  * @param disposition The disposition of the result.
  */
case class TxtResult(
  payload: Txt,
  disposition: Disposition = Successful
) extends Result[Txt] {
  val contentType: ContentType = ContentTypes.`text/plain(UTF-8)`
}

/** Result with an HTMLFormat payload.
  *
  * This kind of result just encapsulates a Twirl Html result and defaults its ContentType to text/html.
  *
  * @param payload The Html payload of the result.
  * @param disposition The disposition of the result.
  */
case class HtmlResult(
  payload: Html,
  disposition: Disposition = Successful
) extends Result[Html] {
  val contentType: ContentType = MediaTypes.`text/html`
}

/** Result with a BSONDocument payload.
  *
  * This kind of result just encapsulates a MongoDB BSONDocument result. Note that the ContentType is not modifiable
  * here as it is hard coded to ScrupalMediaTypes.bson. BSON is not a "standard" media type so we invent our own.
  *
  * @param payload The Html payload of the result.
  * @param disposition The disposition of the result.
  */
case class BSONResult(
  payload: BSONDocument,
  disposition: Disposition = Successful
) extends Result[BSONDocument] {
  val contentType : ContentType = ScrupalMediaTypes.bson
}

/** Result with an Throwable payload.
  *
  * This kind of result just encapsulates an error embodied by a Throwable. This is how hard errors are returned from
  * a result. Note that the Disposition is always an Exception
  *
  * @param payload The error that occurred
 */
case class ExceptionResult(
  payload: Throwable
) extends Result[Throwable] {
  val disposition: Disposition = Exception
  val contentType = ContentTypes.`text/plain(UTF-8)`

  def toBSONResult : BSONResult = {
    val stack = payload.getStackTrace.map { elem ⇒ BSONString(elem.toString) }
    BSONResult(
      BSONDocument(
        "$error" →BSONString(s"${payload.getClass.getName}: ${payload.getMessage}"),
        "$stack" → BSONArray(stack)
      ),
      disposition
    )
  }

  def toHtmlResult : HtmlResult = {
    HtmlResult(scrupal.api.views.html.errors.ExceptionResult(this), disposition)
 }

  def toTxtResult : TxtResult = {
    TxtResult(scrupal.api.views.txt.errors.ExceptionResult(this), disposition)
  }

  def toStringResult : StringResult = {
    StringResult(scrupal.api.views.txt.errors.ExceptionResult(this).body, disposition)
  }
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
  payload: String,
  disposition : Disposition = Unspecified
) extends Result[String] {
  val contentType = ContentTypes.`text/plain(UTF-8)`

  def formatted = s"Error: ${disposition.id.name}: ${payload}"

}

// TODO: Support more Result types: JSON
// case class JSONResult(payload: JsObject, disposition: Disposition = Successful) extends Result[JsObject]

object ScrupalMediaTypes {
  val bson = MediaType.custom("application", "vnd.bson", compressible=true, binary=true,
    fileExtensions=Seq("bson"))
}
