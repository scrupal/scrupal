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

package scrupal.core

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import akka.actor.ActorSystem
import akka.util.Timeout
import scrupal.utils.ScrupalComponent

/** Core Actors Package
  *
  * All processing in Scrupal is asynchronous and non-blocking, right down to the database. Consequently,
  * getting anything done involves using Akka actors. In particular this module contains all the logic for processing
  * requests which can happen with a high degree of concurrency.
 */
package object actors extends ScrupalComponent {


  /** Default Timeout
    * Code in this package is free to use other time out values but this is the default. We chose 8 seconds because
    * in the web world, that's the attention span of a user. Either respond in 8 seconds, or lose their attention.
    */
  implicit val timeout : Timeout = Timeout(8000,TimeUnit.MILLISECONDS)

  /** A counter for the number of actors. This is to ensure we make unique names */
  val actorCounter = new AtomicLong(0)

  /** A method to generate a unique name for each actor that meets Akka's naming requirements.
    *
    * @param name The name of the type of the actor
    * @return The unique name generated for the actor instance
    */
  def actorName(name: String) : String = {
    val result = name.replace(" ", "-")+ "-" + actorCounter.incrementAndGet()
    log.debug("Created new actor: " + name)
    result
  }

}
