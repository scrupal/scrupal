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

trait EntityCollectionReaction extends Reaction with Nameable with Describable {
  def id : String
}

trait EntityInstanceReaction extends Reaction with Nameable with Describable {
  def id : String
  def what : Iterable[String]
}

/** Provider of EntityCommands for command parameters
  * This
  * TODO: Finish documenting EntityCommandProvider interface as it is necessary to understanding entities
  */
trait EntityProvider extends PluralityProvider {

  override def provide(request: Request) : Option[Reaction] = {
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

  def noSuchMessage(req : Request, msg: String) : Reaction = {
    new Reaction {
      val request : Request = req
      def apply(): Future[Response] = { Future.successful( ErrorResponse(msg, Unavailable) ) }
    }
  }

  /** CREATE ENTITY (POST/plural) - Create a new entity from scratch
    * This is a command on the entity type's container to insert a new entity. The instance should be created
    * with the provided identifier, if possible. If not, respond with an error.
    * @param request The request to create the entity
    * @return A Create object that can perform the task from an Actor (i.e. in an isolated context)
    */
  def create(request: Request) : CreateReaction

  /** Retrieve Command (GET/plural) - Retrieve an existing entity by its identifier
    * This is a command on the entity type's contain to retrieve a specific entity. The full instance should be
    * retrieved, including all retrievable facets.
    * @param request The request to retrieve the entity
    * @return
    */
  def retrieve(request : Request) : RetrieveReaction

  /** Update Command (PUT/plural) - Updates all or a few of the fields of an entity
    * @param request The request to update the entity
    * @return
    */
  def update(request : Request) : UpdateReaction

  /** Delete Command (DELETE/plural) */
  /** Delete an entity
    * @param request The request to delete the entity
    * @return THe DeleteReaction to generate the response for the Delete request
    */
  def delete(request : Request) : DeleteReaction

  /** Query Command (OPTIONS/plural)
    *
    * @param request The request to query the entity
    * @return The QueryReaction to generate the response for the Query request
    */
  def query(request: Request) : QueryReaction

  /** Create Facet Command (POST/singular) */
  def add(request: Request) : AddReaction

  /** RetrieveAspect Command (GET/singular) */
  def get(request: Request) : GetReaction

  /** UpdateAspect Command (PUT/singular) */
  def set(request: Request) : SetReaction

  /** XXX Command (DELETE/singular) */
  def remove(request: Request) : RemoveReaction

  /** XXX Command (OPTIONS/singular) */
  def find(request: Request) : FindReaction
}

trait CreateReaction extends EntityCollectionReaction {
  val name : String = "Create"
  val description = "Create a specific instance of the entity and insert it in the entity collection."
  val id = request.instance
  val data : JsObject = emptyJsObject
}

trait RetrieveReaction extends EntityCollectionReaction {
  val name : String = "Retrieve"
  val description = "Retrieve a specific instance of the entity from the entity collection."
  val id : String = request.instance
}

trait UpdateReaction extends EntityCollectionReaction {
  val name : String = "Update"
  val description = "Update a specific instance of the entity."
  val id : String = request.instance
  val fields : JsObject = emptyJsObject
}

trait DeleteReaction extends EntityCollectionReaction {
  val name : String = "Delete"
  val description = "Delete a specific instance from the entity collection."
  val id : String = request.instance
}

trait QueryReaction extends EntityCollectionReaction {
  val name : String = "Query"
  val description = "Query the entity collection for an entity of a certain id or containing certain data."
  val id : String = request.instance
  val fields : JsObject = emptyJsObject
}

trait AddReaction extends EntityInstanceReaction {
  val name : String = "Add"
  val description = "Create a facet on a specific entity in the collection."
  val id : String = request.instance
  val what : Iterable[String] = request.message
  val args : JsObject = emptyJsObject
}

trait GetReaction extends EntityInstanceReaction {
  val name : String = "Get"
  val description = "Retrieve a facet from a specific entity in the collection."
  val id : String = request.instance
  val what : Iterable[String] = request.message
}

trait SetReaction extends EntityInstanceReaction {
  val name : String = "Set"
  val description = "Update a facet from a specific entity in the collection."
  val id : String = request.instance
  val what : Iterable[String] = request.message
  val args : JsObject = emptyJsObject
}

trait RemoveReaction extends EntityInstanceReaction {
  val name : String = "Remove"
  val description = "Delete a facet from a specific entity in the collection."
  val id : String = request.instance
  val what : Iterable[String] = request.message
}

trait FindReaction extends EntityInstanceReaction {
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
} with EntityProvider with Storable with Registrable[Entity] with ModuleOwned
  with Describable with Enablee with Bootstrappable {
  def moduleOf = { Module.values.find(mod ⇒ mod.entities.contains(this)) }

  override def parent = moduleOf

  def instanceType : BundleType[Any]

  def registry = Entity

  /*
  def toMap(mt : Map[String,Any]) : Map[String, Any] = mt

  def validateElement(ref : SelectedLocation, k: String, v : Any) : Results[Any] = {

  }

  override def validate(ref : Location, value : Map[String,Any] ) : VResult = instanceType.validate(ref, value)
*/

  def create(request: Request) : CreateReaction = NoopCreateReaction(request)

  def retrieve(request: Request) : RetrieveReaction = NoopRetrieveReaction(request)

  def update(request : Request) : UpdateReaction = NoopUpdateReaction(request)

  def delete(request : Request) : DeleteReaction = NoopDeleteReaction(request)

  def query(request : Request) : QueryReaction = NoopQueryReaction(request)

  def add(request : Request) : AddReaction = NoopAddReaction(request)

  def get(request : Request) : GetReaction = NoopGetReaction(request)

  def set(request : Request) : SetReaction = NoopSetReaction(request)

  def remove(request : Request) : RemoveReaction = NoopRemoveReaction(request)

  def find(request : Request) : FindReaction = NoopFindReaction(request)

}

trait NoopReaction extends Reaction with Describable {
  def apply() : Future[Response] = Future.successful( NoopResponse )
  override val description = "Noop"
}

case class NoopCreateReaction(request : Request) extends CreateReaction with NoopReaction
case class NoopRetrieveReaction(request : Request) extends RetrieveReaction with NoopReaction
case class NoopUpdateReaction(request : Request) extends UpdateReaction with NoopReaction
case class NoopDeleteReaction(request : Request) extends DeleteReaction with NoopReaction
case class NoopQueryReaction(request : Request) extends QueryReaction with NoopReaction
case class NoopAddReaction(request : Request) extends AddReaction with NoopReaction
case class NoopGetReaction(request : Request) extends GetReaction with NoopReaction
case class NoopSetReaction(request : Request) extends SetReaction with NoopReaction
case class NoopRemoveReaction(request : Request) extends RemoveReaction with NoopReaction

case class NoopFindReaction(request : Request) extends FindReaction with NoopReaction

object Entity extends scrupal.utils.Registry[Entity] {
  val registrantsName : String = "entity"
  val registryName : String = "Entities"
}

