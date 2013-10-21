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

package scrupal.controllers

import play.api.mvc.{RequestHeader, Action}
import play.api.libs.json.{JsValue, JsString, Json}
import scrupal.api.{Module, Type}

/** The Controller For The Scrupal JSON API
  * This controller handles all requests of the forms /api/... and /doc/api/... So that developers can both use and
  * read about the various APIs supported by their Scrupal Installation.
  */
object API extends ScrupalController  {

  def fetchAll(kind: String) = Action { implicit request =>
    kind.toLowerCase() match {
      case "sites" =>    Ok(Json.arr("scrupal.org", "scrupal.com"))
      case "modules" =>  Ok(Json.toJson(moduleNames))
      case "entities" => Ok(Json.arr("One", "Two"))
      case "types" =>    Ok(Json.toJson(typeNames))
      case "users" =>    Ok(Json.arr("One", "Two", "Three"))
      case _ => {        notFound(JsString(kind))  }
    }
  }

  /** Implement the HTTP GET method for an Entity.
    * This method retrieves a specific instance of a kind of entity.
    * @param kind
    * @param id
    * @return
    */
  def fetch(kind: String, id: String) = Action { implicit request =>
    kind.toLowerCase() match {
      case "site" =>    Ok(Json.obj( "name" -> id, "description" -> JsString("Description of " + id )))
      case "module" =>   {
        Module(Symbol(id)) match {
          case Some(m:Module) => Ok(m.toJson)
          case _ => NotFound(Json.obj())
        }
      }
      case "entity" => Ok(Json.obj( "name" -> id, "description" -> JsString("Description of " + id )))
      case "bundle" => Ok(Json.obj( "name" -> id))
      case "trait" =>   Ok(Json.obj( "name" -> id, "description" -> JsString("Description of " + id )))
      case "type" =>    Type(Symbol(id)) match {
        case Some(t:Type) => Ok(t.toJson)
        case _ => notFound(JsString("type " + id))
      }
      case _ => {        notFound(JsString(kind + " " + id)) }
    }
  }

  def createAll(kind: String) = Action { implicit request =>
    kind.toLowerCase() match {
      case "sites" =>    notImplemented(JsString("Creation of " + kind))
      case "modules" =>  notImplemented(JsString("Creation of " + kind))
      case "entities" => notImplemented(JsString("Creation of " + kind))
      case "traits" =>   notImplemented(JsString("Creation of " + kind))
      case "types" =>    notImplemented(JsString("Creation of " + kind))
      case _ => {        notImplemented(JsString("Creation of " + kind))  }
    }
  }

  def create(kind: String, id: String) = Action { implicit request =>
    kind.toLowerCase() match {
      case "sites" =>    notImplemented(JsString("Creation of " + kind + " " + id))
      case "modules" =>  notImplemented(JsString("Creation of " + kind + " " + id))
      case "entities" => notImplemented(JsString("Creation of " + kind + " " + id))
      case "traits" =>   notImplemented(JsString("Creation of " + kind + " " + id))
      case "types" =>    notImplemented(JsString("Creation of " + kind + " " + id))
      case _ => {        notImplemented(JsString("Creation of " + kind + " " + id))  }
    }
  }


  def deleteAll(kind: String) = Action { implicit requiest =>
    kind.toLowerCase() match {
      case "sites" =>    notImplemented(JsString("Deletion of all " + kind))
      case "modules" =>  notImplemented(JsString("Deletion of all " + kind))
      case "entities" => notImplemented(JsString("Deletion of all " + kind))
      case "traits" =>   notImplemented(JsString("Deletion of all " + kind))
      case "types" =>    notImplemented(JsString("Deletion of all " + kind))
      case _ => {        notImplemented(JsString("Deletion of all " + kind))  }
    }
  }

  def delete(kind: String, id: String) = Action { implicit request =>
    kind.toLowerCase() match {
      case "sites" =>    notImplemented(JsString("Deletion of " + kind + " " + id))
      case "modules" =>  notImplemented(JsString("Deletion of " + kind + " " + id))
      case "entities" => notImplemented(JsString("Deletion of " + kind + " " + id))
      case "traits" =>   notImplemented(JsString("Deletion of " + kind + " " + id))
      case "types" =>    notImplemented(JsString("Deletion of " + kind + " " + id))
      case _ => {        notImplemented(JsString("Deletion of " + kind + " " + id))  }
    }
  }


  def updateAll(kind: String) = Action { implicit request =>
    kind.toLowerCase() match {
      case "sites" =>    notImplemented(JsString("Update of " + kind ))
      case "modules" =>  notImplemented(JsString("Update of " + kind ))
      case "entities" => notImplemented(JsString("Update of " + kind ))
      case "traits" =>   notImplemented(JsString("Update of " + kind ))
      case "types" =>    notImplemented(JsString("Update of " + kind ))
      case _ => {        notImplemented(JsString("Update of " + kind ))  }
    }
  }

  def update(kind: String, id: String) = Action { implicit request =>
    kind.toLowerCase() match {
      case "sites" =>    notImplemented(JsString("Update of " + kind + " " + id))
      case "modules" =>  notImplemented(JsString("Update of " + kind + " " + id))
      case "entities" => notImplemented(JsString("Update of " + kind + " " + id))
      case "traits" =>   notImplemented(JsString("Update of " + kind + " " + id))
      case "types" =>    notImplemented(JsString("Update of " + kind + " " + id))
      case _ => {        notImplemented(JsString("Update of " + kind + " " + id))  }
    }
  }

  def summarizeAll(kind: String) = Action { implicit request =>
    kind.toLowerCase() match {
      case "sites" =>    notImplemented(JsString("Info for " + kind ))
      case "modules" =>  notImplemented(JsString("Info for " + kind ))
      case "entities" => notImplemented(JsString("Info for " + kind ))
      case "traits" =>   notImplemented(JsString("Info for " + kind ))
      case "types" =>    notImplemented(JsString("Info for " + kind ))
      case _ => {        notImplemented(JsString("Info for " + kind ))  }
    }
  }

  def summarize(kind: String, id: String) = Action { implicit request =>
    kind.toLowerCase() match {
      case "sites" =>    notImplemented(JsString("Info for " + id + " of kind " + kind))
      case "modules" =>  notImplemented(JsString("Info for " + id + " of kind " + kind))
      case "entities" => notImplemented(JsString("Info for " + id + " of kind " + kind))
      case "traits" =>   notImplemented(JsString("Info for " + id + " of kind " + kind))
      case "types" =>    notImplemented(JsString("Info for " + id + " of kind " + kind))
      case _ => {        notImplemented(JsString("Info for " + id + " of kind " + kind))  }
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
  def optionsOfAll(kind : String) = Action { implicit request =>
    kind.toLowerCase() match {
      case "sites" =>    notImplemented(JsString("Options of " + kind ))
      case "modules" =>  notImplemented(JsString("Options of " + kind ))
      case "entities" => notImplemented(JsString("Options of " + kind ))
      case "traits" =>   notImplemented(JsString("Options of " + kind ))
      case "types" =>    notImplemented(JsString("Options of " + kind ))
      case _ => {        notImplemented(JsString("Options of " + kind ))  }
    }
  }

  def optionsOf(kind: String, id: String) = Action { implicit request =>
    kind.toLowerCase() match {
      case "sites" =>    notImplemented(JsString("Options of " + kind + " for " + id))
      case "modules" =>  notImplemented(JsString("Options of " + kind + " for " + id))
      case "entities" => notImplemented(JsString("Options of " + kind + " for " + id))
      case "traits" =>   notImplemented(JsString("Options of " + kind + " for " + id))
      case "types" =>    notImplemented(JsString("Options of " + kind + " for " + id))
      case _ => {        notImplemented(JsString("Options of " + kind + " for " + id))  }
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
  def get(kind: String, id: String, what: String) = Action { implicit request =>
    notImplemented(JsString("Getting " + what + " from " + kind + " " + id ))
  }

  /** THe extension of updating an entity. You can put any of its fields or even other information including
    * supporting complicated JSON based requests.
    * @param kind
    * @param id
    * @param what
    * @return
    */
  def put(kind: String, id: String, what: String) = Action { implicit request =>
    notImplemented(JsString("Putting " + what + " to " + kind + " " + id ))
  }

}
