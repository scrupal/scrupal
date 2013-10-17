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

import play.api.mvc._
import scrupal.views.html
import scrupal.utils.Pluralizer
import play.api.libs.json.JsString
import scrupal.api.{Modules, Types, Type, Module}

/** One line sentence description here.
  * Further description here.
  */
object APIDoc extends ScrupalController  {

  /** Provide an introduction to the API */
  def introduction() = Action { implicit request: RequestHeader =>
    Ok(html.api.introduction(modules, types))
  }

  def fetchAll(kind: String) = Action { implicit request: RequestHeader =>
    val (singular, description, module) = kind.toLowerCase() match {
      case "sites" =>    ("Site", "A site that Scrupal is configured to serve", "Core")
      case "modules" =>  ("Module", "A Scrupal Plug-in that extends it's functionality", "Core")
      case "entities" => ("Entity", "The kinds of entities that Scrupal is configured to serve", "Core")
      case "traits" =>   ("Trait", "The traits of entities that Scrupal is configured to serve", "Core")
      case "types" =>    ("Type", "The data types that Scrupal is configured to serve", "Core" )
      case _ =>          (kind, "No further description available.", "Unknown")
    }

    Ok(html.api.fetchAll(singular, Pluralizer.pluralize(singular), description, module))
  }

  def fetch(kind: String, id: String) = Action { implicit request: RequestHeader =>
    kind.toLowerCase() match {
      case "type" =>  Types(Symbol(id)) match {
        case t: Some[Type] => Ok(html.api.fetchType(t.get))
        case _ => notFound("Type " + id)
      }
      case "module" => Modules(Symbol(id)) match {
        case m: Some[Module] => Ok(html.api.fetchModule(m.get))
        case _ => notFound("Module " + id)
      }
      case _ =>   notImplemented("fetch of " + kind + " " + id )
    }
  }

  def createAll(kind: String) = Action { implicit request: RequestHeader =>
    notImplemented(JsString("Creation of " + kind))
  }

  def create(kind: String, id: String) = Action { implicit request: RequestHeader =>
    notImplemented(JsString("Creation of " + kind + " " + id))
  }

  def deleteAll(kind: String) = Action { implicit request: RequestHeader =>
    notImplemented(JsString("Deletion of all " + kind))
  }

  def delete(kind: String, id: String) = Action { implicit request: RequestHeader =>
    notImplemented(JsString("Deletion of " + kind + " " + id))
  }

  def updateAll(kind: String) = Action { implicit request: RequestHeader =>
    notImplemented(JsString("Update of " + kind ))
  }

  def update(kind: String, id: String) = Action { implicit request: RequestHeader =>
    notImplemented(JsString("Update of " + kind + " " + id))
  }

  def summarizeAll(kind: String) = Action { implicit request: RequestHeader =>
    notImplemented(JsString("Info for " + kind ))
  }

  def summarize(kind: String, id: String) = Action { implicit request: RequestHeader =>
    notImplemented(JsString("Info for " + id + " of kind " + kind))
  }

  def optionsOfAll(kind : String) = Action { implicit request: RequestHeader =>
    notImplemented(JsString("Options of " + kind ))
  }

  def optionsOf(kind: String, id: String) = Action { implicit request: RequestHeader =>
    notImplemented(JsString("Options of " + kind + " for " + id))
  }

  def doTo(kind: String, id: String, action: String) = Action { implicit request: RequestHeader =>
    notImplemented(JsString("Doing " + action + "to " + id + " of kind " + kind))
  }
}
