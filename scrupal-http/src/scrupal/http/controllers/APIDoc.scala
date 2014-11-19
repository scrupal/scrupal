/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

import scrupal.core.Scrupal
import scrupal.core.api.{Instance, Module}
import scrupal.utils.Pluralizer
import spray.routing._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** One line sentence description here.
  * Further description here.
  */
case class APIDoc(id: Symbol, priority: Int) extends Controller {
  def routes(implicit scrupal: Scrupal) : Route = reject

  /*
# Routes for the REST based API Documentation facility. Same as /api but all GET requests with METHOD inserted at start
GET            /apidoc                                scrupal.controllers.APIDoc.introduction()
GET            /apidoc/GET/:kind/:id/:action          scrupal.controllers.APIDoc.doTo(kind, id, action)
GET            /apidoc/GET/:kind/:id                  scrupal.controllers.APIDoc.fetch(kind, id)
GET            /apidoc/POST/:kind/:id                 scrupal.controllers.APIDoc.create(kind,id)
GET            /apidoc/PUT/:kind/:id                  scrupal.controllers.APIDoc.update(kind, id)
GET            /apidoc/DELETE/:kind/:id               scrupal.controllers.APIDoc.delete(kind, id)
GET            /apidoc/HEAD/:kind/:id                 scrupal.controllers.APIDoc.summarize(kind, id)
GET            /apidoc/OPTIONS/:kind/:id              scrupal.controllers.APIDoc.optionsOf(kind, id)
GET            /apidoc/GET/:kind                      scrupal.controllers.APIDoc.fetchAll(kind)
GET            /apidoc/POST/:kind                     scrupal.controllers.APIDoc.createAll(kind)
GET            /apidoc/PUT/:kind                      scrupal.controllers.APIDoc.updateAll(kind)
GET            /apidoc/DELETE/:kind                   scrupal.controllers.APIDoc.deleteAll(kind)
GET            /apidoc/HEAD/:kind                     scrupal.controllers.APIDoc.summarizeAll(kind)
GET            /apidoc/OPTIONS/:kind                  scrupal.controllers.APIDoc.optionsOfAll(kind)

 */

  /*
    val apidocs = CoreFeatures.RESTAPIDocumentation

    /** Provide an introduction to the API */
    def introduction() = UserAction { implicit context: AnyUserContext =>
      WithFeature(apidocs) {
        Ok(html.api.introduction(modules, types))
      }
    }

    def fetchAll(kind: String) = UserAction { implicit context: AnyUserContext =>
      WithFeature(apidocs) {
        val (singular, description, module) = kind.toLowerCase() match {
          case "sites" =>    ("site", "A site that Scrupal is configured to serve", "Core")
          case "modules" =>  ("module", "A Scrupal Plug-in that extends it's functionality", "Core")
          case "entities" => ("entity", "The kinds of entities that Scrupal is configured to serve", "Core")
          case "types" =>    ("type", "The data types that Scrupal is configured to serve", "Core" )
          case "instances" => ("instance", "The entity instances Scrupl is configured to serve", "Core")
          case _ =>          (kind, "No further description available.", "Unknown")
        }

        Ok(html.api.fetchAll(singular, Pluralizer.pluralize(singular), description, module))
      }
    }

    def fetch(kind: String, id: String) = UserAction.async { implicit context: AnyUserContext =>
      AsyncWithFeature(apidocs) {
        kind.toLowerCase match {
          case "type" =>  Future {
            Type(Symbol(id)) match {
              case t: Some[Type] => Ok(html.api.fetchType(t.get))
              case _ => NotFound("Type " + id, Seq("You mis-typed '" + id + "'?"))
            }
          }
          case "module" => Future {
            Module(Symbol(id)) match {
              case m: Some[Module] => Ok(html.api.fetchModule(m.get))
              case _ => NotFound("Module " + id, Seq("You mis-typed '" + id + "'?"))
            }
          }
          case "site" => Future {
            Site(Symbol(id)) match {
              case s: Some[Site] => Ok(html.api.fetchSite(s.get))
              case _ => NotFound("Site " + id, Seq("You mis-typed '" + id + "'?"))
            }
          }
          case "entity" => Future {
            Entity(Symbol(id)) match {
              case e: Some[Entity] => Ok(html.api.fetchEntity(e.get))
              case _ => NotFound("Entity " + id, Seq())
            }
          }
          case _ => Entity(Symbol(kind)) match {
            case e: Some[Entity] => {
              context.schema.instances.fetch(Symbol(id)) map { os: Option[Instance]=>
                os match {
                  case Some(instance) => Ok(html.api.fetchInstance(Symbol(id), instance))
                  case None => NotFound("the " + kind + " " + id, Seq("the " + kind + " was not found"))
                }
              }
            }
            case _ => Future { NotFound("fetch of " + kind + " " + id, Seq("You mis-typed '" + kind + "'?") ) }
          }
        }
      }
    }

    def createAll(kind: String) = UserAction { implicit context: AnyUserContext => WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Creation of " + kind + " not finished."))
    }}

    def create(kind: String, id: String) = UserAction { implicit context: AnyUserContext => WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Creation of " + kind + " " + id + " not finished."))
    }}

    def deleteAll(kind: String) = UserAction { implicit context: AnyUserContext => WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Deletion of all " + kind + " not finished."))
    }}

    def delete(kind: String, id: String) = UserAction { implicit context: AnyUserContext => WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Deletion of " + kind + " " + id + " not finished."))
    }}

    def updateAll(kind: String) = UserAction { implicit context: AnyUserContext => WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Update of " + kind + " not finished."))
    }}

    def update(kind: String, id: String) = UserAction { implicit context: AnyUserContext => WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Update of " + kind + " " + id + " not finished."))
    }}

    def summarizeAll(kind: String) = UserAction { implicit context: AnyUserContext => WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Info for " + kind + " not finished."))
    }}

    def summarize(kind: String, id: String) = UserAction { implicit context: AnyUserContext => WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Info for " + id + " of kind " + kind + " not finished."))
    }}

    def optionsOfAll(kind : String) = UserAction { implicit context: AnyUserContext => WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Options of " + kind + " not finished."))
    }}

    def optionsOf(kind: String, id: String) = UserAction { implicit context: AnyUserContext => WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Options of " + kind + " for " + id + " not finished."))
    }}

    def doTo(kind: String, id: String, action: String) = UserAction { implicit context: AnyUserContext => WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Doing " + action + "to " + id + " of kind " + kind + " not finished."))
    }}
    */
}
