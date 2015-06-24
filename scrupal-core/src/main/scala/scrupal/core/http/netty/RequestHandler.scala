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

import java.nio.charset.Charset

import akka.http.scaladsl.model.{HttpMethods, Uri, MediaTypes, MediaType}

import javax.inject.{Inject,Singleton}

import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Environment}
import play.api.http.{DefaultHttpRequestHandler, HttpConfiguration, HttpErrorHandler, HttpFilters}
import play.api.libs.iteratee.Enumerator
import play.api.routing.Router
import play.api.mvc.Results.{NotFound, Ok, Conflict}
import _root_.play.api.mvc

import scrupal.api.Request
import scrupal.api._
import scrupal.core.http.{netty, HttpUtils}

import scala.annotation.switch
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.matching.Regex

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
    val reactions : Iterable[(Context,Reactor)] = {
      for (
        site ← scrupal.Sites.forHost(header.host) ;
        context = Context(scrupal, site) ;
        req = PlayRequest(context, header);
        provider ← site.delegates
          if provider.canProvide(req);
        reaction = provider.provide(req)
          if reaction.nonEmpty
      ) yield {
        context → reaction.get
      }
    }
    (reactions.size : @switch) match {
      case 0 ⇒ {
        Some(mvc.Action { r: mvc.RequestHeader ⇒ NotFound(s"No Reactor For $r") })
      }
      case 1 ⇒ {
        val (context : Context, reactor : Reactor) = reactions.head
        val action = SingleAction(context, reactor).action
        Some(action)
      }
      case _ ⇒ {
        Some(mvc.Action { r: mvc.RequestHeader ⇒ Conflict(s"Found ${reactions.size} possible reactions to $r")})
      }
    }
  }
}

case class PlayRequest(context: Context, playRequest: mvc.RequestHeader) extends Request {
  val method = HttpMethods.getForKeyCaseInsensitive(playRequest.method).getOrElse(HttpMethods.GET)
  val path = Uri.Path(playRequest.path,playRequest.charset.map {s ⇒ Charset.forName(s)}.getOrElse(utf8))
  override val query = playRequest.queryString
}

case class PlayDetailedRequest(context: Context, detail: mvc.Request[mvc.RawBuffer]) extends DetailedRequest {
  val method = HttpMethods.getForKeyCaseInsensitive(detail.method).getOrElse(HttpMethods.GET)
  val path = Uri.Path(detail.path, detail.charset.map {s ⇒ Charset.forName(s)}.getOrElse(utf8))
  override val query = detail.queryString
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


case class SingleAction(context: Context, reactor : Reactor)  {
  def action = mvc.Action.async(mvc.BodyParsers.parse.raw) { playRequest : mvc.Request[mvc.RawBuffer] ⇒
    import HttpUtils._
    val details : DetailedRequest = PlayDetailedRequest(context, playRequest)
    context.withExecutionContext { implicit ec : ExecutionContext ⇒
      reactor(details) map { response ⇒
        val status = response.disposition.toStatusCode.intValue()
        val header = mvc.ResponseHeader(status)
        mvc.Result(header, response.payload)
      }
    }
  }
}

