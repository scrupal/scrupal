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

package scrupal.utils

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ Await, Future }
import scala.util.{ Failure, Success, Try }

/** Utilities used throughout Scrupla. Should probably be an aspect, but we just mix this in as needed.
  * Helps with logging, throwing exceptions and dealing with futures.
  */
trait ScrupalComponent {

  def logger_identity : String = this.getClass.getName

  val log = grizzled.slf4j.Logger({
    if (logger_identity == null) "scrupal.utils.NoCanonicalName" else logger_identity }
  )

  /** Identity Aware Exception Toss
    * This function makes it easier to throw (toss) an exception that adds a message to it and also identifies the
    * tosser that threw it. This helps track down where in the code the message was thrown from.
    * @param msg - Message to add to the exception
    * @param cause - The root cause exception
    */
  def toss(msg : ⇒ String, cause : Throwable = null) = throw ScrupalException(this, msg, cause)
  def toss(msg : ⇒ String, cause : Option[Throwable]) =
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
  def await[X](future : ⇒ Future[X], duration : FiniteDuration, opName : String) : Try[X] = Try {
    Await.result(future, duration)
  } match {
    case Success(x) ⇒ Success(x)
    case Failure(x) ⇒
      Failure(ScrupalException(this, "Exception while waiting " + duration.toSeconds + " seconds for operation '" +
        opName + "' to complete:", x))
  }

}

case class ScrupalException(component : ScrupalComponent, message : String, cause : Throwable)
  extends java.lang.Exception(message, cause) {
  def this(comp : ScrupalComponent, message : String) = { this(comp, message, null) }
  override def getMessage = {
    s"${super.getMessage} (@${component.logger_identity})"
  }
  override def getStackTrace : Array[StackTraceElement] = {
    val st = super.getStackTrace
    st.drop(1)
  }
}
