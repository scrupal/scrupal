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

package scrupal.api

import scala.collection.mutable

/** Abstract definition of an event handler for Scrupal
  * This just allows for ensuring that Handlers and Events are well matched
  * @param category The category of events this handler handles
  * @param name THe specific event kind that this handler handles
  */
abstract class Handler(
  val category : EventCategory.Type,
  val name : Symbol) {
  def check(event : Event) {
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
  name : Symbol) extends Handler(category, name) {
  def handle(event : E) = {
    check(event)
  }
}

object Handler {

  private[scrupal] def apply(event : Event) : Option[Handler] = {
    handlers.get(event.category) match {
      case Some(x) ⇒ x.get(event.name) match {
        case Some(h) ⇒ Some(h)
        case _ ⇒ None
      }
      case _ ⇒ None
    }
  }

  private[api] def apply(handler : Handler) = {
    handlers.getOrElse(handler.category, {
      val newMap = new mutable.HashMap[Symbol, Handler]()
      handlers.put(handler.category, newMap);
      newMap
    }).put(handler.name, handler)
  }

  private val handlers = mutable.HashMap[EventCategory.Type, mutable.HashMap[Symbol, Handler]]()

}
