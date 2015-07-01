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

package scrupal.core.akkahttp

import akka.actor._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives._
import akka.util.Timeout
import scrupal.api.Html.ContentsArgs
import scrupal.api._
import scrupal.api.html.PlainPageGenerator
import scrupal.utils.ScrupalComponent

import scalatags.Text.all._

object ScrupalServiceActor {
  def props(scrupal : Scrupal)(implicit askTimeout : Timeout) : Props = Props(classOf[ScrupalServiceActor], scrupal)
  def name = "Scrupal-Service"
}

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ScrupalServiceActor(implicit val scrupal : Scrupal, implicit val askTimeout : Timeout) extends Actor with ScrupalService {

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
  def receive = { case _ ⇒ None } // runRoute(the_router)
}

/** The top level Service For Scrupal
  * This trait is mixed in to the ScrupalServiceActor and provides the means by which all the controller's routes are
  */
trait ScrupalService extends ScrupalComponent
    with BasicDirectives with SiteDirectives with RouteDirectives with RequestLoggers {

  case class RouteConstructionFailure(xcptn : Throwable) extends PlainPageGenerator {
    val title = "Unable To Process Route"
    val description = "Unable To Process Route"
    def content(context : Context, args : ContentsArgs = Html.EmptyContentsArgs) : Html.Contents = {
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
                for (ste ← xcptn.getStackTrace) { ste.toString + { if (ste.isNativeMethod) "(native)" else "" } },
                br()
              )
            )
          ))
      )
    }
  }

  def createRouter(scrupal : Scrupal) : Route = {
    reject
    /* TODO: Review and delete or reinstate
    Try {
      new AssetsController(scrupal).routes(scrupal) ~
        new ActionProviderController().routes(scrupal)
    } match {
      case Success(r) ⇒ r
      case Failure(xcptn) ⇒
        path("") {
          get {
            respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
              extract(x ⇒ x) { request_context ⇒
                complete {
                  val scrupal_context = Context(scrupal, request_context)
                  RouteConstructionFailure(xcptn).render(scrupal_context)
                }
              }
            }
          }
        }
    }
    */
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

