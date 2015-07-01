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

import akka.event.Logging._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.RouteResult.Rejected
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.LogEntry
import akka.shapeless.HList

import scrupal.api._

import scrupal.utils.{Registrable, Registry}

/** Abstract Controller
  *
  * A controller in scrupal is a route provider and action generator. It provides a route object which
  * should separate valid from invalid routes and generate Actions for the valid ones.
  *
  * Created by reidspencer on 10/29/14.
  */
trait Controller extends Registrable[Controller]
  with Directives with ScrupalMarshallers with RequestLoggers {

  /** The priority of this controller for routing
    * This affects the controller's placement in the list of controllers that form the route processing.
    * Lower numbers increase the priority of the controller and move it towards the start of the list.
    * Scrupal's own controllers work within a -100 to 100 range and it is recommended that other
    * controllers operate within that same range. Note that this just helps optimize the processing
    * of routes.
    */
  def priority : Int

  /** The routes that this controller provides
    * Note that this does not include checking of the context path. That will have already done before the
    * routes here are invoked. So, this should only check within that 'context"
    */
  def routes(implicit scrupal : Scrupal) : Route

  /** Required method for registration */
  def registry : Registry[Controller] = Controller

  def request_context = extract(rc ⇒ rc)

  /* FIXME: Reinstate site directive in http.akka.Controller
  def site(scrupal : Scrupal) : Directive1[Site] = {
    extractHost.tflatMap { host : Tuple1[String] ⇒
      val sites = scrupal.Sites.forHost(host)
      if (sites.isEmpty)
        reject(ValidationRejection(s"No site defined for host '$host'."))
      else {
        val site = sites.head
        if (site.isEnabled(scrupal)) {
          schemeName.flatMap { scheme : String ⇒
            validate((scheme == "https") == site.requireHttps,
              s"Site '${site.name} requires ${if (site.requireHttps) "https" else "http"}.").hflatMap { hNil : HList ⇒
                extract { ctxt ⇒ site }
              }
          }
        } else {
          reject(ValidationRejection(s"Site '${site.name} is disabled."))
        }
      }
    }
  }*/
}

abstract class BasicController(val id : Identifier, val priority : Int = 0) extends Controller

/** A Controller For ActionProviders
  *
  * Scrupal API module provides the ActionProvider that can convert a path
  */
class ActionProviderController extends BasicController('ActionProviderController, 0) with PathHelpers {

  def routes(implicit scrupal : Scrupal) : Route = {
    reject
    /* FIXME: Reinstate ActionProviderController.routes
    site(scrupal) { site : Site ⇒
      rawPathPrefix(Slash) {
        val x = FormDataMarshaller
        entity(as[FormData]){ emailData⇒
        }
        request_context { ctxt : RequestContext ⇒
          implicit val context = Context(scrupal, ctxt, site)
          site.extractAction(context) match {
            case Some(action) ⇒ complete {
              makeMarshallable {
                val future_result = action.dispatch
                future_result
              }
            }
            case None ⇒ reject
          }
        }
      }
    }
  */
  }
}

trait RequestLoggers {
  def showRequest(request : HttpRequest) = LogEntry(request.uri, InfoLevel)

  def showAllResponses(request : HttpRequest) : Any ⇒ Option[LogEntry] = {
    case x : HttpResponse ⇒ {
      println (s"Normal: $request")
      createLogEntry(request, x.status + " " + x.toString)
    }
    case Rejected(rejections) ⇒ {
      println (s"Rejection: $request")
      createLogEntry(request, " Rejection " + rejections.toString())
    }
    case x ⇒ {
      println (s"other: $request")
      createLogEntry(request, x.toString)
    }
  }

  def createLogEntry(request : HttpRequest, text : String) : Some[LogEntry] = {
    Some(LogEntry("#### Request " + request + " ⇒ " + text, DebugLevel))
  }

  def showErrorResponses(request : HttpRequest) : Any ⇒ Option[LogEntry] = {
    case HttpResponse(OK | NotModified | PartialContent, _, _, _) ⇒ None
    case HttpResponse(StatusCodes.NotFound, _, _, _) ⇒ Some(LogEntry("404: " + request.uri, WarningLevel))
    case r @ HttpResponse(Found | MovedPermanently, _, _, _) ⇒
      Some(LogEntry(s"${r.status.intValue}: ${request.uri} -> ${r.header[Location].map(_.uri.toString).getOrElse("")}", WarningLevel))
    case response ⇒ Some(
      LogEntry("Non-200 response for\n  Request : " + request + "\n  Response: " + response, WarningLevel))
  }

  /*
  def showRepoResponses(repo : String)(request : HttpRequest) = {
    case HttpResponse(s @ (OK | NotModified), _, _, _) ⇒ Some(LogEntry(s"$repo  ${s.intValue}: ${request.uri}", InfoLevel))
    case HttpResponse(OK, _, _, _) ⇒ Some(LogEntry(repo + " 200 (chunked): " + request.uri, InfoLevel))
    case HttpResponse(StatusCodes.NotFound, _, _, _) ⇒ Some(LogEntry(repo + " 404: " + request.uri))
    case _ ⇒ None
  }
  */
}

object Controller extends Registry[Controller] {
  override val registryName : String = "Controllers"
  override val registrantsName : String = "Controller"

}
