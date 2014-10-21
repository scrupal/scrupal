/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2014, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

package scrupal.utils

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import grizzled.slf4j.Logger

/** Utilities used throughout Scrupla. Should probably be an aspect, but we just mix this in as needed.
  * Helps with logging, throwing exceptions and dealing with futures.
 */
trait ScrupalComponent {

  def logger_identity: String = this.getClass.getCanonicalName

  val log = Logger({ if (logger_identity == null) "scrupal.utils.NoCanonicalName" else logger_identity })

  /** Identity Aware Exception Toss
    * This function makes it easier to throw (toss) an exception that adds a message to it and also identifies the
    * tosser that threw it. This helps track down where in the code the message was thrown from.
    * @param msg - Message to add to the exception
    * @param cause - The root cause exception
    */
  def toss(msg: => String, cause: Throwable = null) = throw ScrupalException(this, msg, cause)
  def toss(msg: => String, cause: Option[Throwable]) =
    throw ScrupalException(this, msg, cause.orNull)

  /** Handle Exceptions From Asynchronous Results
    * This awaits a future operation that returns HTML and converts any exception that arises during the
    * future operation or waiting into HTML as well. So, this is guaranteed to never let an exception escape and to
    * always return Html.
    * @param future The Future[Html] to wait for
    * @param duration How long to wait, at most
    * @param opName The name of the operation being waited for, to assist with error messages
    * @return A blob of Html containing either the intended result or an error message.
    */
  def await[X](future: => Future[X], duration: FiniteDuration, opName: String) : Try[X] = Try {
    Await.result(future, duration)
  } match {
    case Success(x) => Success(x)
    case Failure(x) =>
      Failure(ScrupalException(this, "Exception while waiting " + duration.toSeconds + " seconds for operation '" +
        opName + "' to complete:", x))
  }


}

case class ScrupalException(component: ScrupalComponent, message: String, cause: Throwable )
  extends java.lang.Exception(message, cause)
{
  def this(comp: ScrupalComponent, message: String) = { this(comp, message, null) }
  override def getMessage = {
    super.getMessage + " in " + component.logger_identity
  }
}
