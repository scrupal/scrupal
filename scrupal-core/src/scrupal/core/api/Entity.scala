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

package scrupal.core.api

import reactivemongo.bson._
import scrupal.core.BundleType
import scrupal.db.{VariantStorableRegistrable, IdentifierDAO, ScrupalDB}

import scrupal.utils.{Patterns, Pluralizer}

trait EntityCommand extends Action {
  def apply : Result[_] = BSONResult(BSONDocument(), Indeterminate)
}

trait EntityCollectionCommand extends EntityCommand {
  def id: String
}

trait EntityInstanceCommand extends EntityCommand {
  def id: String
  def what: String
}

/** Provider of EntityCommands for command parameters
  * This
  * TODO: Finish documenting EntityCommandProvider interface as it is necessary to understanding entities
  */
trait EntityCommandProvider {

  /** CREATE ENTITY (POST/plural) - Create a new entity from scratch
    * This is a command on the entity type's container to insert a new entity. The instance should be created
    * with the provided identifier, if possible. If not, respond with an error.
    * @param context The context from which the invocation is being made
    * @param id The unique identifier for new entity instance
    * @param instance The content of the entity (whcih will be validated against the entitie's bundle type)
    * @return A Create object that can perform the task from an Actor (i.e. in an isolated context)
    */
  def create(context: ApplicationContext, id: String, instance: BSONDocument) : Create

  /** Retrieve Command (GET/plural) - Retrieve an existing entity by its identifier
    * This is a command on the entity type's contain to retrieve a specific entity. The full instance should be
    * retrieved, including all retrievable facets.
    * @param context The context from which the invocation is being made
    * @param id The unique identifier for new entity instance
    * @return
    */
  def retrieve(context: ApplicationContext, id: String) : Retrieve

  /** Update Command (PUT/plural) - Updates all or a few of the fields of an entity
    * @param id
    * @param fields
    * @return
    */
  def update(context: ApplicationContext, id: String, fields: BSONDocument) : Update

  // TODO: Finish documenting EntityCommandProvider methods

  /** Delete Command (DELETE/plural) */
  /** Delete an entity
    * @param id
    * @return
    */
  def delete(context: ApplicationContext, id: String) : Delete

  /** Query Command (OPTIONS/plural) */
  def query(context: ApplicationContext, fields: BSONDocument) : Query

  /** Create Facet Command (POST/singular) */
  def createFacet(context: ApplicationContext, id: String, what: String, args: BSONDocument) : CreateFacet

  /** RetrieveAspect Command (GET/singular) */
  def retrieveFacet(context: ApplicationContext, id: String, what: String) : RetrieveFacet

  /** UpdateAspect Command (PUT/singular) */
  def updateFacet(context: ApplicationContext, id: String, what: String, args: BSONDocument) : UpdateFacet

  /** XXX Command (DELETE/singular) */
  def deleteFacet(context: ApplicationContext, id: String, what: String) : DeleteFacet

  /** XXX Command (OPTIONS/singular) */
  def queryFacet(context: ApplicationContext, id: String, what: String, args: BSONDocument) : QueryFacet

  /** XXX Command (POST/singular) */
  def invoke(context: ApplicationContext, id: String, what: String, args: BSONDocument) : Invoke
}

case class Create(context: ApplicationContext, id: String, instance: BSONDocument) extends EntityCollectionCommand

case class Retrieve(context: ApplicationContext, id: String) extends EntityCollectionCommand

case class Update(context: ApplicationContext, id: String, fields: BSONDocument) extends EntityCollectionCommand

case class Delete(context: ApplicationContext, id: String) extends EntityCollectionCommand

case class Query(context: ApplicationContext, id: String, fields: BSONDocument) extends EntityCollectionCommand

case class CreateFacet(context: ApplicationContext, id: String, what: String, args: BSONDocument)
  extends EntityInstanceCommand

case class RetrieveFacet(context: ApplicationContext, id: String, what: String) extends EntityInstanceCommand

case class UpdateFacet(context: ApplicationContext, id: String, what: String, args: BSONDocument)
  extends EntityInstanceCommand

case class DeleteFacet(context: ApplicationContext, id: String, what: String) extends EntityInstanceCommand

case class QueryFacet(context: ApplicationContext, id: String, what: String, args: BSONDocument)
  extends EntityInstanceCommand

case class Invoke(context: ApplicationContext, id: String, what: String, args: BSONDocument)
  extends EntityInstanceCommand

/** The fundamental unit of storage, behavior and interaction in Scrupal.
  *
  * An Entity brings together several things:The BundleType of an Instance's  payload,
  * definitions of the RESTful methods, security constraints, and extension actions for the REST API.
  * This is the key abstraction for Modules. Entities have a public REST API that is served by Scrupal. Entities
  * should represent some concept that is stored by Scrupal and delivered to the user interface via the REST API.
  */
abstract class Entity
  extends EntityCommandProvider with VariantStorableRegistrable[Entity] with ModuleOwned
          with Authorable with Describable with Enablable
          with Pathable with BSONValidator[BSONDocument] with Bootstrappable
{
  def moduleOf = { Module.all.find(mod ⇒ mod.entities.contains(this)) }

  def instanceType: BundleType

  def registry = Entity
  def asT = this

  def apply(value: BSONDocument) : ValidationResult = instanceType(value)

  final val path: String = id.name.toLowerCase.replaceAll(Patterns.NotAllowedInUrl.pattern.pattern,"-")

  final val plural_path = Pluralizer.pluralize(path)

  require(path != plural_path)

  /** The set of additional operations that can be invoked for this Entity in addition to the standard fetch,
    * create, update, relete,
    */
  val actions: Map[Symbol, Action] = Map()

  def create(context: ApplicationContext, id: String, instance: BSONDocument) : Create = Entity.NoopCreate

  def retrieve(context: ApplicationContext, id: String) : Retrieve = Entity.NoopRetrieve

  def update(context: ApplicationContext, id: String, fields: BSONDocument) : Update = Entity.NoopUpdate
  def delete(context: ApplicationContext, id: String) : Delete = Entity.NoopDelete
  def query(context: ApplicationContext, fields: BSONDocument) : Query = Entity.NoopQuery
  def createFacet(context: ApplicationContext, id: String, what: String, args: BSONDocument)
    : CreateFacet = Entity.NoopCreateFacet

  /** RetrieveAspect Command (GET/singular) */
  def retrieveFacet(context: ApplicationContext, id: String, what: String) : RetrieveFacet = Entity.NoopRetrieveFacet

  /** UpdateAspect Command (PUT/singular) */
  def updateFacet(context: ApplicationContext, id: String, what: String, args: BSONDocument)
    : UpdateFacet = Entity.NoopUpdateFacet

  /** XXX Command (DELETE/singular) */
  def deleteFacet(context: ApplicationContext, id: String, what: String) : DeleteFacet = Entity.NoopDeleteFacet

  /** XXX Command (OPTIONS/singular) */
  def queryFacet(context: ApplicationContext, id: String, what: String, args: BSONDocument) : QueryFacet = Entity.NoopQueryFacet

  /** XXX Command (POST/singular) */
  def invoke(context: ApplicationContext, id: String, what: String, args: BSONDocument) : Invoke = Entity.NoopInvoke

  private[scrupal] def bootstrap() = {}
}

object Entity extends scrupal.utils.Registry[Entity] {
  val registrantsName: String = "entity"
  val registryName: String = "Entities"

  case class EntityDao(db: ScrupalDB) extends IdentifierDAO[Entity] {
    implicit val reader: Reader = EntityHandler.asInstanceOf[Reader]
    implicit val writer: Writer = EntityHandler.asInstanceOf[Writer]

    def collectionName: String = "entities"
  }

  lazy val NoopCreate = new Create(ApplicationContext.Empty, "", BSONDocument())
  lazy val NoopRetrieve = new Retrieve(ApplicationContext.Empty, "")
  lazy val NoopUpdate = new Update(ApplicationContext.Empty, "", BSONDocument())
  lazy val NoopDelete = new Delete(ApplicationContext.Empty, "")
  lazy val NoopQuery = new Query(ApplicationContext.Empty, "", BSONDocument())
  lazy val NoopCreateFacet = new CreateFacet(ApplicationContext.Empty, "", "", BSONDocument())
  lazy val NoopRetrieveFacet = new RetrieveFacet(ApplicationContext.Empty, "", "")
  lazy val NoopUpdateFacet = new UpdateFacet(ApplicationContext.Empty, "", "", BSONDocument())
  lazy val NoopDeleteFacet = new DeleteFacet(ApplicationContext.Empty, "", "")
  lazy val NoopQueryFacet = new QueryFacet(ApplicationContext.Empty, "", "", BSONDocument())
  lazy val NoopInvoke = new Invoke(ApplicationContext.Empty, "", "", BSONDocument())

}

