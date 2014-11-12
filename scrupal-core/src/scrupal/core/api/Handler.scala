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

import scala.collection.mutable

/** Abstract definition of an event handler for Scrupal
  * This just allows for ensuring that Handlers and Events are well matched
  * @param category The category of events this handler handles
  * @param name THe specific event kind that this handler handles
  */
abstract class Handler (
  val category : EventCategory.Type,
  val name : Symbol
) {
  def check(event: Event) {
    require(event.category == category, "Event category " + event.category + " for Handler requiring " + category)
    require(event.name == name, "Event " + event.name + " for handler requiring " + name)
  }
  lazy val label = name.name
}

/** Abstract definition of a Handler for a specific kind of event
  *
  * @param category The category of event to which this handler applies
  * @param name The name of the event this Handler handles
  * @tparam E kind of Event this handler is for
  */
abstract class HandlerFor[E <: Event](
  category : EventCategory.Type,
  name : Symbol
) extends Handler(category, name) {
  def handle(event : E) = {
    check(event)
  }
}

object Handler {

  private[scrupal] def apply(event: Event) : Option[Handler] = {
    handlers.get(event.category) match {
      case Some(x) => x.get(event.name) match {
        case Some(h) => Some(h)
        case _ => None
      }
      case _ => None
    }
  }

  private[api] def apply(handler: Handler) = {
    handlers.getOrElse(handler.category, {
      val newMap = new mutable.HashMap[Symbol,Handler]()
      handlers.put(handler.category, newMap );
      newMap
    }).put(handler.name, handler)
  }

  private val handlers = mutable.HashMap[EventCategory.Type,mutable.HashMap[Symbol,Handler]]()

}
