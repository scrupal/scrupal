/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/
package scrupal.core.api

import play.twirl.api.Html
import reactivemongo.bson.{BSONArray, BSONString, BSONDocument}
import spray.http.{MediaTypes, MediaType}

/** Encapsulation Of An
  *
  * Requests are generated from a request receiver such as scrupal-http. They encapsulate an entity, an instance of
  * that entity, and the invocation arguments. This request information is passed to an Action when it is invoked.
  */
trait Request {
  def context : Context
}

/** Encapsulation of an Action's Result
  *
  * Results are generated from an action processing a request. They encapsulate a payload and a disposition. The
  * disposition provides a quick summary of the result while the payload provides access to the actual resulting
  * information.
  * @tparam P The type of the resulting payload
  */
trait Result[P] {
  /** Disposition of a Result
    *
    * This provides a basic summary of the result usign the Disposition enumeration. There are several ways to be
    * successful in responding to a request and several ways to fail. Each of these basic ways of responding are
    * encoded into the disposition as a simple enumeration value. This allows the receiver of the Result[P] to
    * quickly asses what should be done with the result.
    * @return The Disposition of the result
    */
  def disposition : Disposition

  /** Payload Content of The Result
    * The is the actual result. It can be an Scala type but should correspond to the MediaType
    * @return
    */
  def payload: P

  /** Type Of Media Returned.
    *
    * This is a MIME media type value from Spray. It indicates what kind of media is being returned by the payload.
    * @return
    */
  def mediaType : MediaType
}

case class OctetsResult(
  payload: Array[Byte],
  mediaType: MediaType,
  disposition: Disposition = Successful
) extends Result[Array[Byte]]

case class TextResult(
  payload: String,
  disposition: Disposition = Successful
) extends Result[String] {
  val mediaType = MediaTypes.`text/plain`
}

case class HtmlResult(
  payload: Html,
  disposition: Disposition = Successful
) extends Result[Html] {
  val mediaType = MediaTypes.`text/html`
}

case class BSONResult(
  payload: BSONDocument,
  disposition: Disposition = Successful
) extends Result[BSONDocument] {
  val mediaType = MediaType.custom("application", "vnd.bson", compressible=true, binary=true, fileExtensions=Seq
    ("bson"))
}

case class ExceptionResult(
  error: Throwable,
  disposition: Disposition = Exception
) extends Result[BSONDocument] {
  val mediaType = MediaTypes.`text/plain`

  val payload = {
    val stack = error.getStackTrace.map { elem ⇒ BSONString(elem.toString) }
    BSONDocument(
      "$error" →BSONString(s"${error.getClass.getName}: ${error.getMessage}"),
      "$stack" → BSONArray(stack)
    )
  }
}
// case class JSONResult(payload: JsObject, disposition: Disposition = Successful) extends Result[JsObject]

/** An Invokable Action
  *
  * Actions bring behavior to entities. Each entity can declare a set of actions that its users can invoke from the
  * REST api. Unlike the methods provided by the [[Entity]] class, these Actions operate on the Instances of the
  * entity themselves. The Entity methods are more like operations on the collection of Entities.
  */
abstract class Action extends (() => Result[_]) with Request {
  /** Objects mixing in this trait will define apply to implement the Action.
    * Note that the result type is fixed to return a BSONDocument because Methods are only invokable from the REST api
    * which requires results to be in the form of a BSONDocument
    * @return The Play Action that results in a JsObject to send to the client
    */
  def apply() : Result[_]
}
