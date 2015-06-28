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

package scrupal.core.http.netty

import javax.inject.{Inject,Singleton}

import play.api.inject.ApplicationLifecycle
import play.api.mvc._
import play.api.{Configuration, Environment}
import play.api.http.{DefaultHttpRequestHandler, HttpConfiguration, HttpErrorHandler, HttpFilters}
import play.api.routing.Router
import play.api.mvc.Results.{NotFound, Conflict}
import _root_.play.api.mvc
import scrupal.api.{Reactor, Response, Stimulus, Context}

import scrupal.core.http.netty

import scala.annotation.switch
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

/** Scrupal Request Handler
  *
  * Play will invoke this handler to dispatch HTTP Requests as they come in. It's job is
  */
@Singleton
class RequestHandler @Inject() (
  env: Environment,
  play_config : Configuration,
  errorHandler: HttpErrorHandler,
  http_config: HttpConfiguration,
  filters: HttpFilters,
  lifecycle : ApplicationLifecycle
) extends DefaultHttpRequestHandler(Router.empty, errorHandler, http_config, filters) {

  implicit val scrupal = netty.Scrupal("Scrupal", play_config, lifecycle)

  scrupal.open()

  override def routeRequest(header: mvc.RequestHeader) : Option[mvc.Handler] = {
    val handlers = {
      for ( site ← scrupal.Sites.forHost(header.host) ;
            context = Context(scrupal, site) ;
            reactor ← site.reactorFor(header)
      ) yield {
        context → reactor
      }
    }

    (handlers.size : @switch) match {
      case 0 ⇒
        Some(mvc.Action { r: mvc.RequestHeader ⇒ NotFound(s"No Reactor For $r") })
      case 1 ⇒
        val (context: Context, reactor: Reactor) = handlers.head
        Some {
          Action.async { req: Request[AnyContent] ⇒
            val details: Stimulus = Stimulus(context, req)
            context.withExecutionContext { implicit ec: ExecutionContext ⇒
              reactor(details) map { response: Response ⇒
                val d = response.disposition
                val status = d.toStatusCode.intValue()
                val msg = Some(s"HTTP($status): ${d.id.name}(${d.code}): ${d.msg}")
                val header = ResponseHeader(status, reasonPhrase = msg)
                Result(header, response.toEnumerator)
              }
            }
          }
        }
      case _ ⇒
        Some {
          mvc.Action { r: mvc.RequestHeader ⇒ Conflict(s"Found ${handlers.size} possible reactions to $r")}
        }
    }
  }
}
