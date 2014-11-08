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

import scrupal.utils.Registry

object Entity extends Registry[Entity] {
  val registrantsName: String = "entity"
  val registryName: String = "Entities"
}

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
  module: Module,
  var enabled : Boolean = true
) extends StorableRegistrable[Entity] with Describable with Enablable with BSONValidator
{
  def registry = Entity
  def asT = this

  def apply(value: BSONValue) : ValidationResult = instanceType(value)

  /** The set of additional operations that can be invoked for this Entity in addition to the standard fetch,
    * create, update, relete,
    */
  val actions: Map[Symbol, Action[_,_]] = Map()

  case class Create(id: String, instance: BSONDocument)
  case class Retrieve(id: String)
  case class Update(id: String, fields: BSONDocument)
  case class Delete(id: String)
  case class Query(fields: BSONDocument)
  case class Option(id: String, option: String)
  case class Get(id: String, what: String, data: BSONDocument)
  case class Put(id: String, what: String, data: BSONDocument)
  case class AddFacet(id: String, name: String, facet: Facet)

  class Worker() extends Actor with ActorLogging {
    def receive : Receive = {
      // TODO: Implement Entity.receive to process messages
      case a: Action[_, _] =>
      case Create(id: String, instance: BSONDocument) =>
      case Retrieve(id: String) =>
      case Update(id: String, fields: BSONDocument) =>
      case Delete(id: String) =>
      case Query(fields: BSONDocument) =>
      case Option(id: String, option: String) =>
      case Get(id: String, what: String, data: BSONDocument) =>
      case Put(id: String, what: String, data: BSONDocument) =>
      case AddFacet(id: String, name: String, facet: Facet) =>
    }
  }

  private[this] val worker = system.actorOf(Props(classOf[Worker], this), "Entity.Worker." + label)


  /** Fetch a single instance of this entity kind
    * Presumably this entity is stored somewhere and this method retrieve it, puts the data into a JsObject and
    * returns it.
    * @param id The identifier of the instance to be retrieved
    * @return The JsObject representing the payload of the entity retrieved
    */
  def retrieve(id: String)  = worker ! Retrieve(id)

  /** Create a single instance of this entity kind
    * Presumably the entity
    * @param instance
    * @return
    */
  def create(id:String, instance: BSONDocument ) = worker ! Create(id, instance)

  /** Update all or a few of the fields of an entity
    * @param id
    * @param fields
    * @return
    */
  def update(id: String, fields: BSONDocument) = worker ! Update(id, fields)

  /** Delete an entity
    * @param id
    * @return
    */
  def delete(id: String) = worker ! Delete(id)

  def query(fields: BSONDocument) = worker ! Query(fields)

  /** Get meta information about an entity
    *
    * @param id
    * @param option
    * @return
    */
  def option(id: String, option: String) = worker ! Option(id, option)

  /** Extension of fetch to retrieve not the entity but some aspect, or invoke some functionality
    *
    * @param id
    * @param what
    * @param data
    * @return
    */
  def get(id: String, what: String, data: BSONDocument) = worker ! Get(id, what, data)

  /** Extension of update to send data as parameter to functionality or update something that is not part of the entity
    *
    * @param id
    * @param what
    * @param data
    * @return
    */
  def put(id: String, what: String, data: BSONDocument)  = worker ! Put(id, what, data)

  def addFacet(id: String, name: String, facet: Facet) = worker ! AddFacet(id, name, facet)

}

