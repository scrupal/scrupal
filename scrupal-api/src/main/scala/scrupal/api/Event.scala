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

/** The categories of events by their applicability */
object EventCategory extends Enumeration {
  type Type = Value
  val Scrupal = Value // Events related to Scrupal as a whole
  val Entity = Value // Events related to entities
  val Module = Value // Events related to modules
}

/** Abstract notion of an event */
abstract class Event(
  val category : EventCategory.Type,
  val name : Symbol)

// Scrupal general events below here
/** Event definition for scrupal startup */
case class Scrupal_Start() extends Event(EventCategory.Scrupal, 'Start)
case class Scrupal_Stop() extends Event(EventCategory.Scrupal, 'Stop)

