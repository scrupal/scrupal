package scrupal.http.controllers

import scrupal.core.api.{Identifier, Module, Entity, Site}
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
case class EntityController(id: Identifier, priority: Int, site: Site) extends Controller with Directives {
  val context_path : String = id.name

  /** A pure function to convert a site into a mapping of the entities that it supports
    *
    * @param site The site to be mapped
    * @return The map of entity names to entity instances
    */
  def getEntities(site: Site) : Map[String,Entity] = {
    for (
      app ← site.applications ;
      mod ← Module.find(app.modules);
      entity ← mod.entities if entity.enabled
    )
    yield entity.label → entity
  }.toMap

  val entities = getEntities(site)

  def routes : Route = {
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
