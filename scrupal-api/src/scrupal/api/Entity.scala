/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
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

package scrupal.api

import reactivemongo.bson._
import scrupal.api.types.BundleType
import scrupal.db.{VariantStorable, IdentifierDAO, ScrupalDB}

import scrupal.utils.{Registrable, Enablee}
import shapeless.HList
import spray.http.{HttpHeader, HttpMethods, Uri}
import spray.routing.PathMatcher.{Unmatched, Matched}
import spray.routing.PathMatchers._

import scala.concurrent.Future

trait EntityCollectionAction extends Action with Nameable with Describable {
  def id: String
}

trait EntityInstanceAction extends Action with Nameable with Describable {
  def what: Seq[String]
}

/** Provider of EntityCommands for command parameters
  * This
  * TODO: Finish documenting EntityCommandProvider interface as it is necessary to understanding entities
  */
trait EntityActionProvider extends TerminalActionProvider {

  def make_args(ctxt: Context) : BSONDocument = {
    ctxt.request match {
      case Some(context) ⇒
        val headers = context.request.headers.map { hdr : HttpHeader  ⇒ hdr.name → BSONString(hdr.value) }
        val params = context.request.uri.query.map { case(k,v) ⇒ k -> BSONString(v) }
        BSONDocument(headers ++ params)
      case None ⇒
        BSONDocument()
    }
  }

  def actionFor(key: String, path: Uri.Path, context: Context) : Option[Action] = {
    if (key == pluralKey) {
      val pm = (Segments ~PathEnd)
      pm(path) match {
        case Matched(pathRest, extractions) ⇒
          val id_path : String = extractions.head.mkString("/")
          context.request.get.request.method match {
            case HttpMethods.POST ⇒ Some(create(context, id_path, make_args(context)))
            case HttpMethods.GET ⇒ Some(retrieve(context, id_path))
            case HttpMethods.PUT ⇒ Some(update(context, id_path, make_args(context)))
            case HttpMethods.DELETE ⇒ Some(delete(context, id_path))
            case HttpMethods.OPTIONS ⇒ Some(query(context, id_path, make_args(context)))
          }
        case Unmatched ⇒ None
      }
    } else if (key == singularKey) {
      val pm = (Segments ~ PathEnd)
      pm(path) match {
        case Matched(pathRest, extractions) ⇒
          val id_path = extractions.head
          context.request.get.request.method match {
            case HttpMethods.POST ⇒ Some(createFacet(context, id_path, make_args(context)))
            case HttpMethods.GET ⇒ Some(retrieveFacet(context, id_path))
            case HttpMethods.PUT ⇒ Some(updateFacet(context, id_path, make_args(context)))
            case HttpMethods.DELETE ⇒ Some(deleteFacet(context, id_path))
            case HttpMethods.OPTIONS ⇒ Some(queryFacet(context, id_path, make_args(context)))
          }
        case Unmatched ⇒ None
      }
    } else {
      None
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
  def create(context: Context, id: String, instance: BSONDocument) : Create

  /** Retrieve Command (GET/plural) - Retrieve an existing entity by its identifier
    * This is a command on the entity type's contain to retrieve a specific entity. The full instance should be
    * retrieved, including all retrievable facets.
    * @param context The context from which the invocation is being made
    * @param id The unique identifier for new entity instance
    * @return
    */
  def retrieve(context: Context, id: String) : Retrieve

  /** Update Command (PUT/plural) - Updates all or a few of the fields of an entity
    * @param id
    * @param fields
    * @return
    */
  def update(context: Context, id: String, fields: BSONDocument) : Update

  /** Delete Command (DELETE/plural) */
  /** Delete an entity
    * @param id
    * @return
    */
  def delete(context: Context, id: String) : Delete

  /** Query Command (OPTIONS/plural) */
  def query(context: Context, id: String, fields: BSONDocument) : Query

  /** Create Facet Command (POST/singular) */
  def createFacet(context: Context, what: Seq[String], args: BSONDocument) : CreateFacet

  /** RetrieveAspect Command (GET/singular) */
  def retrieveFacet(context: Context, what: Seq[String]) : RetrieveFacet

  /** UpdateAspect Command (PUT/singular) */
  def updateFacet(context: Context, what: Seq[String], args: BSONDocument) : UpdateFacet

  /** XXX Command (DELETE/singular) */
  def deleteFacet(context: Context, what: Seq[String]) : DeleteFacet

  /** XXX Command (OPTIONS/singular) */
  def queryFacet(context: Context, what: Seq[String], args: BSONDocument) : QueryFacet
}

abstract class Create(val context: Context, val id: String, val instance: BSONDocument, val name: String = "Create")
  extends EntityCollectionAction {
  val description = "Create a specific instance of the entity and insert it in the entity collection."
}

abstract class Retrieve(val context: Context, val id: String, val name : String = "Retrieve")
  extends EntityCollectionAction {
  val description = "Retrieve a specific instance of the entity from the entity collection."
}

abstract class Update(val context: Context, val id: String, fields: BSONDocument, val name : String = "Retrieve")
  extends EntityCollectionAction {
  val description = "Update a specific instance of the entity."
}

abstract class Delete(val context: Context, val id: String, val name : String = "Delete")
  extends EntityCollectionAction {
  val description = "Delete a specific instance from the entity collection."
}

abstract class Query(val context: Context, val id: String, fields: BSONDocument, val name : String = "Query")
  extends EntityCollectionAction {
  val description = "Query the entity collection for an entity of a certain id or containing certain data."
}

abstract class CreateFacet(val context: Context, val what: Seq[String], val args: BSONDocument,
                           val name : String = "CreateFacet")
  extends EntityInstanceAction {
  val description = "Create a facet on a specific entity in the collection."
}

abstract class RetrieveFacet(val context: Context, val what: Seq[String], val name: String = "RetrieveFacet")
  extends EntityInstanceAction {
  val description = "Retrieve a facet from a specific entity in the collection."
}

abstract class UpdateFacet(val context: Context, val what: Seq[String], val args: BSONDocument,
                           val name: String = "UpdateFacet")
  extends EntityInstanceAction {
  val description = "Update a facet from a specific entity in the collection."
}

abstract class DeleteFacet(val context: Context, val what: Seq[String], val name: String = "DeleteFacet")
  extends EntityInstanceAction {
  val description = "Delete a facet from a specific entity in the collection."
}

abstract class QueryFacet(val context: Context, val what: Seq[String], val args: BSONDocument,
                          val name: String = "QueryFacet")
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
abstract class Entity(sym: Symbol) extends { val id: Symbol = sym; val _id: Symbol = sym} with EntityActionProvider
with
  VariantStorable[Symbol] with Registrable[Entity] with ModuleOwned
          with Describable with Enablee with BSONValidator[BSONDocument] with Bootstrappable
{
  def moduleOf = { Module.values.find(mod ⇒ mod.entities.contains(this)) }

  override def parent = moduleOf

  def instanceType: BundleType

  def registry = Entity

  def apply(value: BSONDocument) : ValidationResult = instanceType(value)

  def create(context: Context, id: String, instance: BSONDocument)
    : Create = NoopCreate(context, id, instance)

  def retrieve(context: Context, id: String)
    : Retrieve = NoopRetrieve(context, id)

  def update(context: Context, id: String, fields: BSONDocument)
    : Update = NoopUpdate(context, id, fields)

  def delete(context: Context, id: String)
    : Delete = NoopDelete(context, id)

  def query(context: Context, id: String, fields: BSONDocument)
    : Query = NoopQuery(context, id, fields)

  def createFacet(context: Context, what: Seq[String], args: BSONDocument)
    : CreateFacet = NoopCreateFacet(context, what, args)

  def retrieveFacet(context: Context, what: Seq[String])
    : RetrieveFacet = NoopRetrieveFacet(context, what)

  def updateFacet(context: Context, what: Seq[String], args: BSONDocument)
    : UpdateFacet = NoopUpdateFacet(context, what, args)

  def deleteFacet(context: Context, what: Seq[String])
    : DeleteFacet = NoopDeleteFacet(context, what)

  def queryFacet(context: Context, what: Seq[String], args: BSONDocument)
    : QueryFacet = NoopQueryFacet(context,what, args)

}

trait NoopAction extends Describable {
  def apply : Future[Result[_]] = Future.successful( BSONResult(BSONDocument(), Indeterminate) )
  override val description = "Noop"
}

case class NoopCreate(
  override val context: Context,
  override val id: String,
  override val instance: BSONDocument
) extends Create(context, id,  instance) with NoopAction

case class NoopRetrieve(
  override val context: Context,
  override val id: String) extends Retrieve(context, id) with NoopAction

case class NoopUpdate(
  override val context: Context,
  override val id: String, fields: BSONDocument) extends Update(context, id, fields) with NoopAction

case class NoopDelete(
  override val context: Context,
  override val id: String) extends Delete(context, id) with NoopAction

case class NoopQuery(
  override val context: Context,
  override val id: String, fields: BSONDocument) extends Query(context, id, fields) with NoopAction

case class NoopCreateFacet(
  override val context: Context,
  override val what: Seq[String],
  override val args: BSONDocument
) extends CreateFacet(context, what, args) with NoopAction

case class NoopRetrieveFacet(
  override val context: Context,
  override val what: Seq[String]
) extends RetrieveFacet(context, what) with NoopAction

case class NoopUpdateFacet(
  override val context: Context,
  override val what: Seq[String],
  override val args: BSONDocument
) extends UpdateFacet(context, what, args) with NoopAction

case class NoopDeleteFacet(
  override val context: Context,
  override val what: Seq[String]
) extends DeleteFacet(context, what) with NoopAction

case class NoopQueryFacet(
  override val context: Context,
  override val what: Seq[String],
  override val args: BSONDocument
) extends QueryFacet(context, what, args) with NoopAction


object Entity extends scrupal.utils.Registry[Entity] {
  val registrantsName: String = "entity"
  val registryName: String = "Entities"

  import BSONHandlers._


  case class EntityDao(db: ScrupalDB) extends IdentifierDAO[Entity] {
    implicit val reader: Reader = EntityHandler.asInstanceOf[Reader]
    implicit val writer: Writer = EntityHandler.asInstanceOf[Writer]

    def collectionName: String = "entities"
  }

}

