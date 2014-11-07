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

import reactivemongo.bson._
import scrupal.core.BundleType

import scrupal.utils.Registry

/** The fundamental unit of storage, behavior and interaction in Scrupal
  * An Entity brings together several things:The BundleType of an Instance's  payload,
  * definitions of the RESTful methods, security constraints, and extension actions for the REST API.
  * This is the key abstraction for Modules. Entities have a public REST API that is served by Scrupal. Entities
  * should represent some concept that is stored by Scrupal and delivered to the user interface via the REST API.
  */
case class Entity (
  id : Identifier,
  description: String,
  instanceType: BundleType,
  module: Module
) extends StorableRegistrable[Entity] with Describable with BSONValidator {
  def registry = Entity
  def asT = this

  def apply(value: BSONValue) : ValidationResult = instanceType(value)

  /** The set of additional operations that can be invoked for this Entity in addition to the standard fetch,
    * create, update, relete,
    */
  val methods: Map[Symbol, Action[_,_]] = Map()

  /** Fetch a single instance of this entity kind
    * Presumably this entity is stored somewhere and this method retrieve it, puts the data into a JsObject and
    * returns it.
    * @param id The identifier of the instance to be retrieved
    * @return The JsObject representing the payload of the entity retrieved
    */
  def fetch(id: String)  = ???

  /** Create a single instance of this entity kind
    * Presumably the entity
    * @param instance
    * @return
    */
  def create(instance: BSONDocument ) = ???

  /** Update all or a few of the fields of an entity
    * @param id
    * @param fields
    * @return
    */
  def update(id: String, fields: BSONDocument) = ???

  /** Delete an entity
    * @param id
    * @return
    */
  def delete(id: String) = ???

  /** Get meta information about an entity
    *
    * @param id
    * @param option
    * @return
    */
  def option(id: String, option: String) = ???

  /** Extension of fetch to retrieve not the entity but some aspect, or invoke some functionality
    *
    * @param id
    * @param what
    * @param data
    * @return
    */
  def get(id: String, what: String, data: BSONDocument) = ???

  /** Extension of update to send data as parameter to functionality or update something that is not part of the entity
    *
    * @param id
    * @param what
    * @param data
    * @return
    */
  def put(id: String, what: String, data: BSONDocument)  = ???
}

object Entity extends Registry[Entity] {
  val registrantsName: String = "entity"
  val registryName: String = "Entities"

  /* TODO: Delete this when we're sure we never need to store entities in the DB
  case class EntityDao(db: DefaultDB) extends DataAccessObject[Entity,Identifier](db, "entities") {
    implicit val modelHandler :  BSONDocumentReader[Entity] with BSONDocumentWriter[Entity] with BSONHandler[BSONDocument,Entity] = handler[Entity]
    implicit val idHandler = (id: Symbol) ⇒ reactivemongo.bson.BSONString(id.name)
    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("_id" -> IndexType.Ascending), name = Some("UniqueId"))
    )
  }
  */
}


