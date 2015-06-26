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

package scrupal.core.controllers


import play.api.Routes
import play.api.routing.{SimpleRouter, Router}
import play.api.routing.sird._
import play.api.mvc.{Request ⇒ PRequest, _}
import play.api.mvc.Results.{NotFound, Ok, Conflict}

import scrupal.api._

import scrupal.core.http.HttpUtils
import scrupal.core.http.netty.PlayDetailedRequest
import scrupal.utils.{Pluralizer, Patterns}

import scala.concurrent.{Future, ExecutionContext}

/** Router For Entities
  *
  * This maps
  */
abstract class EntityRouter(val key : String, site: Site, ep: EntityProvider)(implicit scrupal: Scrupal) extends Router {

  override def documentation : Seq[(String,String,String)] = {
    Seq( ("TBD", "TBD", "TBD") ) // TODO: Implement EntityRouter.documentation
  }

  def makeKey(name: String) = name.toLowerCase.replaceAll(Patterns.NotAllowedInUrl.pattern.pattern, "-")

  /** Singular form of the entity's label */
  val singularPrefix = makeKey(key)

  /** Plural form of the entity's label */
  val pluralPrefix = makeKey(Pluralizer.pluralize(key))

  private def convert(func: => Reactor): Action[_] = {
    Action.async(BodyParsers.parse.raw) { req: PRequest[RawBuffer] ⇒
      import HttpUtils._
      val context = Context(scrupal, site)
      val details: DetailedRequest = PlayDetailedRequest(context, req)
      val reactor = func
      context.withExecutionContext { implicit ec: ExecutionContext ⇒
        reactor(details) map { response ⇒
          val d = response.disposition
          val status = d.toStatusCode.intValue()
          val msg = Some(s"HTTP($status): ${d.id.name}(${d.code}): ${d.msg}")
          val header = ResponseHeader(status, reasonPhrase = msg)
          Result(header, response.payload)
        }
      }
    }
  }

  val routesPattern : Router.Routes = {
    case OPTIONS(p"$rest*") ⇒ convert( ep.query(rest) )
    case GET(p"/${long(id)}$rest*") ⇒ convert( ep.retrieve(id, rest))
    case PUT(p"/${long(id)}$rest*") ⇒ convert( ep.update(id, rest))
    case POST(p"$rest*") ⇒ convert(ep.create(rest))
    case DELETE(p"/${long(id)}$rest*") ⇒ convert( ep.delete(id, rest))

    case GET(p"/$id<[^0-9]+>$rest*") ⇒ convert(ep.retrieve(id, rest))
    case PUT(p"/$id<[^0-9]+>$rest*") ⇒ convert(ep.update(id, rest))
    case DELETE(p"/$id<[^0-9]+>}$rest*") ⇒ convert( ep.delete(id, rest))

    case OPTIONS(p"${long(id)}/$facet$rest*") ⇒ convert(ep.find(id, facet, rest))
    case GET(p"/${long(id)}/$facet/$facet_id$rest*") ⇒ convert(ep.get(id, facet, facet_id, rest))
    case PUT(p"/${long(id)}/$facet/$facet_id$rest*") ⇒ convert(ep.set(id, facet, facet_id, rest))
    case POST(p"${long(id)}/$facet$rest*") ⇒ convert(ep.add(id, facet, rest))
    case DELETE(p"/${long(id)}/$facet/$facet_id$rest*") ⇒ convert(ep.remove(id, facet, facet_id, rest))

    case OPTIONS(p"$id<[^0-9]+>/$facet$rest*") ⇒ convert(ep.find(id, facet, rest))
    case GET(p"/$id<[^0-9]+>/$facet/$facet_id$rest*") ⇒ convert(ep.get(id, facet, facet_id, rest))
    case PUT(p"/$id<[^0-9]+>/$facet/$facet_id$rest*") ⇒ convert(ep.set(id, facet, facet_id, rest))
    case POST(p"$id<[^0-9]+>/$facet$rest*") ⇒ convert(ep.add(id, facet, rest))
    case DELETE(p"/$id<[^0-9]+>/$facet/$facet_id$rest*") ⇒ convert(ep.remove(id, facet, facet_id, rest))
  }

  def routes : Router.Routes = {
    val singular = withPrefix(routesPattern, singularPrefix)
    val plural = withPrefix(routesPattern, pluralPrefix)
    val routes = plural.orElse(singular)
    routes
  }

  def withPrefix(routes: Router.Routes, prefix: String) : Router.Routes = {
    val p = if (prefix.endsWith("/")) prefix else prefix + "/"
    val prefixed: PartialFunction[RequestHeader, RequestHeader] = {
      case header: RequestHeader if header.path.startsWith(p) =>
        header.copy(path = header.path.drop(p.length - 1))
    }
    Function.unlift(prefixed.lift.andThen(_.flatMap(routes.lift)))
  }
}
