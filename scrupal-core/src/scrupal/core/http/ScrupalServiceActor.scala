/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.core.http

import akka.actor.{Actor, Props}
import akka.util.Timeout
import scrupal.core.api._
import scrupal.core.html.PlainPage
import scrupal.utils.ScrupalComponent
import spray.http.MediaTypes._
import spray.routing.{Route, _}

import scala.util.{Failure, Success, Try}
import scalatags.Text.all._

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

  val the_router = createRouter(scrupal)


  log.warn("Router: " + the_router) // TODO: turn down to trace when we're sure routing works

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(the_router)
}


/** The top level Service For Scrupal
  * This trait is mixed in to the ScrupalServiceActor and provides the means by which all the controller's routes are
  */
trait ScrupalService extends HttpService with ScrupalComponent with SiteDirectives with RequestLoggers {

  def routeConstructionFailure(xcptn: Throwable) = {
    PlainPage("Unable To Process Route", "Unable To Process Route", {
      Seq(
        h1("Unable To Process Routes"),
        p("Unfortunately, Scrupal failed to construct its routes so it cannot process your request. This",
          "usually means there is a software problem that affects route construction in the controllers."),
        dl(
          dt("Exception Class"), dd(xcptn.getClass.getName),
          dt("Exception Message"), dd(xcptn.getMessage),
          dt("Stack Trace"), dd(
            code(
              small(
                for (ste <- xcptn.getStackTrace) { ste.toString + { if (ste.isNativeMethod) "(native)" else "" } },
                br()
              )
            )
        ))
      )
    })
  }

  def createRouter(scrupal: Scrupal) : Route = {

    Try {
      new AssetsController(scrupal).routes(scrupal) ~
      new ActionProviderController().routes(scrupal)
    } match {
      case Success(r) ⇒ r
      case Failure(xcptn) ⇒
        path("") {
          get {
            respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
              extract( x ⇒ x ) { request_context ⇒
                complete {
                  val scrupal_context = Context(scrupal, request_context)
                  routeConstructionFailure(xcptn)(scrupal_context).toString()
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

}

