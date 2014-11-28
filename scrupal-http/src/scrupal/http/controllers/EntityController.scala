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
import scrupal.api._
import shapeless.HNil
import spray.http.{Uri, HttpHeader}
import spray.routing.Route
import spray.routing._
import shapeless.::

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
case class EntityController(id: Symbol, provider: Entity, priority: Int) extends ActionProviderController {
  def make_args(ctxt: Context) : BSONDocument = {
    ctxt.request match {
      case Some(context) ⇒
        val headers = context.request.headers.map { hdr : HttpHeader  ⇒ hdr.name → BSONString(hdr.value) }
        val params = context.request.uri.query.map { case(k,v) ⇒ k -> BSONString(v) }
        BSONDocument(headers ++ params)
      case None ⇒
        BSONDocument()
    }
  }

  override def action(key: String, unmatchedPath: Uri.Path, context: Context) : Directive1[Action] = {
    new Directive1[Action] {
      def happly(f: ::[Action, HNil] ⇒ Route): Route = {
        if (key == provider.pluralKey) {
          rawPathPrefix(Segments ~ PathEnd) { segments: List[String] ⇒
            validate(segments.size > 0, "Empty identifier not permitted") {
              val id_path = segments.mkString("/")
              post {
                val action = provider.create(context, id_path, make_args(context))
                f(action :: HNil)
              } ~
                get {
                  val action = provider.retrieve(context, id_path)
                  f(action :: HNil)
                } ~
                put {
                  val action = provider.update(context, id_path, make_args(context))
                  f(action :: HNil)
                } ~
                delete {
                  val action = provider.delete(context, id_path)
                  f(action :: HNil)
                } ~
                options {
                  val action = provider.query(context, id_path, make_args(context))
                  f(action :: HNil)
                } ~
                reject(ValidationRejection(s"Not a good match for method"))
            }
          } ~
            reject(ValidationRejection(s"Request path is missing the entity identifier portion"))
        } else if (key == provider.singularKey) {
          rawPathPrefix(Segment / Segments ~ PathEnd) { (id: String, what: List[String]) ⇒
            validate(id.length > 0, "Empty identifier not permitted") {
              post {
                val action = provider.createFacet(context, id, what, make_args(context))
                f(action :: HNil)
              } ~
              get {
                val action = provider.retrieveFacet(context, id, what)
                f(action :: HNil)
              } ~
              put {
                val action = provider.updateFacet(context, id, what, make_args(context))
                f(action :: HNil)
              } ~
              delete {
                val action = provider.deleteFacet(context, id, what)
                f(action :: HNil)
              } ~
              options {
                val action = provider.queryFacet(context, id, what, make_args(context))
                f(action :: HNil)
              } ~
              reject(ValidationRejection(s"Not a good match for method"))
            }
          } ~ reject(ValidationRejection(s"Request path is missing entity id and what portions"))
        } else {
          reject(ValidationRejection(s"Request path is poorly formed."))
        }
      }
    }
  }
}
