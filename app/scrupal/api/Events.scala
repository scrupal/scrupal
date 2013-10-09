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

package scrupal.api

import play.api.mvc.RequestHeader


/** The categories of events by their applicability */
object EventCategory extends Enumeration {
  type Type = Value
  val Scrupal = Value // Events related to Scrupal as a whole
  val Request = Value // Events related requests coming into Scrupal
  val Entity = Value // Events related to entities
  val Module = Value // Events related to modules
}

/** Abstract notion of an event */
abstract class Event (
  val category : EventCategory.Type,
  val name : Symbol
)

// Scrupal general events below here
/** Event definition for scrupal startup */
case class Scrupal_Start() extends Event(EventCategory.Scrupal, 'Start)
case class Scrupal_Stop() extends Event(EventCategory.Scrupal, 'Stop)

// Request related events below here
abstract class RequestEvent(
  val request: RequestHeader,
  category : EventCategory.Type,
  name : Symbol
) extends Event(category, name)

/** An event that occurs when an error (exception, i.e. HTTP 500) is thrown during request processing */
case class Request_Error(override val request: RequestHeader)
  extends RequestEvent(request, EventCategory.Request, 'Error)

/** An event that occurs when a handler is not found for a request */
case class Request_NotFound(override val request: RequestHeader)
  extends RequestEvent(request, EventCategory.Request, 'NotFound)

/** An event that occurs when a bad request is recognized */
case class Request_BadRequest(override val request: RequestHeader)
  extends RequestEvent(request, EventCategory.Request, 'BadRequest)

/** An event that occurs after successfully handling a request */
case class Request_Handled(override val request: RequestHeader)
  extends RequestEvent(request, EventCategory.Request, 'Handled)

abstract class HandlerFor[E <: Event]{
  def handle(event : E)
}

/** An enumeration of the events in which a Module can register interest */
object Events extends Enumeration {
  type Type = Value
  val Scrupal_Start = Value // When Scrupal initializes
  val Scrupal_Stop = Value
  val Cache_Lookup = Value
  val Entity_Insert = Value
  val Entity_Update = Value
  val Entity_Delete = Value
  val Module_Enable = Value
  val Module_Disable = Value
}
