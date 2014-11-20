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

import java.util.concurrent.atomic.AtomicLong

import akka.actor.SupervisorStrategy.Decider
import akka.actor._
import akka.routing.{DefaultResizer, SmallestMailboxPool}

import scala.concurrent.duration.Duration

object ActionProcessor {

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
  def makeDispatcherRef(system: ActorSystem, min_actors: Int, max_actors: Int) : ActorRef = {
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
      Props(classOf[ActionProcessor]).withRouter(
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
    system.actorOf(Props(classOf[ActionProcessor]), actorName("EntityProcessor"))
  }


}

/** The super simple ActionProcessor
  *
  * An action is just a non-blocking function that produces a result. This Actor runs those functions in an Actor and
  * sends the Result[_] back to the sender.
  *
  *
  */
class ActionProcessor extends Actor with ActorLogging {

  def receive : Receive = {
    case action: Action ⇒
      try {
        sender ! action()
      } catch {
        case xcptn: Throwable ⇒
          log.warning(s"The EntityCommand, $action, threw an exception: ", xcptn) // FIXME: doesn't print the exception
          sender ! ExceptionResult(xcptn)
      }
  }
}
