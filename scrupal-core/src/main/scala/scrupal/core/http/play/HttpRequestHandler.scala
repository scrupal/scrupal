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

package scrupal.core.http.play

import akka.http.scaladsl.model.{MediaTypes, MediaType}

import com.google.inject.Inject
import play.api.{Configuration, Environment}
import play.api.http.{DefaultHttpRequestHandler, HttpConfiguration, HttpErrorHandler, HttpFilters}
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc._
import play.api.routing.Router
import play.api.mvc.Results.Ok

import scrupal.api.Request
import scrupal.api._
import scrupal.core.http.HttpUtils

import scala.annotation.switch
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.matching.Regex

/** Scrupal Request Handler
  *
  * Play will invoke this handler to dispatch HTTP Requests as they come in. It's job is
  */
class HttpRequestHandler @Inject() (
  env: Environment,
  play_config : Configuration,
  errorHandler: HttpErrorHandler,
  http_config: HttpConfiguration,
  filters: HttpFilters
) extends DefaultHttpRequestHandler(Router.empty, errorHandler, http_config, filters) {

  implicit val scrupal = _root_.scrupal.core.impl.Scrupal("Scrupal", Some(play_config))


  override def routeRequest(header: RequestHeader) : Option[Handler] = {
    val reactions : Iterable[(Site,Reactor)] = {
      for (
        site ← scrupal.Sites.forHost(header.host);
        req = PlayRequest(scrupal, header, site);
        app ← site.applications if app.canProvide(req);
        reaction = app.provide(req) if reaction.nonEmpty
      ) yield {
        site → reaction.get
      }
    }
    (reactions.size : @switch) match {
      case 0 ⇒ super.routeRequest(header)
      case 1 ⇒ {
        val (site : Site, reactor : Reactor) = reactions.head
        val action = SingleAction(scrupal, site, reactor).action
        Some(action)
      }
      case _ ⇒ {
        val action = MultiAction(reactions).action
        Some(action)
      }
    }
  }
}

object PathParts extends Regex( """/([^/]+)/([^/]+)/([^/]+)/([^?&#]+)""" )

case class PlayRequest(scrupal: Scrupal, playRequest: RequestHeader, site: Site) extends Request {
  val PathParts(application, entity, instance, msg) = playRequest.path
  override val message = msg.split('/').toIterable
  val context = Context(scrupal, site)
}

case class PlayDetailedRequest(scrupal: Scrupal, detail: play.api.mvc.Request[RawBuffer], site: Site) extends DetailedRequest {
  val PathParts(application, entity, instance, msg) = detail.path
  override val message = msg.split('/').toIterable
  val context = Context(scrupal, site)
  override val mediaType : MediaType = detail.mediaType match {
    case Some(mt) ⇒
      MediaTypes.getForKey( mt.mediaType → mt.mediaSubType ).getOrElse(MediaTypes.`application/octet-stream`)
    case None ⇒
      MediaTypes.`application/octet-stream`
  }

  override val parameters = detail.headers.toSimpleMap ++
    detail.queryString.transform {
      case (k,v) ⇒
        v.mkString(",")
    }

  override val payload : Enumerator[Array[Byte]] = detail.body.asBytes() match {
    case Some(bytes) ⇒ Enumerator(bytes)
    case None ⇒ Enumerator.empty
  }
}


case class SingleAction(scrupal: Scrupal, site: Site, reactor : Reactor)  {
  def action = Action.async(BodyParsers.parse.raw) { playRequest : play.api.mvc.Request[RawBuffer] ⇒
    import HttpUtils._
    val details : DetailedRequest = PlayDetailedRequest(scrupal, playRequest, site)
    scrupal.withExecutionContext { implicit ec : ExecutionContext ⇒
      reactor(details) map { response ⇒
        val status = response.disposition.toStatusCode.intValue()
        val header = ResponseHeader(status)
        Result(header, response.payload)
      }
    }
  }
}

case class MultiAction(routes: Iterable[(Site, Reactor)]) {
  def action = Action.async(BodyParsers.parse.raw) { playRequest : play.api.mvc.Request[RawBuffer] ⇒
    /// TODO: Implement MultiAction
    import HttpUtils._
    Future.successful { Ok("MultiAction not yet implemented") }
  }
}

