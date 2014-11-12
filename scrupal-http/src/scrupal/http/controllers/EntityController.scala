package scrupal.http.controllers

import scrupal.core.Scrupal
import scrupal.core.api._
import scrupal.http.directives.{PathHelpers, SiteDirectives}
import shapeless.{HNil}
import spray.http._
import spray.httpx.marshalling.{ToResponseMarshaller, BasicMarshallers}
import spray.routing.{Directives, Route}
import spray.routing._

import scala.concurrent.ExecutionContext

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
  extends Controller with Directives with SiteDirectives with PathHelpers with BasicMarshallers
{
  val context_path : String = id.name

  /** Mapping of Application to Entity By Path Segment
    * This just provides a shorter name than listing out the map of map of entity. This is how entity paths
    * are mapped. The outer map contains the application context path element. The inner map contains the name of
    * the type of the entity in both singular and plural forms. The intent is to match paths like
    * {{{/application/entity}}} and locate the correct entity to forward the request to.
    */
  type AppEntityMap = Map[String,Map[String,Entity]]

  /** A pure function to convert a site into a mapping of the entities that it supports
    *
    * @return The map of entity names to entity instances
    */
  def getAppEntities : AppEntityMap = {
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

  val appEntities : AppEntityMap = getAppEntities

  type AppEntityList = shapeless.::[Application,shapeless.::[Entity,HNil]]


  def scrupal_entity : Directive[AppEntityList] = new Directive[AppEntityList] {
    def happly(f: AppEntityList ⇒ Route) = {
      directories(appEntities) {
        case (appName, entities) ⇒ {
          directories(entities) {
            case (entityName, entity) ⇒ {
              Application.forPath(appName).map { app ⇒
                f(app :: entity :: HNil)
              }.getOrElse {
                reject(ValidationRejection(s"No application found matching '$appName'"))
              }
            }
          }
        }
      }
    }
  }

  val html_ct = ContentType(MediaTypes.`text/html`,HttpCharsets.`UTF-8`)
  val text_ct = ContentType(MediaTypes.`text/plain`,HttpCharsets.`UTF-8`)

  def html_marshaller : ToResponseMarshaller[HTMLResult] = {
    ToResponseMarshaller.delegate[HTMLResult,String](html_ct) { h ⇒ h.payload.body }
  }

  def text_marshaller : ToResponseMarshaller[TextResult] = {
    ToResponseMarshaller.delegate[TextResult,String](text_ct) { h ⇒ h.payload }
  }

  implicit val mystery_marshaller: ToResponseMarshaller[Result[_]] = {
    ToResponseMarshaller.delegate[Result[_], String](text_ct, html_ct) { (r : Result[_], ct) ⇒
      r match {
        case h: HTMLResult ⇒ h.payload.body ;
        case t: TextResult ⇒ t.payload
      }
    }
  }

  def request_context = extract( rc ⇒ rc )

  def routes(scrupal: Scrupal) : Route = {
    scrupal.withExecutionContext { implicit ec: ExecutionContext ⇒
      site { aSite ⇒
        validate(aSite == theSite, s"Expected site ${theSite.name } but got ${aSite.name }") {
          get {
            scrupal_entity {
              case (app: Application, entity: Entity) ⇒ {
                path(".*".r ~ PathEnd) { id: String ⇒
                  request_context { rc: RequestContext ⇒
                    val ctxt = Context(scrupal, aSite, rc, app, entity)
                    complete(entity.retrieve(id, ctxt))
                  }
                } ~
                reject(ValidationRejection(s"Unacceptable path"))
              }
            }
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
            } ~
            reject
        }
      }
    }
  }

  def entity_route(entity: Entity) : Route = {
    get {
      pathPrefix("echo") {
        complete("This is echo")
      }
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
