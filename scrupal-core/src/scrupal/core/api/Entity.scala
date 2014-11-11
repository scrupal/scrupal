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

package scrupal.core.api

import akka.actor.{Props, Actor, ActorLogging}
import reactivemongo.bson._
import scrupal.core.BundleType
import scrupal.db.{VariantStorableRegistrable, StorableRegistrable, IdentifierDAO, ScrupalDB}

import scrupal.utils.{Pluralizer, Registry}

object Entity extends Registry[Entity] {
  val registrantsName: String = "entity"
  val registryName: String = "Entities"

  case class EntityDao(db: ScrupalDB) extends IdentifierDAO[Entity] {
    implicit val reader: Reader = EntityHandler.asInstanceOf[Reader]
    implicit val writer: Writer = EntityHandler.asInstanceOf[Writer]

    def collectionName: String = "entities"
  }
}

/** The fundamental unit of storage, behavior and interaction in Scrupal.
  *
  * An Entity brings together several things:The BundleType of an Instance's  payload,
  * definitions of the RESTful methods, security constraints, and extension actions for the REST API.
  * This is the key abstraction for Modules. Entities have a public REST API that is served by Scrupal. Entities
  * should represent some concept that is stored by Scrupal and delivered to the user interface via the REST API.
  */
trait Entity
  extends VariantStorableRegistrable[Entity] with ModuleOwned
          with Authorable with Describable with Enablable with Pathable with BSONValidator[BSONDocument]
{
  def moduleOf = { Module.all.find(mod ⇒ mod.entities.contains(this)) }

  def instanceType: BundleType

  def registry = Entity
  def asT = this

  def apply(value: BSONDocument) : ValidationResult = instanceType(value)

  val plural_path = Pluralizer.pluralize(path)

  /** The set of additional operations that can be invoked for this Entity in addition to the standard fetch,
    * create, update, relete,
    */
  val actions: Map[Symbol, Action[_,_]] = Map()

  case class Create(id: String, instance: BSONDocument, ctxt: Context)
  case class Retrieve(id: String, ctxt: Context)
  case class Update(id: String, fields: BSONDocument, ctxt: Context)
  case class Delete(id: String, ctxt: Context)
  case class Query(fields: BSONDocument, ctxt: Context)
  case class Option(id: String, option: String, ctxt: Context)
  case class Get(id: String, what: String, data: BSONDocument, ctxt: Context)
  case class Put(id: String, what: String, data: BSONDocument, ctxt: Context)
  case class AddFacet(id: String, name: String, facet: Facet, ctxt: Context)

  class DefaultWorker extends Actor with ActorLogging {
    def receive : Receive = {
      // TODO: Implement Entity.receive to process messages
      case a: Action[_, _] =>
      case Create(id: String, instance: BSONDocument, ctxt: Context) =>
      case Retrieve(id: String, ctxt: Context) =>
      case Update(id: String, fields: BSONDocument, ctxt: Context) =>
      case Delete(id: String, ctxt: Context) =>
      case Query(fields: BSONDocument, ctxt: Context) =>
      case Option(id: String, option: String, ctxt: Context) =>
      case Get(id: String, what: String, data: BSONDocument, ctxt: Context) =>
      case Put(id: String, what: String, data: BSONDocument, ctxt: Context) =>
      case AddFacet(id: String, name: String, facet: Facet, ctxt: Context) =>
    }
  }

  protected val worker = system.actorOf(Props(classOf[DefaultWorker], this), "EntityWorker-" + label)


  /** Fetch a single instance of this entity kind
    * Presumably this entity is stored somewhere and this method retrieve it, puts the data into a JsObject and
    * returns it.
    * @param id The identifier of the instance to be retrieved
    * @return The JsObject representing the payload of the entity retrieved
    */
  def retrieve(id: String, ctxt: Context)  = worker ! Retrieve(id, ctxt)

  /** Create a single instance of this entity kind
    * Presumably the entity
    * @param instance
    * @return
    */
  def create(id:String, instance: BSONDocument, ctxt: Context ) = worker ! Create(id, instance, ctxt)

  /** Update all or a few of the fields of an entity
    * @param id
    * @param fields
    * @return
    */
  def update(id: String, fields: BSONDocument, ctxt: Context) = worker ! Update(id, fields, ctxt)

  /** Delete an entity
    * @param id
    * @return
    */
  def delete(id: String, ctxt: Context) = worker ! Delete(id, ctxt)

  def query(fields: BSONDocument, ctxt: Context) = worker ! Query(fields, ctxt)

  /** Get meta information about an entity
    *
    * @param id
    * @param option
    * @return
    */
  def option(id: String, option: String, ctxt: Context) = worker ! Option(id, option, ctxt)

  /** Extension of fetch to retrieve not the entity but some aspect, or invoke some functionality
    *
    * @param id
    * @param what
    * @param data
    * @return
    */
  def get(id: String, what: String, data: BSONDocument, ctxt: Context) = worker ! Get(id, what, data, ctxt)

  /** Extension of update to send data as parameter to functionality or update something that is not part of the entity
    *
    * @param id
    * @param what
    * @param data
    * @return
    */
  def put(id: String, what: String, data: BSONDocument, ctxt: Context)  = worker ! Put(id, what, data, ctxt)

  def addFacet(id: String, name: String, facet: Facet, ctxt: Context) = worker ! AddFacet(id, name, facet, ctxt)

}

