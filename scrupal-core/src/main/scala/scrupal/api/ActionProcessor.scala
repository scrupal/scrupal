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

import java.util.concurrent.atomic.AtomicLong

import akka.actor.SupervisorStrategy.Decider
import akka.actor._
import akka.routing.{ DefaultResizer, SmallestMailboxPool }
import scrupal.utils.ScrupalComponent

import scala.concurrent.Future
import scala.concurrent.duration.Duration

object ActionProcessor extends ScrupalComponent {

  /** A counter for the number of actors. This is to ensure we make unique names */
  val actorCounter = new AtomicLong(0)

  /** A method to generate a unique name for each actor that meets Akka's naming requirements.
    *
    * @param name The name of the type of the actor
    * @return The unique name generated for the actor instance
    */
  def actorName(name : String) : String = {
    val result = name.replace(" ", "-") + "-" + actorCounter.incrementAndGet()
    log.debug("Created new actor: " + name)
    result
  }

  /** Name of the configuration key for the configuration of the dispatcher */
  val dispatcher_config_name = "scrupal.dispatcher"

  /** Supervision Strategy Decider
    * This "Decider" determines what to do for a given exception type. For now, all exceptions cause restart of the
    * actor.
    * @return An Akka Decider object
    */
  def decider : Decider = { case t : Throwable ⇒ SupervisorStrategy.Restart }

  /** Escalation Object
    * We use a OneForOneStrategy so that failure decisions are done on an actor-by-actor basis.
    */
  val escalator = OneForOneStrategy(
    maxNrOfRetries = -1,
    withinTimeRange = Duration.Inf,
    loggingEnabled = true)(decider = decider)

  /** Make The Dispatcher ActorRef
    *
    * This is a router behind a cadre of ActionProcessor actors. The number of actors can be huge but it should be
    * bounded and suitable for the workload. One actor per thread is not a bad choice. ActionProcessor actors take in
    * Action requests from outside scrupal-core and process them. The router will select the actor with the smallest
    * number of messages in its mailbox thus providing smart load balancing. The actors are created by the router and
    * managed by them. The pool starts with the minimum number of actors and increases as needed to the upper bound.
    */
  def makeDispatcherRef(system : ActorSystem, min_actors : Int, max_actors : Int) : ActorRef = {

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
      actorName("ActionProcessor")
    )
  }

  def makeSingletonRef(system : ActorSystem) = {
    system.actorOf(Props(classOf[ActionProcessor]), actorName("ActionProcessor"))
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
    case action : Reactor ⇒
      try {
        val result = action()
        sender ! result
      } catch {
        case xcptn : Throwable ⇒
          log.warning(s"The Command, $action, threw an exception: ", xcptn) // FIXME: doesn't print the exception
          sender ! Future.failed(xcptn)
      }
  }
}
