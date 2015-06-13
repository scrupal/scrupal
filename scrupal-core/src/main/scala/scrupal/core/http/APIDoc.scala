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

import scrupal.core.api.Scrupal
import spray.routing._

/** One line sentence description here.
  * Further description here.
  */
case class APIDoc(id : Symbol, priority : Int) extends Controller {
  def routes(implicit scrupal : Scrupal) : Route = reject

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
    def introduction() = UserAction { implicit context: AnyUserContext ⇒
      WithFeature(apidocs) {
        Ok(html.api.introduction(modules, types))
      }
    }

    def fetchAll(kind: String) = UserAction { implicit context: AnyUserContext ⇒
      WithFeature(apidocs) {
        val (singular, description, module) = kind.toLowerCase() match {
          case "sites" ⇒    ("site", "A site that Scrupal is configured to serve", "Core")
          case "modules" ⇒  ("module", "A Scrupal Plug-in that extends it's functionality", "Core")
          case "entities" ⇒ ("entity", "The kinds of entities that Scrupal is configured to serve", "Core")
          case "types" ⇒    ("type", "The data types that Scrupal is configured to serve", "Core" )
          case "instances" ⇒ ("instance", "The entity instances Scrupl is configured to serve", "Core")
          case _ ⇒          (kind, "No further description available.", "Unknown")
        }

        Ok(html.api.fetchAll(singular, Pluralizer.pluralize(singular), description, module))
      }
    }

    def fetch(kind: String, id: String) = UserAction.async { implicit context: AnyUserContext ⇒
      AsyncWithFeature(apidocs) {
        kind.toLowerCase match {
          case "type" ⇒  Future {
            Type(Symbol(id)) match {
              case t: Some[Type] ⇒ Ok(html.api.fetchType(t.get))
              case _ ⇒ NotFound("Type " + id, Seq("You mis-typed '" + id + "'?"))
            }
          }
          case "module" ⇒ Future {
            Module(Symbol(id)) match {
              case m: Some[Module] ⇒ Ok(html.api.fetchModule(m.get))
              case _ ⇒ NotFound("Module " + id, Seq("You mis-typed '" + id + "'?"))
            }
          }
          case "site" ⇒ Future {
            Site(Symbol(id)) match {
              case s: Some[Site] ⇒ Ok(html.api.fetchSite(s.get))
              case _ ⇒ NotFound("Site " + id, Seq("You mis-typed '" + id + "'?"))
            }
          }
          case "entity" ⇒ Future {
            Entity(Symbol(id)) match {
              case e: Some[Entity] ⇒ Ok(html.api.fetchEntity(e.get))
              case _ ⇒ NotFound("Entity " + id, Seq())
            }
          }
          case _ ⇒ Entity(Symbol(kind)) match {
            case e: Some[Entity] ⇒ {
              context.schema.instances.fetch(Symbol(id)) map { os: Option[Instance]⇒
                os match {
                  case Some(instance) ⇒ Ok(html.api.fetchInstance(Symbol(id), instance))
                  case None ⇒ NotFound("the " + kind + " " + id, Seq("the " + kind + " was not found"))
                }
              }
            }
            case _ ⇒ Future { NotFound("fetch of " + kind + " " + id, Seq("You mis-typed '" + kind + "'?") ) }
          }
        }
      }
    }

    def createAll(kind: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Creation of " + kind + " not finished."))
    }}

    def create(kind: String, id: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Creation of " + kind + " " + id + " not finished."))
    }}

    def deleteAll(kind: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Deletion of all " + kind + " not finished."))
    }}

    def delete(kind: String, id: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Deletion of " + kind + " " + id + " not finished."))
    }}

    def updateAll(kind: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Update of " + kind + " not finished."))
    }}

    def update(kind: String, id: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Update of " + kind + " " + id + " not finished."))
    }}

    def summarizeAll(kind: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Info for " + kind + " not finished."))
    }}

    def summarize(kind: String, id: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Info for " + id + " of kind " + kind + " not finished."))
    }}

    def optionsOfAll(kind : String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Options of " + kind + " not finished."))
    }}

    def optionsOf(kind: String, id: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Options of " + kind + " for " + id + " not finished."))
    }}

    def doTo(kind: String, id: String, action: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Doing " + action + "to " + id + " of kind " + kind + " not finished."))
    }}
    */
}
