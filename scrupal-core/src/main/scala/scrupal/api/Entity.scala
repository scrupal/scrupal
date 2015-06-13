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

package scrupal.api

import akka.http.scaladsl.model.{ HttpHeader, HttpMethods }
import akka.http.scaladsl.server.PathMatcher.{ Unmatched, Matched }
import akka.http.scaladsl.server.PathMatchers._
import akka.http.scaladsl.server.RequestContext

import play.api.libs.json._

import scrupal.api.types.BundleType
import scrupal.storage.api.Storable
import scrupal.utils.{ Registrable, Enablee }
import scrupal.utils.Validation._

import scala.concurrent.Future

trait EntityCollectionAction extends Action with Nameable with Describable {
  def id : String
}

trait EntityInstanceAction extends Action with Nameable with Describable {
  def what : Seq[String]
}

/** Provider of EntityCommands for command parameters
  * This
  * TODO: Finish documenting EntityCommandProvider interface as it is necessary to understanding entities
  */
trait EntityActionProvider extends PluralTerminalActionProvider {

  def make_args(ctxt : Context) : JsObject = {
    val headers = ctxt.request.request.headers.map { hdr : HttpHeader ⇒ hdr.name → JsString(hdr.value) }
    val params = ctxt.request.request.uri.query.map { case (k, v) ⇒ k -> JsString(v) }
    JsObject(headers ++ params)
  }

  override def provideAction(matchingSegment : String, context : Context) : Option[Action] = {
    val pm = Segments ~ PathEnd
    pm(context.request.unmatchedPath) match {
      case Matched(pathRest, extractions) ⇒
        if (matchingSegment == pluralKey) {
          val id_path : String = extractions._1.mkString("/")
          context.request.request.method match {
            case HttpMethods.POST    ⇒ Some(create(context, id_path, make_args(context)))
            case HttpMethods.GET     ⇒ Some(retrieve(context, id_path))
            case HttpMethods.PUT     ⇒ Some(update(context, id_path, make_args(context)))
            case HttpMethods.DELETE  ⇒ Some(delete(context, id_path))
            case HttpMethods.OPTIONS ⇒ Some(query(context, id_path, make_args(context)))
          }
        } else if (matchingSegment == singularKey) {
          val id_path = extractions._1
          context.request.request.method match {
            case HttpMethods.POST    ⇒ Some(createFacet(context, id_path, make_args(context)))
            case HttpMethods.GET     ⇒ Some(retrieveFacet(context, id_path))
            case HttpMethods.PUT     ⇒ Some(updateFacet(context, id_path, make_args(context)))
            case HttpMethods.DELETE  ⇒ Some(deleteFacet(context, id_path))
            case HttpMethods.OPTIONS ⇒ Some(queryFacet(context, id_path, make_args(context)))
          }
        } else
          None
      case Unmatched ⇒ None
    }
  }

  /** CREATE ENTITY (POST/plural) - Create a new entity from scratch
    * This is a command on the entity type's container to insert a new entity. The instance should be created
    * with the provided identifier, if possible. If not, respond with an error.
    * @param context The context from which the invocation is being made
    * @param id The unique identifier for new entity instance
    * @param instance The content of the entity (whcih will be validated against the entitie's bundle type)
    * @return A Create object that can perform the task from an Actor (i.e. in an isolated context)
    */
  def create(context : Context, id : String, instance : JsObject) : Create

  /** Retrieve Command (GET/plural) - Retrieve an existing entity by its identifier
    * This is a command on the entity type's contain to retrieve a specific entity. The full instance should be
    * retrieved, including all retrievable facets.
    * @param context The context from which the invocation is being made
    * @param id The unique identifier for new entity instance
    * @return
    */
  def retrieve(context : Context, id : String) : Retrieve

  /** Update Command (PUT/plural) - Updates all or a few of the fields of an entity
    * @param id
    * @param fields
    * @return
    */
  def update(context : Context, id : String, fields : JsObject) : Update

  /** Delete Command (DELETE/plural) */
  /** Delete an entity
    * @param id
    * @return
    */
  def delete(context : Context, id : String) : Delete

  /** Query Command (OPTIONS/plural) */
  def query(context : Context, id : String, fields : JsObject) : Query

  /** Create Facet Command (POST/singular) */
  def createFacet(context : Context, what : Seq[String], args : JsObject) : CreateFacet

  /** RetrieveAspect Command (GET/singular) */
  def retrieveFacet(context : Context, what : Seq[String]) : RetrieveFacet

  /** UpdateAspect Command (PUT/singular) */
  def updateFacet(context : Context, what : Seq[String], args : JsObject) : UpdateFacet

  /** XXX Command (DELETE/singular) */
  def deleteFacet(context : Context, what : Seq[String]) : DeleteFacet

  /** XXX Command (OPTIONS/singular) */
  def queryFacet(context : Context, what : Seq[String], args : JsObject) : QueryFacet
}

abstract class Create(val context : Context, val id : String, val instance : JsObject, val name : String = "Create")
  extends EntityCollectionAction {
  val description = "Create a specific instance of the entity and insert it in the entity collection."
}

abstract class Retrieve(val context : Context, val id : String, val name : String = "Retrieve")
  extends EntityCollectionAction {
  val description = "Retrieve a specific instance of the entity from the entity collection."
}

abstract class Update(val context : Context, val id : String, fields : JsObject, val name : String = "Retrieve")
  extends EntityCollectionAction {
  val description = "Update a specific instance of the entity."
}

abstract class Delete(val context : Context, val id : String, val name : String = "Delete")
  extends EntityCollectionAction {
  val description = "Delete a specific instance from the entity collection."
}

abstract class Query(val context : Context, val id : String, fields : JsObject, val name : String = "Query")
  extends EntityCollectionAction {
  val description = "Query the entity collection for an entity of a certain id or containing certain data."
}

abstract class CreateFacet(val context : Context, val what : Seq[String], val args : JsObject,
  val name : String = "CreateFacet")
  extends EntityInstanceAction {
  val description = "Create a facet on a specific entity in the collection."
}

abstract class RetrieveFacet(val context : Context, val what : Seq[String], val name : String = "RetrieveFacet")
  extends EntityInstanceAction {
  val description = "Retrieve a facet from a specific entity in the collection."
}

abstract class UpdateFacet(val context : Context, val what : Seq[String], val args : JsObject,
  val name : String = "UpdateFacet")
  extends EntityInstanceAction {
  val description = "Update a facet from a specific entity in the collection."
}

abstract class DeleteFacet(val context : Context, val what : Seq[String], val name : String = "DeleteFacet")
  extends EntityInstanceAction {
  val description = "Delete a facet from a specific entity in the collection."
}

abstract class QueryFacet(val context : Context, val what : Seq[String], val args : JsObject,
  val name : String = "QueryFacet")
  extends EntityInstanceAction {
  val description = "Query a specific entity in the collection for a facet of a certain id or containing certain data."
}

/** The fundamental unit of storage, behavior and interaction in Scrupal.
  *
  * An Entity brings together several things:The BundleType of an Instance's  payload,
  * definitions of the RESTful methods, security constraints, and extension actions for the REST API.
  * This is the key abstraction for Modules. Entities have a public REST API that is served by Scrupal. Entities
  * should represent some concept that is stored by Scrupal and delivered to the user interface via the REST API.
  */
abstract class Entity(sym : Symbol) extends {
  val id : Symbol = sym; val _id : Symbol = sym; val segment : String = id.name
} with EntityActionProvider with Storable with Registrable[Entity] with ModuleOwned
  with Describable with Enablee with Bootstrappable {
  def moduleOf = { Module.values.find(mod ⇒ mod.entities.contains(this)) }

  override def parent = moduleOf

  def instanceType : BundleType[Any]

  def registry = Entity

  /*
  def toMap(mt : Map[String,Any]) : Map[String, Any] = mt

  def validateElement(ref : SelectedLocation, k: String, v : Any) : Results[Any] = {

  }

  override def validate(ref : Location, value : Map[String,Any] ) : VResult = instanceType.validate(ref, value)
*/

  def create(context : Context, id : String, instance : JsObject) : Create = NoopCreate(context, id, instance)

  def retrieve(context : Context, id : String) : Retrieve = NoopRetrieve(context, id)

  def update(context : Context, id : String, fields : JsObject) : Update = NoopUpdate(context, id, fields)

  def delete(context : Context, id : String) : Delete = NoopDelete(context, id)

  def query(context : Context, id : String, fields : JsObject) : Query = NoopQuery(context, id, fields)

  def createFacet(context : Context, what : Seq[String], args : JsObject) : CreateFacet = NoopCreateFacet(context, what, args)

  def retrieveFacet(context : Context, what : Seq[String]) : RetrieveFacet = NoopRetrieveFacet(context, what)

  def updateFacet(context : Context, what : Seq[String], args : JsObject) : UpdateFacet = NoopUpdateFacet(context, what, args)

  def deleteFacet(context : Context, what : Seq[String]) : DeleteFacet = NoopDeleteFacet(context, what)

  def queryFacet(context : Context, what : Seq[String], args : JsObject) : QueryFacet = NoopQueryFacet(context, what, args)

}

trait NoopAction extends Describable {
  def apply : Future[Result[_]] = Future.successful(JsonResult(emptyJsObject, Indeterminate))
  override val description = "Noop"
}

case class NoopCreate(
  override val context : Context,
  override val id : String,
  override val instance : JsObject) extends Create(context, id, instance) with NoopAction

case class NoopRetrieve(
  override val context : Context,
  override val id : String) extends Retrieve(context, id) with NoopAction

case class NoopUpdate(
  override val context : Context,
  override val id : String, fields : JsObject) extends Update(context, id, fields) with NoopAction

case class NoopDelete(
  override val context : Context,
  override val id : String) extends Delete(context, id) with NoopAction

case class NoopQuery(
  override val context : Context,
  override val id : String, fields : JsObject) extends Query(context, id, fields) with NoopAction

case class NoopCreateFacet(
  override val context : Context,
  override val what : Seq[String],
  override val args : JsObject) extends CreateFacet(context, what, args) with NoopAction

case class NoopRetrieveFacet(
  override val context : Context,
  override val what : Seq[String]) extends RetrieveFacet(context, what) with NoopAction

case class NoopUpdateFacet(
  override val context : Context,
  override val what : Seq[String],
  override val args : JsObject) extends UpdateFacet(context, what, args) with NoopAction

case class NoopDeleteFacet(
  override val context : Context,
  override val what : Seq[String]) extends DeleteFacet(context, what) with NoopAction

case class NoopQueryFacet(
  override val context : Context,
  override val what : Seq[String],
  override val args : JsObject) extends QueryFacet(context, what, args) with NoopAction

object Entity extends scrupal.utils.Registry[Entity] {
  val registrantsName : String = "entity"
  val registryName : String = "Entities"

  /*
  import BSONHandlers._

  case class EntityDao(db : ScrupalDB) extends IdentifierDAO[Entity] {
    implicit val reader : Reader = EntityHandler.asInstanceOf[Reader]
    implicit val writer : Writer = EntityHandler.asInstanceOf[Writer]

    def collectionName : String = "entities"
  }
*/
}

