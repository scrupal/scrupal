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

