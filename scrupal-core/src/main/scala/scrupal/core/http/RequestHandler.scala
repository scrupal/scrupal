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

package scrupal.core.http

import javax.inject.{Inject, Singleton}

import play.api.http.{DefaultHttpRequestHandler, HttpConfiguration, HttpErrorHandler, HttpFilters}
import play.api.inject.ApplicationLifecycle
import play.api.mvc.Results.Conflict
import play.api.mvc._
import play.api.{Configuration, Environment}
import scrupal.api.{Scrupal, Context, Reactor}

import scala.annotation.switch
import scala.language.implicitConversions

/** Scrupal Request Handler
  *
  * Play will invoke this handler to dispatch HTTP Requests as they come in. It's job is
  */
@Singleton
class RequestHandler @Inject() (
  env: Environment,
  scrupal: Scrupal,
  play_config : Configuration,
  errorHandler: HttpErrorHandler,
  http_config: HttpConfiguration,
  filters: HttpFilters,
  lifecycle: ApplicationLifecycle,
  globalRouter: router.Routes
  ) extends DefaultHttpRequestHandler(globalRouter, errorHandler, http_config, filters) {

  scrupal.open()

  override def routeRequest(header: RequestHeader): Option[Handler] = {
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
        super.routeRequest(header)
      case 1 ⇒
        val (context: Context, reactor: Reactor) = handlers.head
        Some {
          Action.async { req: Request[AnyContent] ⇒
            reactor.resultFrom(context, req)
          }
        }
      case _ ⇒
        Some {
          Action { r: RequestHeader ⇒ Conflict(s"Found ${handlers.size} possible reactions to $r") }
        }
    }
  }
}
