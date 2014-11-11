package scrupal.http.controllers

import scrupal.core.Scrupal
import scrupal.core.api.{Identifier, Module, Entity, Site}
import scrupal.http.directives.{PathHelpers, SiteDirectives}
import shapeless.HNil
import spray.routing.{Directives, Route}
import spray.routing._
import spray.routing.Directives._

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
case class EntityController(id: Identifier, priority: Int, theSite: Site)
  extends Controller with Directives with SiteDirectives with PathHelpers
{
  val context_path : String = id.name

  /** Mapping of Site to Application to Entity
    * This just provides a shorter name than listing out the map of map of map of entity. This is how entity paths
    * are mapped. The outer map contains the application context path element. The inner map contains the name of
    * the type of the entity in both singular and plural forms. The intent is to match paths like
    * {{{/application/entity}}} and locate the correct entity to forward the request to.
    */
  type SiteAppEntityMap = Map[String,Map[String,Entity]]

  /** A pure function to convert a site into a mapping of the entities that it supports
    *
    * @return The map of entity names to entity instances
    */
  def getAppEntities : SiteAppEntityMap = {
    if (theSite.isEnabled) {
      for (app ← theSite.applications if app.isEnabled) yield {
        app.path -> {
          for (
            mod ← app.modules if mod.isEnabled ;
            entity ← mod.entities if entity.isEnabled ;
            name ← Seq(entity.path, entity.plural_path)
          ) yield {
            name → entity
          }
        }.toMap
      }
    }.toMap else {
      Map.empty
    }
  }

  val appEntities : SiteAppEntityMap = getAppEntities
/*
  def entity : Directive[String :: String :: Entity :: HNil] = {
    directories(appEntities) {
      case (app, entities) ⇒ {
        directories(entities) {
          case (name, entity) ⇒ {
            hprovide(app :: name :: entity :: HNil )
          }
        }
      }
    }
  }
*/
  def routes(scrupal: Scrupal) : Route = {
    site { aSite ⇒
      validate(aSite == theSite, s"Expected site ${theSite.name} but got ${aSite.name}") {
        get {
          complete("get")
        } ~
          put {
            complete("put")
          } ~
          post {
            complete("post")
          } ~
          delete {
            complete("delete")
          } ~
          options {
            complete("options")
          }
      }
    }
  }

  /*
# Provide A REST based API For Scrupal Entities
GET            /api/:kind/:id/:what                   scrupal.controllers.API.get(kind, id, what)
PUT            /api/:kind/:id/:what                   scrupal.controllers.API.put(kind, id, what)

GET            /api/:kind/:id                         scrupal.controllers.API.fetch(kind, id)
POST           /api/:kind/:id                         scrupal.controllers.API.create(kind,id)
PUT            /api/:kind/:id                         scrupal.controllers.API.update(kind, id)
DELETE         /api/:kind/:id                         scrupal.controllers.API.delete(kind, id)
OPTIONS        /api/:kind/:id                         scrupal.controllers.API.optionsOf(kind, id)

GET            /api/:kind                             scrupal.controllers.API.fetchAll(kind)
POST           /api/:kind                             scrupal.controllers.API.createAll(kind)
PUT            /api/:kind                             scrupal.controllers.API.updateAll(kind)
DELETE         /api/:kind                             scrupal.controllers.API.deleteAll(kind)
OPTIONS        /api/:kind                             scrupal.controllers.API.optionsOfAll(kind)

   */
}
