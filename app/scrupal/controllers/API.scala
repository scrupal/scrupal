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
import scrupal.views.html
import play.api.templates.Html
import play.api.libs.json.{JsValue, JsString, Json}
import scrupal.api.{Types, Modules, Type, Module}

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
      case _ => {        NotFound  }
    }
  }

  def fetch(kind: String, id: String) = Action { implicit request =>
    kind.toLowerCase() match {
      case "site" =>    Ok(Json.obj( "name" -> id, "description" -> JsString("Description of " + id )))
      case "module" =>   {
        Modules(Symbol(id)) match {
          case Some(m:Module) => Ok(m.toJson)
          case _ => NotFound(Json.obj())
        }
      }
      case "entity" => Ok(Json.obj( "name" -> id, "description" -> JsString("Description of " + id )))
      case "bundle" => Ok(Json.obj( "name" -> id))
      case "trait" =>   Ok(Json.obj( "name" -> id, "description" -> JsString("Description of " + id )))
      case "type" =>    Types(Symbol(id)) match {
        case Some(t:Type) => Ok(t.toJson)
        case _ => notFound(JsString("type " + id))
      }
      case _ => {        notFound(JsString(kind + " " + id))  }
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

  def doTo(kind: String, id: String, action: String) = Action { implicit request =>
    notImplemented(JsString("Doing " + action + "to " + id + " of kind " + kind))
  }

}
