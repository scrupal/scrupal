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

import play.api.libs.json._
import scrupal.api.types.BundleType
import scrupal.storage.api.Storable
import scrupal.utils.{Enablee, Registrable}

import scala.concurrent.Future

trait EntityCollectionReactor extends Reactor with Nameable with Describable {
  def id : String
}

trait EntityInstanceReactor extends Reactor with Nameable with Describable {
  def id : String
  def what : Iterable[String]
}

/** Provider of EntityCommands for command parameters
  * This
  * TODO: Finish documenting EntityCommandProvider interface as it is necessary to understanding entities
  */
trait EntityProvider extends PluralityProvider {

  override def provide(request: Request) : Option[Reactor] = {
    request.message.headOption.map { msg ⇒
      if (isPlural(request)) {
        msg.toLowerCase match {
          case "create" ⇒ create(request)
          case "retrieve" ⇒ retrieve(request)
          case "update" ⇒ update(request)
          case "delete" ⇒ delete(request)
          case "query" ⇒ query(request)
          case x: String ⇒ noSuchMessage(request, s"Message $x not supported on enitty $label")
        }
      } else {
        msg.toLowerCase match {
          case "add" ⇒ add(request)
          case "get" ⇒ get(request)
          case "set" ⇒  set(request)
          case "remove" ⇒ remove(request)
          case "find" ⇒  find(request)
          case x: String ⇒ noSuchMessage(request, s"Message $x not supported on entity $label instance ${request.instance}")
        }
      }
    }
  }

  def noSuchMessage(req : Request, msg: String) : Reactor = {
    new Reactor {
      val request : Request = req
      def apply(request: Request): Future[Response] = { Future.successful( ErrorResponse(msg, Unavailable) ) }
    }
  }

  /** CREATE ENTITY (POST/plural) - Create a new entity from scratch
    * This is a command on the entity type's container to insert a new entity. The instance should be created
    * with the provided identifier, if possible. If not, respond with an error.
    * @param request The request to create the entity
    * @return A Create object that can perform the task from an Actor (i.e. in an isolated context)
    */
  def create(request: Request) : CreateReactor

  /** Retrieve Command (GET/plural) - Retrieve an existing entity by its identifier
    * This is a command on the entity type's contain to retrieve a specific entity. The full instance should be
    * retrieved, including all retrievable facets.
    * @param request The request to retrieve the entity
    * @return
    */
  def retrieve(request : Request) : RetrieveReactor

  /** Update Command (PUT/plural) - Updates all or a few of the fields of an entity
    * @param request The request to update the entity
    * @return
    */
  def update(request : Request) : UpdateReactor

  /** Delete Command (DELETE/plural) */
  /** Delete an entity
    * @param request The request to delete the entity
    * @return THe DeleteReaction to generate the response for the Delete request
    */
  def delete(request : Request) : DeleteReactor

  /** Query Command (OPTIONS/plural)
    *
    * @param request The request to query the entity
    * @return The QueryReaction to generate the response for the Query request
    */
  def query(request: Request) : QueryReactor

  /** Create Facet Command (POST/singular) */
  def add(request: Request) : AddReactor

  /** RetrieveAspect Command (GET/singular) */
  def get(request: Request) : GetReactor

  /** UpdateAspect Command (PUT/singular) */
  def set(request: Request) : SetReactor

  /** XXX Command (DELETE/singular) */
  def remove(request: Request) : RemoveReactor

  /** XXX Command (OPTIONS/singular) */
  def find(request: Request) : FindReactor
}

trait CreateReactor extends EntityCollectionReactor {
  val name : String = "Create"
  val description = "Create a specific instance of the entity and insert it in the entity collection."
  val id = request.instance
  val data : JsObject = emptyJsObject
}

trait RetrieveReactor extends EntityCollectionReactor {
  val name : String = "Retrieve"
  val description = "Retrieve a specific instance of the entity from the entity collection."
  val id : String = request.instance
}

trait UpdateReactor extends EntityCollectionReactor {
  val name : String = "Update"
  val description = "Update a specific instance of the entity."
  val id : String = request.instance
  val fields : JsObject = emptyJsObject
}

trait DeleteReactor extends EntityCollectionReactor {
  val name : String = "Delete"
  val description = "Delete a specific instance from the entity collection."
  val id : String = request.instance
}

trait QueryReactor extends EntityCollectionReactor {
  val name : String = "Query"
  val description = "Query the entity collection for an entity of a certain id or containing certain data."
  val id : String = request.instance
  val fields : JsObject = emptyJsObject
}

trait AddReactor extends EntityInstanceReactor {
  val name : String = "Add"
  val description = "Create a facet on a specific entity in the collection."
  val id : String = request.instance
  val what : Iterable[String] = request.message
  val instance : JsObject = emptyJsObject
}

trait GetReactor extends EntityInstanceReactor {
  val name : String = "Get"
  val description = "Retrieve a facet from a specific entity in the collection."
  val id : String = request.instance
  val what : Iterable[String] = request.message
}

trait SetReactor extends EntityInstanceReactor {
  val name : String = "Set"
  val description = "Update a facet from a specific entity in the collection."
  val id : String = request.instance
  val what : Iterable[String] = request.message
  val fields : JsObject = emptyJsObject
}

trait RemoveReactor extends EntityInstanceReactor {
  val name : String = "Remove"
  val description = "Delete a facet from a specific entity in the collection."
  val id : String = request.instance
  val what : Iterable[String] = request.message
}

trait FindReactor extends EntityInstanceReactor {
  val name : String = "Find"
  val description = "Query a specific entity in the collection for a facet of a certain id or containing certain data."
  val id : String = request.instance
  val what : Iterable[String] = request.message
  val args : JsObject = emptyJsObject
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
} with EntityProvider with Storable with Registrable[Entity] with ModuleOwned with Authorable
  with Describable with Enablee with Bootstrappable {
  def moduleOf = { scrupal.Modules.values.find(mod ⇒ mod.entities.contains(this)) }

  implicit def scrupal: Scrupal

  override def parent = moduleOf

  def instanceType : BundleType

  def registry = scrupal.Entities

  /*
  def toMap(mt : Map[String,Any]) : Map[String, Any] = mt

  def validateElement(ref : SelectedLocation, k: String, v : Any) : Results[Any] = {

  }

  override def validate(ref : Location, value : Map[String,Any] ) : VResult = instanceType.validate(ref, value)
*/

  def create(request: Request) : CreateReactor = NoopCreateReactor(request)

  def retrieve(request: Request) : RetrieveReactor = NoopRetrieveReactor(request)

  def update(request : Request) : UpdateReactor = NoopUpdateReactor(request)

  def delete(request : Request) : DeleteReactor = NoopDeleteReactor(request)

  def query(request : Request) : QueryReactor = NoopQueryReactor(request)

  def add(request : Request) : AddReactor = NoopAddReactor(request)

  def get(request : Request) : GetReactor = NoopGetReactor(request)

  def set(request : Request) : SetReactor = NoopSetReactor(request)

  def remove(request : Request) : RemoveReactor = NoopRemoveReactor(request)

  def find(request : Request) : FindReactor = NoopFindReactor(request)

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


trait NoopReactor extends Reactor with Describable {
  def apply(request: Request) : Future[Response] = Future.successful( NoopResponse )
  override val description = "Noop"
}

case class NoopCreateReactor(request : Request) extends CreateReactor with NoopReactor
case class NoopRetrieveReactor(request : Request) extends RetrieveReactor with NoopReactor
case class NoopUpdateReactor(request : Request) extends UpdateReactor with NoopReactor
case class NoopDeleteReactor(request : Request) extends DeleteReactor with NoopReactor
case class NoopQueryReactor(request : Request) extends QueryReactor with NoopReactor
case class NoopAddReactor(request : Request) extends AddReactor with NoopReactor
case class NoopGetReactor(request : Request) extends GetReactor with NoopReactor
case class NoopSetReactor(request : Request) extends SetReactor with NoopReactor
case class NoopRemoveReactor(request : Request) extends RemoveReactor with NoopReactor
case class NoopFindReactor(request : Request) extends FindReactor with NoopReactor

