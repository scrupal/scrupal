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

import scrupal.storage.api.Storable
import scrupal.utils.{Enablee, Registrable}

import scala.concurrent.Future
import scala.language.implicitConversions

/** The fundamental unit of storage, behavior and interaction in Scrupal.
  *
  * An Entity brings together several things:The BundleType of an Instance's  payload,
  * definitions of the RESTful methods, security constraints, and extension actions for the REST API.
  * This is the key abstraction for Modules. Entities have a public REST API that is served by Scrupal. Entities
  * should represent some concept that is stored by Scrupal and delivered to the user interface via the REST API.
  */
abstract class Entity(sym : Symbol)(implicit scrpl : Scrupal) extends {
  val id : Symbol = sym
  val segment : String = id.name
  implicit val scrupal: Scrupal = scrpl
} with EntityProvider with Storable with Registrable[Entity] with ModuleOwned with Authorable
  with Describable with Enablee with Bootstrappable {
  def moduleOf = { scrupal.Modules.values.find(mod ⇒ mod.entities.contains(this)) }

  override def parent = moduleOf

  def instanceType : BundleType

  def registry = scrupal.Entities

  /*
  def toMap(mt : Map[String,Any]) : Map[String, Any] = mt

  def validateElement(ref : SelectedLocation, k: String, v : Any) : Results[Any] = {

  }

  override def validate(ref : Location, value : Map[String,Any] ) : VResult = instanceType.validate(ref, value)
*/

  def create(details: String) : EntityCreate = {
    NoOpEntityCreate(details)
  }

  def retrieve(instance_id: String, details: String) : EntityRetrieve = {
    NoOpEntityRetrieve(instance_id, details)
  }

  def retrieve(instance_id: Long, details: String) : EntityRetrieve = {
    retrieve (instance_id.toString, details)
  }

  def info(instance_id: String, details: String) : EntityInfo = {
    NoOpEntityInfo(instance_id, details)
  }

  def info(instance_id: Long, details: String) : EntityInfo = {
    info (instance_id.toString, details)
  }

  def update(instance_id: String, details: String) : EntityUpdate = {
    NoOpEntityUpdate(instance_id, details)
  }
  def update(instance_id: Long, details: String) : EntityUpdate = {
    update(instance_id.toString, details)
  }

  def delete(instance_id: String, details: String) : EntityDelete = {
    NoOpEntityDelete(instance_id, details)
  }
  def delete(instance_id: Long, details: String) : EntityDelete = {
    delete(instance_id.toString, details)
  }

  def query(details: String) : EntityQuery = {
    NoOpEntityQuery(details)
  }

  def add(instance_id: String, facet: String, details: String) : EntityAdd = {
    NoOpEntityAdd(instance_id, facet, details)
  }
  def add(instance_id: Long, facet: String, details: String) : EntityAdd = {
    add(instance_id.toString, facet, details)
  }

  def get(instance_id: String, facet: String, facet_id: String, details: String) : EntityGet = {
    NoOpEntityGet(instance_id, facet, facet_id, details)
  }
  def get(instance_id: Long, facet: String, facet_id: String, details: String) : EntityGet = {
    get(instance_id.toString, facet, facet_id, details)
  }

  def facetInfo(instance_id: String, facet: String, facet_id: String, details: String) : EntityFacetInfo = {
    NoOpEntityFacetInfo(instance_id, facet, facet_id, details)
  }
  def facetInfo(instance_id: Long, facet: String, facet_id: String, details: String) : EntityFacetInfo = {
    facetInfo(instance_id.toString, facet, facet_id, details)
  }

  def set(instance_id: String, facet: String, facet_id: String, details: String) : EntitySet = {
    NoOpEntitySet(instance_id, facet, facet_id, details)
  }
  def set(instance_id: Long, facet: String, facet_id: String, details: String) : EntitySet = {
    set(instance_id.toString, facet, facet_id, details)
  }

  def remove(instance_id: String, facet: String, facet_id: String, details: String) : EntityRemove = {
    NoOpEntityRemove(instance_id, facet, facet_id, details)
  }
  def remove(instance_id: Long, facet: String, facet_id: String, details: String) : EntityRemove = {
    remove(instance_id.toString, facet, facet_id, details)
  }

  def find(instance_id: String, facet: String, details: String) : EntityFind = {
    NoOpEntityFind(instance_id, facet, details)
  }
  def find(instance_id: Long, facet: String, details: String) : EntityFind = {
    find(instance_id.toString, facet, details)
  }

}

/** The registry of Entities for this Scrupal.
  *
  * This object is the registry of Entity objects. When a [[scrupal.api.Entity]] is instantiated, it will register
  * itself with this object. The object is located in [[scrupal.api.Scrupal]]
  */
case class EntitiesRegistry() extends scrupal.utils.Registry[Entity] {
  val registrantsName : String = "entity"
  val registryName : String = "Entities"
}


trait NoOpReactor extends Reactor with Describable {
  def apply(request: Stimulus) : Future[Response] = Future.successful( NoopResponse )
}

case class NoOpEntityCreate(details: String)
  extends EntityCreate with NoOpReactor
case class NoOpEntityRetrieve(instance_id: String, details: String)
  extends EntityRetrieve with NoOpReactor
case class NoOpEntityInfo(instance_id: String, details: String)
  extends EntityInfo with NoOpReactor
case class NoOpEntityUpdate(instance_id: String, details: String)
  extends EntityUpdate with NoOpReactor
case class NoOpEntityDelete(instance_id: String, details: String)
  extends EntityDelete with NoOpReactor
case class NoOpEntityQuery(details: String)
  extends EntityQuery with NoOpReactor
case class NoOpEntityAdd(instance_id: String, facet: String, details: String)
  extends EntityAdd with NoOpReactor
case class NoOpEntityGet(instance_id: String, facet: String, facet_id: String, details: String)
  extends EntityGet with NoOpReactor
case class NoOpEntityFacetInfo(instance_id: String, facet: String, facet_id: String, details: String)
  extends EntityFacetInfo with NoOpReactor
case class NoOpEntitySet(instance_id: String, facet: String, facet_id: String, details: String)
  extends EntitySet with NoOpReactor
case class NoOpEntityRemove(instance_id: String, facet: String, facet_id: String, details: String)
  extends EntityRemove with NoOpReactor
case class NoOpEntityFind(instance_id: String, facet: String, details: String)
  extends EntityFind with NoOpReactor

