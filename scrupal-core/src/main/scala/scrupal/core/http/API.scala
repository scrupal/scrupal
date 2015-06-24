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

import akka.http.scaladsl.server.Route
import scrupal.api.Scrupal
import scrupal.core.http.akkahttp.Controller

/** The Controller For The Scrupal JSON API
  * This controller handles all requests of the forms /api/... and /doc/api/... So that developers can both use and
  * read about the various APIs supported by their Scrupal Installation.
  */
case class API(id : Symbol, priority : Int) extends Controller {
  def routes(implicit scrupal : Scrupal) : Route = reject

  /*
  val feature = CoreFeatures.RESTAPIAccess

  def fetchAll(kind: String) = UserAction { implicit context: AnyUserContext ⇒
    WithFeature(feature) {
      kind.toLowerCase() match {
        case "sites" ⇒ Ok(Json.arr("scrupal.org", "scrupal.com"))
        case "modules" ⇒ Ok(Json.toJson(moduleNames))
        case "entities" ⇒ Ok(Json.arr("One", "Two"))
        case "types" ⇒ Ok(Json.toJson(typeNames))
        case "users" ⇒ Ok(Json.arr("One", "Two", "Three"))
        case _ ⇒ {
          NotFound(JsString(kind))
        }
      }
    }
  }

  /** Implement the HTTP GET method for an Entity.
    * This method retrieves a specific instance of a kind of entity.
    * @param kind
    * @param id
    * @return
    */
  def fetch(kind: String, id: String) = UserAction { implicit context: AnyUserContext ⇒
    WithFeature(feature) {
      kind.toLowerCase() match {
        case "site" ⇒ Ok(Json.obj("name" -> id, "description" -> JsString("Description of " + id)))
        case "module" ⇒ {
          Module(Symbol(id)) match {
            case Some(m: Module) ⇒ Ok(m.toJson)
            case _ ⇒ NotFound(Json.obj())
          }
        }
        case "entity" ⇒ Ok(Json.obj("name" -> id, "description" -> JsString("Description of " + id)))
        case "bundle" ⇒ Ok(Json.obj("name" -> id))
        case "trait" ⇒ Ok(Json.obj("name" -> id, "description" -> JsString("Description of " + id)))
        case "type" ⇒ Type(Symbol(id)) match {
          case Some(t: Type) ⇒ Ok(t.toJson)
          case _ ⇒ NotFound(JsString("type " + id))
        }
        case _ ⇒ {
          NotFound(JsString(kind + " " + id))
        }
      }
    }
  }

  def createAll(kind: String) = UserAction { implicit context: AnyUserContext ⇒
    WithFeature(feature) {
      kind.toLowerCase() match {
        case "sites" ⇒ NotImplemented(JsString("Creation of " + kind))
        case "modules" ⇒ NotImplemented(JsString("Creation of " + kind))
        case "entities" ⇒ NotImplemented(JsString("Creation of " + kind))
        case "traits" ⇒ NotImplemented(JsString("Creation of " + kind))
        case "types" ⇒ NotImplemented(JsString("Creation of " + kind))
        case _ ⇒ NotImplemented(JsString("Creation of " + kind))
      }
    }
  }

  def create(kind: String, id: String) = UserAction { implicit context: AnyUserContext ⇒
    WithFeature(feature) {
      kind.toLowerCase() match {
        case "sites" ⇒ NotImplemented(JsString("Creation of " + kind + " " + id))
        case "modules" ⇒ NotImplemented(JsString("Creation of " + kind + " " + id))
        case "entities" ⇒ NotImplemented(JsString("Creation of " + kind + " " + id))
        case "traits" ⇒ NotImplemented(JsString("Creation of " + kind + " " + id))
        case "types" ⇒ NotImplemented(JsString("Creation of " + kind + " " + id))
        case _ ⇒ {
          NotImplemented(JsString("Creation of " + kind + " " + id))
        }
      }
    }
  }

  def deleteAll(kind: String) = UserAction { implicit request: AnyUserContext ⇒
    WithFeature(feature) {
      kind.toLowerCase() match {
        case "sites" ⇒ NotImplemented(JsString("Deletion of all " + kind))
        case "modules" ⇒ NotImplemented(JsString("Deletion of all " + kind))
        case "entities" ⇒ NotImplemented(JsString("Deletion of all " + kind))
        case "traits" ⇒ NotImplemented(JsString("Deletion of all " + kind))
        case "types" ⇒ NotImplemented(JsString("Deletion of all " + kind))
        case _ ⇒ {
          NotImplemented(JsString("Deletion of all " + kind))
        }
      }
    }
  }

  def delete(kind: String, id: String) = UserAction { implicit context: AnyUserContext ⇒
    kind.toLowerCase() match {
      case "sites" ⇒    NotImplemented(JsString("Deletion of " + kind + " " + id))
      case "modules" ⇒  NotImplemented(JsString("Deletion of " + kind + " " + id))
      case "entities" ⇒ NotImplemented(JsString("Deletion of " + kind + " " + id))
      case "traits" ⇒   NotImplemented(JsString("Deletion of " + kind + " " + id))
      case "types" ⇒    NotImplemented(JsString("Deletion of " + kind + " " + id))
      case _ ⇒ {        NotImplemented(JsString("Deletion of " + kind + " " + id))  }
    }
  }


  def updateAll(kind: String) = UserAction { implicit context: AnyUserContext ⇒
    kind.toLowerCase() match {
      case "sites" ⇒    NotImplemented(JsString("Update of " + kind ))
      case "modules" ⇒  NotImplemented(JsString("Update of " + kind ))
      case "entities" ⇒ NotImplemented(JsString("Update of " + kind ))
      case "traits" ⇒   NotImplemented(JsString("Update of " + kind ))
      case "types" ⇒    NotImplemented(JsString("Update of " + kind ))
      case _ ⇒ {        NotImplemented(JsString("Update of " + kind ))  }
    }
  }

  def update(kind: String, id: String) = UserAction { implicit context: AnyUserContext ⇒
    kind.toLowerCase() match {
      case "sites" ⇒    NotImplemented(JsString("Update of " + kind + " " + id))
      case "modules" ⇒  NotImplemented(JsString("Update of " + kind + " " + id))
      case "entities" ⇒ NotImplemented(JsString("Update of " + kind + " " + id))
      case "traits" ⇒   NotImplemented(JsString("Update of " + kind + " " + id))
      case "types" ⇒    NotImplemented(JsString("Update of " + kind + " " + id))
      case _ ⇒ {        NotImplemented(JsString("Update of " + kind + " " + id))  }
    }
  }

  def summarizeAll(kind: String) = UserAction { implicit context: AnyUserContext ⇒
    kind.toLowerCase() match {
      case "sites" ⇒    NotImplemented(JsString("Info for " + kind ))
      case "modules" ⇒  NotImplemented(JsString("Info for " + kind ))
      case "entities" ⇒ NotImplemented(JsString("Info for " + kind ))
      case "traits" ⇒   NotImplemented(JsString("Info for " + kind ))
      case "types" ⇒    NotImplemented(JsString("Info for " + kind ))
      case _ ⇒ {        NotImplemented(JsString("Info for " + kind ))  }
    }
  }

  def summarize(kind: String, id: String) = UserAction { implicit context: AnyUserContext ⇒
    kind.toLowerCase() match {
      case "sites" ⇒    NotImplemented(JsString("Info for " + id + " of kind " + kind))
      case "modules" ⇒  NotImplemented(JsString("Info for " + id + " of kind " + kind))
      case "entities" ⇒ NotImplemented(JsString("Info for " + id + " of kind " + kind))
      case "traits" ⇒   NotImplemented(JsString("Info for " + id + " of kind " + kind))
      case "types" ⇒    NotImplemented(JsString("Info for " + id + " of kind " + kind))
      case _ ⇒ {        NotImplemented(JsString("Info for " + id + " of kind " + kind))  }
    }
  }

  /** Implements the OPTIONS HTTP Method for an entity or Scrupal in its entirety.
    * @see [[http://greenbytes.de/tech/webdav/rfc2616.html#OPTIONS RFC 2616 - 9.2 OPTIONS ]]
    * The OPTIONS method allows us to take some liberties. It says ''A 200 response should include any header fields
    * that indicate optional features implemented by the server and applicable to that resource (e.g., Allow),
    * possibly including extensions not defined by this specification.'' We utilize the **including extensions not
    * defined by this specification** part heavily here. Options provides complete access to meta information about
    * the entity.
    *
    * The request payload is expected to be a Json array that contains the names of the information fields that the
    * client is interested in. If the array is empty, all fields will be returned. Each field provides one aspect of
    * meta-information about the entity, for example, such things as the number of entities in the collection,
    * access rights, etc.
    *
    * Most of the processing for this request can be handled by this Controller,
    * but some help is needed for the fields that require entity help. The Entity method
    * `option(name:String, id: InstanceId)` will be called to fill in the blanks as needed by the request
   */
  def optionsOfAll(kind : String) = UserAction { implicit context: AnyUserContext ⇒
    kind.toLowerCase() match {
      case "sites" ⇒    NotImplemented(JsString("Options of " + kind ))
      case "modules" ⇒  NotImplemented(JsString("Options of " + kind ))
      case "entities" ⇒ NotImplemented(JsString("Options of " + kind ))
      case "traits" ⇒   NotImplemented(JsString("Options of " + kind ))
      case "types" ⇒    NotImplemented(JsString("Options of " + kind ))
      case _ ⇒ {        NotImplemented(JsString("Options of " + kind ))  }
    }
  }

  def optionsOf(kind: String, id: String) = UserAction { implicit context: AnyUserContext ⇒
    kind.toLowerCase() match {
      case "sites" ⇒    NotImplemented(JsString("Options of " + kind + " for " + id))
      case "modules" ⇒  NotImplemented(JsString("Options of " + kind + " for " + id))
      case "entities" ⇒ NotImplemented(JsString("Options of " + kind + " for " + id))
      case "traits" ⇒   NotImplemented(JsString("Options of " + kind + " for " + id))
      case "types" ⇒    NotImplemented(JsString("Options of " + kind + " for " + id))
      case _ ⇒ {        NotImplemented(JsString("Options of " + kind + " for " + id))  }
    }
  }

  /** The extension of fetching an entity. You can get any of its fields or even other information including the
    * result of doing some processing. This permits extension of the REST API above. These requests are forwarded to
    * the entity involved.
    * @param kind
    * @param id
    * @param what
    * @return
    */
  def get(kind: String, id: String, what: String) = UserAction { implicit context: AnyUserContext ⇒
    NotImplemented(JsString("Getting " + what + " from " + kind + " " + id ))
  }

  /** The extension of updating an entity. You can put any of its fields or even other information including
    * supporting complicated JSON based requests.
    * @param kind
    * @param id
    * @param what
    * @return
    */
  def put(kind: String, id: String, what: String) = UserAction { implicit context: AnyUserContext ⇒
    NotImplemented(JsString("Putting " + what + " to " + kind + " " + id ))
  }
*/
}
