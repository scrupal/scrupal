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

package scrupal.core.actors

import akka.actor.SupervisorStrategy.Decider
import akka.actor._
import akka.routing.{DefaultResizer, SmallestMailboxPool}
import scrupal.core.api.{EntityCommand, ExceptionResult, Entity}
import scrupal.utils.ScrupalComponent

import scala.concurrent.duration.Duration

object EntityProcessor {

  /** Name of the configuration key for the configuration of the dispatcher */
  val dispatcher_config_name = "scrupal.dispatcher"


  /** Make The Dispatcher ActorRef
    *
    * This is a router behind a cadre of EntityProcessor actors. The number of actors can be huge but it should be
    * bounded and suitable for the workload. One actor per thread is not a bad choice. EntityProcessor actors take in
    * requests from outside scrupal-core (e.g. scrupal-http turns HTTP requests into Entity Actions)
    * This is the router that dispatches metric/event/topology requests to BSMC's Web Service SOAP interface for
    * handling. The router will select the actor with the smallest number of messages in its mailbox thus providing
    * smart load balancing. The actors are created by the router and managed by them. The pool starts with the
    * minimum number of actors and increases as needed to the upper bound.
    */
  def makeDispatcherRef(system: ActorSystem, min_actors: Int, max_actors: Int) = {
    /** Supervision Strategy Decider
      * This "Decider" determines what to do for a given exception type. For now, all exceptions cause restart of the
      * actor.
      * @return An Akka Decider object
      */
    def decider: Decider = { case t:Throwable => SupervisorStrategy.Restart }

    /** Escalation Object
      * We use a OneForOneStrategy so that failure decisions are done on an actor-by-actor basis.
      */
    val escalator = OneForOneStrategy(
      maxNrOfRetries = -1,
      withinTimeRange = Duration.Inf,
      loggingEnabled = true)(decider = decider)

    val resizer = Some(DefaultResizer(min_actors, max_actors))

    system.actorOf(
      Props(classOf[EntityProcessor]).withRouter(
        SmallestMailboxPool(
          nrOfInstances = min_actors,
          routerDispatcher = dispatcher_config_name,
          resizer = resizer,
          supervisorStrategy = escalator
        )
      ),
      actorName("EntityProcessingDispatcher")
    )
  }

  def makeSingletonRef(system: ActorSystem) = {
    system.actorOf(Props(classOf[EntityProcessor]),actorName("EntityProcessor"))
  }
}

/** The base trait for Scrupal Actors
  * This allows us to put the common things for all Scrupal Actors in one place
  */
class EntityProcessor extends Actor with ActorLogging {

  def receive : Receive = {
    case ecc: EntityCommand ⇒
      try {
        val result = ecc()
        sender ! result
      } catch {
        case xcptn: Throwable ⇒
          log.warning(s"The EntityCommand, $ecc, threw an exception: ", xcptn) // FIXME: doesn't print the exception
          sender ! ExceptionResult(xcptn)
      }
  }


}
