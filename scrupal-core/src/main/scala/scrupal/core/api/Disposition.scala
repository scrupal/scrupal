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

import scrupal.utils.{Registrable, Registry}
import spray.http.ContentType

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
  def isSuccessful : Boolean = code > 0
  def isFailure : Boolean = code < 0
}

// Indeterminate Dispositions have value 0 and should only be used rarely to indicate null or impossibility.
object Indeterminate extends Disposition(0, 'Indeterminate, "Disposition of the request is unknown.")

// Successful Dispositions are in the positive range
object Successful     extends Disposition(1, 'Successful, "Action was successfully processed synchronously.")
object Received       extends Disposition(2, 'Received, "Action is processing asynchronously without response.")
object Pending        extends Disposition(3, 'Pending, "Action is pending asynchronous processing without response.")
object Promise        extends Disposition(4, 'Promise,
                              "Action is pending asynchronous processing with a promise of future result.")

// Unsuccessful Dispositions are in the negative range
object Unspecified    extends Disposition( -1, 'Unspecified,
                              "Action processing yielded an error of unspecified nature.")
object TimedOut       extends Disposition( -2, 'TimedOut, "Action processing attempted but it timed out.")
object Unintelligible extends Disposition( -3, 'Unintelligible, "Action rejected because it could not be understood.")
object Unimplemented  extends Disposition( -4, 'Unimplemented, "Action rejected because its has not been implemented yet.")
object Unsupported    extends Disposition( -5, 'Unsupported,
                              "Action rejected because its resource is no longer supported.")
object Unauthorized   extends Disposition( -6, 'Unauthorized,
                              "Action rejected because requester is not authorized for it.")
object Unavailable    extends Disposition( -7, 'Unavailable,
                              "Action rejected because the resource is not currently available.")
object NotFound       extends Disposition( -8, 'NotFound, "Action rejected because the resource was not found.")
object Ambiguous      extends Disposition( -9, 'Ambiguous,
                              "Action rejected because of ambiguity on the resource requested.")
object Conflict       extends Disposition(-10, 'Conflict,
                              "Action rejected because it would cause a conflict between resources.")
object TooComplex     extends Disposition(-11, 'TooComplex,
                              "Action rejected because it implies processing that is too complex.")
object Exhausted      extends Disposition(-12, 'Exhausted,
                              "Action processing started but a computing resource became exhausted")
object Exception      extends Disposition(-13, 'Exception,
                              "An exception occurred during the processing of the action")
object Unacceptable   extends Disposition(-14, 'Unacceptable,
                              "The content of the request could not be accepted")

object Disposition extends Registry[Disposition] {
  val registryName = "Dispositions"
  val registrantsName = "disposition"
}
