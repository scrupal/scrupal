/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.http.controllers

import reactivemongo.bson.{BSONString, BSONDocument}
import scrupal.core.Scrupal
import scrupal.core.api._
import scrupal.http.ScrupalMarshallers
import scrupal.http.directives.{PathHelpers, SiteDirectives}
import shapeless.HNil
import spray.http.HttpHeader
import spray.routing.{Directives, Route}
import spray.routing._

import scala.concurrent.ExecutionContext

/** A Controller For Entities
  * This controller handles entity requests for an site. It caches the set of entities it is responsible for by traversing
  * the site it is provided with and locating all the entities that are enabled for the site. It then builds a route
  * from all the methods and paths that entities inherently support. Requests to these paths are constructed into
  * Action objects which are delivered to core for execution. The asynchronous response eventually gets back to the user
  *
  * NOTE: if new entities are created or lots have been disabled, it will be more efficient to just instantiate a new
  * EntityController and let it reacquire the set of entities it serves. This will rebuild the routes accordingly and
  * start simply returning 404 instead of errors about unavailable resources.
  * Created by reid on 11/7/14.
  */
case class EntityController(id: Symbol, priority: Int, theSite: Site, appEntities: Site#ApplicationMap)
  extends Controller with Directives with SiteDirectives with PathHelpers with ScrupalMarshallers
{

  type AppEntityList = shapeless.::[Application,shapeless.::[String,shapeless.::[Entity,HNil]]]


  def app_entity : Directive[AppEntityList] = new Directive[AppEntityList] {
    def happly(f: AppEntityList ⇒ Route) = {

      pathPrefix(appEntities) {
        case (app, app_entities) ⇒ {
          pathPrefix(Segment) { seg: String ⇒
            app_entities.get(seg) match {
              case Some((entityName,entity)) ⇒ f(app :: entityName :: entity :: HNil)
              case None => reject
            }
          }
        }
      }
    }
  }


  def make_args(ctxt: ApplicationContext) : BSONDocument = {
    ctxt.request match {
      case Some(context) ⇒
        val headers = context.request.headers.map { hdr : HttpHeader  ⇒ hdr.name → BSONString(hdr.value) }
        val params = context.request.uri.query.map { case(k,v) ⇒ k -> BSONString(v) }
        BSONDocument(headers ++ params)
      case None ⇒
        BSONDocument()
    }
  }

  def routes(scrupal: Scrupal) : Route = {
    scrupal.withExecutionContext { implicit ec: ExecutionContext ⇒
      site(scrupal) { aSite ⇒
        validate(aSite == theSite, s"Expected site ${theSite.name } but got ${aSite.name }") {
          app_entity {
            case (app: Application, entityName: String, entity: Entity) ⇒ {
              if (entityName == entity.path) {
                rawPathPrefix(Slash ~ Segments ~ PathEnd) { id: List[String] ⇒
                  validate(id.size > 0, "Empty identifier not permitted") {
                    request_context { rc: RequestContext ⇒
                      val ctxt = Context(scrupal, rc, aSite, app)
                      val id_path = id.mkString("/")
                      post    { complete(scrupal.handle(entity.create(ctxt, id_path, make_args(ctxt)))) } ~
                        get     { complete(scrupal.handle(entity.retrieve(ctxt, id_path))) } ~
                        put     { complete(scrupal.handle(entity.update(ctxt, id_path, make_args(ctxt)))) } ~
                        delete  { complete(scrupal.handle(entity.delete(ctxt, id_path))) } ~
                        options { complete(scrupal.handle(entity.query(ctxt, id_path, make_args(ctxt)))) } ~
                        reject(ValidationRejection(s"Not a good match for method"))
                    }
                  }
                } ~ reject(ValidationRejection(s"Request path is missing the entity identifier portion"))

              } else if (entityName == entity.plural_path) {
                rawPathPrefix(Slash ~ Segment / Segments ~ PathEnd) { (id: String, what: List[String]) ⇒
                  validate(id.length > 0, "Empty identifier not permitted") {
                    request_context { rc: RequestContext ⇒
                      val ctxt = Context(scrupal, rc, aSite, app)
                      post    { complete(scrupal.handle(entity.createFacet(ctxt, id, what, make_args(ctxt)))) } ~
                        get     { complete(scrupal.handle(entity.retrieveFacet(ctxt, id, what))) } ~
                        put     { complete(scrupal.handle(entity.updateFacet(ctxt, id, what, make_args(ctxt)))) } ~
                        delete  { complete(scrupal.handle(entity.deleteFacet(ctxt, id, what))) } ~
                        options { complete(scrupal.handle(entity.queryFacet(ctxt, id, what, make_args(ctxt)))) } ~
                        reject(ValidationRejection(s"Not a good match for method"))
                    }
                  }
                } ~ reject(ValidationRejection(s"Request path is missing entity id and what portions"))

              } else {
                reject(ValidationRejection(s"Request path is poorly formed."))
              }
            }
          }
        }
      }
    }
  }
}
