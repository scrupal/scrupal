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
package scrupal.api

import spray.http.ContentType

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
  def payload: P

  /** Type Of Media Returned.
    *
    * This is a ContentType value from Spray. It indicates what kind of media and character encoding is being
    * returned by the payload.
    * @return A ContentType corresponding to the content type of `payload`
    */
  def contentType : ContentType
}

/** An Invokable Action
  *
  * Actions bring extensible behavior to Scrupal. An action object should be considered as a request to perform some
  * action in some context. To that end, you will note that an Action:
  *
  * - is a function that takes no arguments and returns a generic result (Result[_])
  *
  * - has a context that provides the invocation circumstances of the action
  *
  * - can be extended to include other information or behavior peculiar to a given type of action
  *
  * Action objects are how Scrupal represents an action taken from an external application. Actions are processed by
  * the ActionProcessor actor. Extensions of Action represent actual requests by adding parametric data to the Action
  * and implementing the `apply` function.

  */
trait Action extends (() => Result[_]) {
  /** The action part of an Action object.
    *
    * Objects mixing in this trait will define apply to implement the Action. Note that the result type is a generic
    * Result[_]. The only things you have to return are a Disposition, a ContentType and some sort of payload of
    * arbitrary type. Clients of the action should understand the actual type of result.
    * @return The Result[_] yielded from executing the action.
    */
  def apply() : Result[_]

  /** The context in which the Action is invoked.
    *
    * Every action executes within some context. The abstract Context class has many variants but a consistent interface
    * to allow actions to comprehend and manipulate the context in which they are executing.
    * @see [[Context]]
    * @return
    */
  def context : Context

}

