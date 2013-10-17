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

package scrupal.api

import scala.collection.immutable.HashMap
import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.http.Status
import scrupal.utils.Registrable


case class EssentialEntity(
  id: Symbol,
  val description: String,
  val instanceTypeId: TypeIdentifier
)

case class StatusResult[TYPE](result: JsResult[TYPE], status: Int = Status.OK)

/** The fundamental unit of storage, behavior and interaction in Scrupal
  * An Entity brings together several things:The BundleType of an Instance's JSon payload, the web c
  * definitions of the RESTful methods, security constraints, and extension actions for the REST API.
  * This is the key abstraction for Modules. Entities have a public REST API that is served by Scrupal. Entities
  * should represent some concept that is stored by Scrupal and delivered to the user interface via the REST API.
  * There, a
  */
abstract class Entity(
  override val id: Symbol,
  override val description: String,
  override val instanceTypeId: TypeIdentifier
) extends EssentialEntity(id, description, instanceTypeId) with Registrable {

  /** Obtain the type of this Entity's Instance bundle. */
  def instanceType : Type = Type(instanceTypeId).getOrElse(Type.NotAType)

  /** Entity Instances must be defined as a Bundle. Enforce that here */
  require(instanceType.kind == 'Bundle)

  val actions: HashMap[Symbol, Action] = HashMap()

  /** Fetch a single instance of this entity kind
    * Presumably this entity is stored somewhere and this method retrieve it, puts the data into a JsObject and
    * returns it.
    * @param id The identifier of the instance to be retrieved
    * @return The JsObject representing the payload of the entity retrieved
    */
  def fetch(id: InstanceIdentifier) : JsObject = ???

  /** Create a single instance of this entity kind
    * Presumably the entity
    * @param instance
    * @return
    */
  def create(instance: JsObject ) : StatusResult[InstanceIdentifier]  = ???

  /** Update all or a few of the fields of an entity
    *
    * @param id
    * @param fields
    * @return
    */
  def update(id: InstanceIdentifier, fields: JsObject) : StatusResult[Long] = ???

  /** Delete an entity */
  def delete(id: InstanceIdentifier) : StatusResult[Boolean]  = ???

  /** Get meta information about an entity */
  def option(id: InstanceIdentifier, option: String) : StatusResult[JsValue] = ???

}



