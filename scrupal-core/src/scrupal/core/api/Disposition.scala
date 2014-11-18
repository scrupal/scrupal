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

import scrupal.utils.{Registry, Registrable}
import spray.http.MediaType

/** Disposition Of An Action Result
  * Every Result produced by an Action contains a Disposition which indicates to the requester of the action in a
  * very concise way how the action was handled. In general, positive valued dispositions are successful while negative
  * valued dispositions are unsuccessful. Dispositions are named and have a default message that can be used in a
  * response to the requester.
  */

case class Disposition(
  code: Int,
  id: Symbol,
  msg: String
) extends Registrable[Disposition] {
  def registry = Disposition
  def asT = this
  def apply[T]( result: T, mt: MediaType) : Result[T] = {
    val disp = this
    new Result[T] { val disposition = disp; val payload = result; val mediaType = mt }
  }
  def isSuccessful : Boolean = code > 0
  def isFailure : Boolean = code < 0
}

// Indeterminate Dispositions have value 0 and should only be used rarely to indicate null or impossibility.
object Indeterminate extends Disposition(0, 'Indeterminate, "Disposition of the request is unknown.")

// Successful Dispositions are in the positive range
object Successful     extends Disposition(1, 'Responding, "Request was successfully processed synchronously.")
object Received       extends Disposition(2, 'Received, "Request received and is processing asynchronously without response.")
object Pending        extends Disposition(3, 'Pending, "Request received and is pending asynchronous processing without response.")
object Promise        extends Disposition(4, 'Promise, "Request received and an asynchronous response is promised in the future.")

// Unsuccessful Dispositions are in the negative range
object TimedOut       extends Disposition( -1, 'TimedOut, "Request processing attempted but it timed out.")
object Unintelligible extends Disposition( -2, 'Unintelligible, "Request rejected because it could not be understood.")
object Unimplemented  extends Disposition( -3, 'Unimplemented, "Request rejected because its has not been implemented yet.")
object Unsupported    extends Disposition( -4, 'Unsupported, "Request rejected because its resource is no longer supported.")
object Unauthorized   extends Disposition( -5, 'Unauthorized, "Request rejected because requester is not authorized for it.")
object Unavailable    extends Disposition( -6, 'Unavailable, "Request rejected because the resource is not currently available.")
object NotFound       extends Disposition( -7, 'NotFound, "Request rejected because the resource was not found.")
object Ambiguous      extends Disposition( -8, 'Ambiguous, "Request rejected because of ambiguity on the resource requested.")
object Conflict       extends Disposition( -9, 'Conflict, "Request rejected because it would cause a conflict between resources.")
object TooComplex     extends Disposition(-10, 'TooComplex, "Request rejected because it implies processing ")
object Exhausted      extends Disposition(-11, 'Exhausted, "Request processing started but a computing resource became exhausted")
object Exception      extends Disposition(-13, 'Exception, "An exception occurred during the processing of the request")

object Disposition extends Registry[Disposition] {
  val registryName = "Dispositions"
  val registrantsName = "disposition"
}
