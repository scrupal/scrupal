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

/** The categories of events by their applicability */
object EventCategory extends Enumeration {
  type Type = Value
  val Scrupal = Value // Events related to Scrupal as a whole
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

