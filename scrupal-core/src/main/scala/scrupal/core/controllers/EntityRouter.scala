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


import play.api.routing.Router
import play.api.routing.sird._
import play.api.mvc.{Request ⇒ PRequest, _}

import scrupal.api._

import scrupal.core.http.HttpUtils
import scrupal.core.http.netty.PlayDetailedRequest
import scrupal.utils.{Pluralizer, Patterns}

import scala.concurrent.{Future, ExecutionContext}

/** Router For Entities
  *
  * This maps
  */
case class EntityRouter(
  key : String, site: Site, entityProvider: EntityProvider
)(implicit scrupal: Scrupal) extends Router { self ⇒

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
    case GET(p"/${long(id)}/$facet/$facet_id$rest*") ⇒
      convert(entityProvider.get(id, facet, facet_id, rest))
    case GET(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet/$facet_id$rest*") ⇒
      convert(entityProvider.get(id, facet, facet_id, rest))
    case GET(p"/${long(id)}$rest*") ⇒
      convert(entityProvider.retrieve(id, rest))
    case GET(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>$rest*") ⇒
      convert(entityProvider.retrieve(id, rest))

    case OPTIONS(p"/${long(id)}/$facet$rest*") ⇒
      convert(entityProvider.find(id, facet, rest))
    case OPTIONS(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet$rest*") ⇒
      convert(entityProvider.find(id, facet, rest))
    case OPTIONS(p"/$rest*") ⇒
      convert(entityProvider.query(rest))

    case POST(p"/${long(id)}/$facet$rest*") ⇒
      convert(entityProvider.add(id, facet, rest))
    case POST(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet$rest*") ⇒
      convert(entityProvider.add(id, facet, rest))
    case POST(p"/$rest*") ⇒
      convert(entityProvider.create(rest))

    case PUT(p"/${long(id)}/$facet/$facet_id$rest*") ⇒
      convert(entityProvider.set(id, facet, facet_id, rest))
    case PUT(p"/${long(id)}$rest*") ⇒
      convert(entityProvider.update(id, rest))
    case PUT(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet/$facet_id$rest*") ⇒
      convert(entityProvider.set(id, facet, facet_id, rest))
    case PUT(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>$rest*") ⇒
      convert(entityProvider.update(id, rest))

    case DELETE(p"/${long(id)}/$facet/$facet_id$rest*") ⇒
      convert(entityProvider.remove(id, facet, facet_id, rest))
    case DELETE(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet/$facet_id$rest*") ⇒
      convert(entityProvider.remove(id, facet, facet_id, rest))
    case DELETE(p"/${long(id)}$rest*") ⇒
      convert(entityProvider.delete(id, rest))
    case DELETE(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>}$rest*") ⇒
      convert(entityProvider.delete(id, rest))
    case rh: RequestHeader ⇒
      throw new MatchError(s"EntityRouter could not match $rh")
  }

  val singularRoutes : Router.Routes = withPrefix(routesPattern, singularPrefix)
  val pluralRoutes : Router.Routes = withPrefix(routesPattern, pluralPrefix)
  val combinedRoutes : Router.Routes = pluralRoutes.orElse(singularRoutes)

  val routes : Router.Routes = combinedRoutes

  def withPrefix(prefix: String) : Router = {
    if (prefix == "/") {
      self
    } else {
      new Router {
        def routes = {
          val p = if (prefix.endsWith("/")) prefix else prefix + "/"
          val prefixed: PartialFunction[RequestHeader, RequestHeader] = {
            case rh: RequestHeader if rh.path.startsWith(p) => rh.copy(path = rh.path.drop(p.length - 1))
          }
          Function.unlift(prefixed.lift.andThen(_.flatMap(self.routes.lift)))
        }
        def withPrefix(prefix: String) = self.withPrefix(prefix)
        def documentation = self.documentation
      }
    }
  }

  protected def withPrefix(routes: Router.Routes, prefix: String) : Router.Routes = {
    val p = if (prefix.startsWith("/")) prefix else "/" + prefix
    val prefixed: PartialFunction[RequestHeader, RequestHeader] = {
      case header: RequestHeader if header.path.startsWith(p) =>
        header.copy(path = header.path.drop(p.length))
    }
    Function.unlift(prefixed.lift.andThen(_.flatMap(routes.lift)))
  }
}
