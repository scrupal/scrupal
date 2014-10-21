/**********************************************************************************************************************
  * This file is part of Scrupal a Web Application Framework.                                                          *
  *                                                                                                                    *
  * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

/** A method of providing an Action for an Entity
  * Methods bring behavior to entities. Each entity can declare a set of methods that its users can invoke from the
  * REST api, in addition to the standard REST api methods.
  */
trait Method {
  /** Objects mixing in this trait will define apply to implement the Action.
    * Note that the result type is fixed to return a JsObject because Methods are only invokable from the REST api
    * which requires results to be in the form of a JsObject
    * @return The Play Action that results in a JsObject to send to the client
    */
  def apply : String // Action[JsObject]
}
