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
import scrupal.db.{VariantStorableRegistrable, IdentifierDAO, ScrupalDB}

import scrupal.utils.Enablee
import shapeless.HList

import scala.concurrent.Future

trait EntityCollectionAction extends Action {
  def id: String
}

trait EntityInstanceAction extends Action {
  def id: String
  def what: Seq[String]
}

/** Provider of EntityCommands for command parameters
  * This
  * TODO: Finish documenting EntityCommandProvider interface as it is necessary to understanding entities
  */
trait EntityActionProvider extends TerminalActionProvider {

  def pathsToActions = Seq.empty[PathToAction[_ <: HList]]

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
  def createFacet(context: Context, id: String, what: Seq[String], args: BSONDocument) : CreateFacet

  /** RetrieveAspect Command (GET/singular) */
  def retrieveFacet(context: Context, id: String, what: Seq[String]) : RetrieveFacet

  /** UpdateAspect Command (PUT/singular) */
  def updateFacet(context: Context, id: String, what: Seq[String], args: BSONDocument) : UpdateFacet

  /** XXX Command (DELETE/singular) */
  def deleteFacet(context: Context, id: String, what: Seq[String]) : DeleteFacet

  /** XXX Command (OPTIONS/singular) */
  def queryFacet(context: Context, id: String, what: Seq[String], args: BSONDocument) : QueryFacet
}

abstract class Create(val context: Context, val id: String, val instance: BSONDocument)
  extends EntityCollectionAction

abstract class Retrieve(val context: Context, val id: String)
  extends EntityCollectionAction

abstract class Update(val context: Context, val id: String, fields: BSONDocument)
  extends EntityCollectionAction

abstract class Delete(val context: Context, val id: String)
  extends EntityCollectionAction

abstract class Query(val context: Context, val id: String, fields: BSONDocument)
  extends EntityCollectionAction

abstract class CreateFacet(val context: Context, val id: String, val what: Seq[String], val args: BSONDocument)
  extends EntityInstanceAction

abstract class RetrieveFacet(val context: Context, val id: String, val what: Seq[String])
  extends EntityInstanceAction

abstract class UpdateFacet(val context: Context, val id: String, val what: Seq[String], val args: BSONDocument)
  extends EntityInstanceAction

abstract class DeleteFacet(val context: Context, val id: String, val what: Seq[String])
  extends EntityInstanceAction

abstract class QueryFacet(val context: Context, val id: String, val what: Seq[String], val args: BSONDocument)
  extends EntityInstanceAction


/** The fundamental unit of storage, behavior and interaction in Scrupal.
  *
  * An Entity brings together several things:The BundleType of an Instance's  payload,
  * definitions of the RESTful methods, security constraints, and extension actions for the REST API.
  * This is the key abstraction for Modules. Entities have a public REST API that is served by Scrupal. Entities
  * should represent some concept that is stored by Scrupal and delivered to the user interface via the REST API.
  */
abstract class Entity
  extends EntityActionProvider with VariantStorableRegistrable[Entity] with ModuleOwned
          with Describable with Enablee with BSONValidator[BSONDocument] with Bootstrappable
{
  def moduleOf = { Module.values.find(mod ⇒ mod.entities.contains(this)) }

  override def parent = moduleOf

  def instanceType: BundleType

  def registry = Entity
  def asT = this

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

  def createFacet(context: Context, id: String, what: Seq[String], args: BSONDocument)
    : CreateFacet = NoopCreateFacet(context, id, what, args)

  def retrieveFacet(context: Context, id: String, what: Seq[String])
    : RetrieveFacet = NoopRetrieveFacet(context, id, what)

  def updateFacet(context: Context, id: String, what: Seq[String], args: BSONDocument)
    : UpdateFacet = NoopUpdateFacet(context, id, what, args)

  def deleteFacet(context: Context, id: String, what: Seq[String])
    : DeleteFacet = NoopDeleteFacet(context, id, what)

  def queryFacet(context: Context, id: String, what: Seq[String], args: BSONDocument)
    : QueryFacet = NoopQueryFacet(context, id, what, args)

}

trait NoopAction {
  def apply : Future[Result[_]] = Future.successful( BSONResult(BSONDocument(), Indeterminate) )
}

case class NoopCreate(
  override val context: Context,
  override val id: String,
  override val instance: BSONDocument) extends Create(context, id,  instance) with NoopAction

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
  override val id: String,
  override val what: Seq[String],
  override val args: BSONDocument
) extends CreateFacet(context, id, what, args) with NoopAction

case class NoopRetrieveFacet(
  override val context: Context,
  override val id: String,
  override val what: Seq[String]) extends RetrieveFacet(context, id, what) with NoopAction

case class NoopUpdateFacet(
  override val context: Context,
  override val id: String,
  override val what: Seq[String],
  override val args: BSONDocument) extends UpdateFacet(context, id, what, args) with NoopAction

case class NoopDeleteFacet(
  override val context: Context,
  override val id: String,
  override val what: Seq[String]) extends DeleteFacet(context, id, what) with NoopAction

case class NoopQueryFacet(
  override val context: Context,
  override val id: String,
  override val what: Seq[String],
  override val args: BSONDocument) extends QueryFacet(context, id, what, args) with NoopAction


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

