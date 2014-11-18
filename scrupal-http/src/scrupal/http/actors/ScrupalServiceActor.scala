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

package scrupal.http.actors

import akka.actor.{Props, Actor}
import akka.util.Timeout
import scrupal.core.Scrupal
import scrupal.core.api.Context
import scrupal.http.controllers.{AssetsController, WelcomeController, EntityController, Controller}
import scrupal.http.directives.SiteDirectives
import scrupal.utils.ScrupalComponent
import spray.http.MediaTypes._
import spray.routing.Route
import spray.routing._

import scala.util.{Failure, Success, Try}

object ScrupalServiceActor {
  def props(scrupal: Scrupal)(implicit askTimeout: Timeout): Props = Props(classOf[ScrupalServiceActor], scrupal)
  def name = "Scrupal-Service"
}

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ScrupalServiceActor(val scrupal: Scrupal)(implicit val askTimeout: Timeout) extends Actor with ScrupalService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // val assets = new AssetsController
  // val webjars = new WebJarsController

  // First, create all the controllers because routing depends on having controllers to make the routes.
  val the_controllers = createControllers(scrupal)

  val the_router = createRouter(scrupal)


  log.warn("Router: " + the_router) // FIXME: turn down to trace when we're sure routing works

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(the_router)
}


/** The top level Service For Scrupal
  * This trait is mixed in to the ScrupalServiceActor and provides the means by which all the controller's routes are
  */
trait ScrupalService extends HttpService with ScrupalComponent with SiteDirectives {

  def createRouter(scrupal: Scrupal) : Route = {
    Try {
      // Fold all the controller routes into one big one, sorted by priority
      val sorted_controllers = Controller.values.sortBy { c => c.priority}

      if (sorted_controllers.isEmpty)
        toss("No controllers found")

      // Very first thing we want to always do is make sure Scrupal Is Ready
      val base_routing = scrupalIsReady(scrupal)

      // scrupal-http provides the AssetController. Instantiate it now and provide it as the root controller
      val assets = new AssetsController

      // Now construct the routes from the prioritized set of controllers we found
      sorted_controllers.foldLeft[Route](assets.routes(scrupal)) { (route, ctrlr) =>
        route ~ ctrlr.routes(scrupal)
      }
    } match {
      case Success(r) ⇒ r
      case Failure(xcptn) ⇒
        path("") {
          get {
            respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
              extract( x ⇒ x ) { request_context ⇒
                complete {
                  val scrupal_context = Context(scrupal, request_context)
                  _root_.scrupal.http.views.html.RouteConstructionFailure(xcptn)(scrupal_context).toString()
                }
              }
            }
          }
        }
    }
  }

  /*
   * Called when an HTTP request has been received.
   *
   * The default is to use the application router to find the appropriate action.
   *
   * @param request the HTTP request header (the body has not been parsed yet)
   * @return an action to handle this request - if no action is returned, a 404 not found result will be sent to client
   * @see onActionNotFound
  override def onRouteRequest(request: RequestHeader): Option[play.api.mvc.Handler] = {
    if (ScrupalIsConfigured || pathsOkayWhenUnconfigured.findFirstMatchIn(request.path).isDefined ) {
      Logger.trace("Standard Routing for: " + request.path)
      DefaultGlobal.onRouteRequest(request)
    } else {
      Logger.trace("Configuration Routing for: " + request.path)
      Some(scrupal.controllers.ConfigWizard.configure())
    }
  }
  private val pathsOkayWhenUnconfigured = "^/(assets/|webjars/|configure|reconfigure|doc|scaladoc)".r
   */

  def createControllers(scrupal: Scrupal) : Seq[Controller] = {
    val configured_controllers = {
      for ((siteName, (site, appEntities)) ← scrupal.getAppEntities) yield {
        new EntityController(Symbol(siteName), 0, site, appEntities)
      }
    }.toSeq
    if (configured_controllers.isEmpty) {
      Seq( WelcomeController() )
    } else {
      configured_controllers
    }
  }
}

