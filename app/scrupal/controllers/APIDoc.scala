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

import scrupal.views.html
import scrupal.utils.Pluralizer
import scrupal.api._
import scrupal.models.CoreFeatures
import scala.Some
import play.api.libs.json.JsString

/** One line sentence description here.
  * Further description here.
  */
object APIDoc extends ScrupalController  {

  val apidocs = CoreFeatures.RESTAPIDocumentation

  /** Provide an introduction to the API */
  def introduction() = UserAction { implicit context: AnyUserContext => WithFeature(apidocs) {
      Ok(html.api.introduction(modules, types))
    }
  }

  def fetchAll(kind: String) = UserAction { implicit context: AnyUserContext =>  WithFeature(apidocs) {
    val (singular, description, module) = kind.toLowerCase() match {
      case "sites" =>    ("Site", "A site that Scrupal is configured to serve", "Core")
      case "modules" =>  ("Module", "A Scrupal Plug-in that extends it's functionality", "Core")
      case "entities" => ("Entity", "The kinds of entities that Scrupal is configured to serve", "Core")
      case "types" =>    ("Type", "The data types that Scrupal is configured to serve", "Core" )
      case _ =>          (kind, "No further description available.", "Unknown")
    }

    Ok(html.api.fetchAll(singular, Pluralizer.pluralize(singular), description, module))
  }}

  def fetch(kind: String, id: String) = UserAction { implicit context: AnyUserContext => WithFeature(apidocs) {
    kind.toLowerCase match {
      case "type" =>  Type(Symbol(id)) match {
        case t: Some[Type] => Ok(html.api.fetchType(t.get))
        case _ => NotFound("Type " + id, Seq("You mis-typed '" + id + "'?"))
      }
      case "module" => Module(Symbol(id)) match {
        case m: Some[Module] => Ok(html.api.fetchModule(m.get))
        case _ => NotFound("Module " + id, Seq("You mis-typed '" + id + "'?"))
      }
      case "site" => Site(Symbol(id)) match {
        case s: Some[Site] => Ok(html.api.fetchSite(s.get))
        case _ => NotFound("Site " + id, Seq("You mis-typed '" + id + "'?"))
      }
      case "entity" => Entity(Symbol(id)) match {
        case e: Some[Entity] => Ok(html.api.fetchEntity(e.get))
        case _ => NotFound("Entity " + id, Seq())
      }
      case _ => Entity(Symbol(kind)) match {
        case e: Some[Entity] => {
          val lid = id.toLong
          context.schema.Instances.fetch(lid) map { instance: Instance =>
            Ok(html.api.fetchInstance(lid, instance))
          }
        } getOrElse {
          NotFound( "the " + kind + " " + id, Seq("the " + kind + " has not been created yet?"))
        }
        case _ => NotFound("fetch of " + kind + " " + id, Seq("You mis-typed '" + kind + "'?") )
      }
    }
  }}

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
}
