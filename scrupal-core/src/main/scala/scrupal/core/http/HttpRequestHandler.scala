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

import play.api.http.{HttpFilters, HttpConfiguration, HttpErrorHandler, DefaultHttpRequestHandler}
import play.api.libs.iteratee.Iteratee
import play.api.mvc.{Result, RequestHeader, EssentialAction, Handler}
import play.api.routing.Router
import play.api.routing.Router.Routes
import scrupal.api.Site

import scala.annotation.switch

/** Scrupal Request Handler
  *
  * Play will invoke this handler to dispatch HTTP Requests as they come in. It's job is
  */
class HttpRequestHandler(errorHandler: HttpErrorHandler, configuration: HttpConfiguration,
  filters: HttpFilters) extends DefaultHttpRequestHandler(Router.empty, errorHandler, configuration, filters) {

  override def routeRequest(request: RequestHeader) : Option[Handler] = {
    val routes : Iterable[Routes] = for (
      site ← Site.forHost(request.host) ;
      app ← site.applications if app.hasRouteFor(request)
    ) yield {
      app.routeFor(request)
    }
    (routes.size : @switch) match {
      case 0 ⇒ super.routeRequest(request)
      case 1 ⇒ routes.head.lift(request)
      case _ ⇒ Some(new MultiAction(routes))
    }
  }
}

case class MultiAction(routes: Iterable[Routes]) extends EssentialAction {
  /// TODO: Implement MultiAction
  def apply(request: RequestHeader) : Iteratee[Array[Byte], Result] = ???
}
