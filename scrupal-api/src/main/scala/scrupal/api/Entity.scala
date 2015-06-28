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

  def create(details: String) : CreateReactor = {
    NoOpCreateReactor(details)
  }

  def retrieve(instance_id: String, details: String) : RetrieveReactor = {
    NoOpRetrieveReactor(instance_id, details)
  }

  def retrieve(instance_id: Long, details: String) : RetrieveReactor = {
    retrieve (instance_id.toString, details)
  }

  def update(instance_id: String, details: String) : UpdateReactor = {
    NoOpUpdateReactor(instance_id, details)
  }
  def update(instance_id: Long, details: String) : UpdateReactor = {
    update(instance_id.toString, details)
  }

  def delete(instance_id: String, details: String) : DeleteReactor = {
    NoOpDeleteReactor(instance_id, details)
  }
  def delete(instance_id: Long, details: String) : DeleteReactor = {
    delete(instance_id.toString, details)
  }

  def query(details: String) : QueryReactor = {
    NoOpQueryReactor(details)
  }

  def add(instance_id: String, facet: String, details: String) : AddReactor = {
    NoOpAddReactor(instance_id, facet, details)
  }
  def add(instance_id: Long, facet: String, details: String) : AddReactor = {
    add(instance_id.toString, facet, details)
  }

  def get(instance_id: String, facet: String, facet_id: String, details: String) : GetReactor = {
    NoOpGetReactor(instance_id, facet, facet_id, details)
  }
  def get(instance_id: Long, facet: String, facet_id: String, details: String) : GetReactor = {
    get(instance_id.toString, facet, facet_id, details)
  }

  def set(instance_id: String, facet: String, facet_id: String, details: String) : SetReactor = {
    NoOpSetReactor(instance_id, facet, facet_id, details)
  }
  def set(instance_id: Long, facet: String, facet_id: String, details: String) : SetReactor = {
    set(instance_id.toString, facet, facet_id, details)
  }

  def remove(instance_id: String, facet: String, facet_id: String, details: String) : RemoveReactor = {
    NoOpRemoveReactor(instance_id, facet, facet_id, details)
  }
  def remove(instance_id: Long, facet: String, facet_id: String, details: String) : RemoveReactor = {
    remove(instance_id.toString, facet, facet_id, details)
  }

  def find(instance_id: String, facet: String, details: String) : FindReactor = {
    NoOpFindReactor(instance_id, facet, details)
  }
  def find(instance_id: Long, facet: String, details: String) : FindReactor = {
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

case class NoOpCreateReactor(details: String)
  extends CreateReactor with NoOpReactor
case class NoOpRetrieveReactor(instance_id: String, details: String)
  extends RetrieveReactor with NoOpReactor
case class NoOpUpdateReactor(instance_id: String, details: String)
  extends UpdateReactor with NoOpReactor
case class NoOpDeleteReactor(instance_id: String, details: String)
  extends DeleteReactor with NoOpReactor
case class NoOpQueryReactor(details: String)
  extends QueryReactor with NoOpReactor
case class NoOpAddReactor(instance_id: String, facet: String, details: String)
  extends AddReactor with NoOpReactor
case class NoOpGetReactor(instance_id: String, facet: String, facet_id: String, details: String)
  extends GetReactor with NoOpReactor
case class NoOpSetReactor(instance_id: String, facet: String, facet_id: String, details: String)
  extends SetReactor with NoOpReactor
case class NoOpRemoveReactor(instance_id: String, facet: String, facet_id: String, details: String)
  extends RemoveReactor with NoOpReactor
case class NoOpFindReactor(instance_id: String, facet: String, details: String)
  extends FindReactor with NoOpReactor

