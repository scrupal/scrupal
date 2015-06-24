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

import akka.http.scaladsl.model.{Uri, HttpMethods}
import akka.http.scaladsl.server.PathMatcher.{Matched, Unmatched}
import akka.http.scaladsl.server.{PathMatcher1, PathMatcher}
import akka.http.scaladsl.server.PathMatchers._
import play.api.libs.json.JsObject
import scrupal.storage.api.Storable
import scrupal.utils.{Enablee, Registrable}

import scala.concurrent.Future
import scala.language.implicitConversions

trait EntityCollectionReactor extends Reactor with Nameable with Describable {
  def instance_id : String
  def rest: Uri.Path
}

trait EntityInstanceReactor extends Reactor with Nameable with Describable {
  def instance_id : String
  def facet: String
  def rest: Uri.Path
}

/** Provider of EntityCommands for command parameters
  * This
  * TODO: Finish documenting EntityCommandProvider interface as it is necessary to understanding entities
  */
trait EntityProvider extends PluralityProvider {

  val pluralEntityMatcher: PathMatcher1[String] = pluralMatcher / /*instance_id=*/Segment

  val singularEntityMatcher : PathMatcher[(String,String)] = pluralMatcher / /*instance_id=*/Segment / /*facet_name*/ Segment

  override def provide(request: Request) : Option[Reactor] = {
    singularEntityMatcher(request.path) match {
      case Matched(rest, extractions) ⇒
        val (instance_id, facet_name) = extractions
        request.method match {
          case HttpMethods.POST ⇒ Some(add(request, instance_id, facet_name, rest))
          case HttpMethods.GET ⇒ Some(get(request, instance_id, facet_name, rest))
          case HttpMethods.PUT ⇒  Some(set(request, instance_id, facet_name, rest))
          case HttpMethods.DELETE ⇒ Some(remove(request, instance_id, facet_name, rest))
          case HttpMethods.OPTIONS ⇒  Some(find(request, instance_id, facet_name, rest))
          case _ ⇒ noSuchMessage(request, s"$request not supported on entity $label.")
        }
      case Unmatched ⇒
        pluralEntityMatcher(request.path) match {
          case Matched(rest, extractions) ⇒
            val instance_id = extractions._1
            request.method match {
              case HttpMethods.POST ⇒ Some(create(request, instance_id, rest))
              case HttpMethods.GET ⇒ Some(retrieve(request, instance_id, rest))
              case HttpMethods.PUT ⇒ Some(update(request, instance_id, rest))
              case HttpMethods.DELETE ⇒ Some(delete(request, instance_id, rest))
              case HttpMethods.OPTIONS ⇒ Some(query(request, instance_id, rest))
              case _ ⇒ noSuchMessage(request, s"$request not supported on entitty $label.")
            }
          case Unmatched ⇒ None
        }
    }
  }

  def noSuchMessage(req : Request, msg: String) : Option[Reactor] = {
    Some(new Reactor {
      val request : Request = req
      def apply(request: Request): Future[Response] = { Future.successful( ErrorResponse(msg, Unavailable) ) }
    })
  }

  /** CREATE ENTITY (POST/plural) - Create a new entity from scratch
    * This is a command on the entity type's container to insert a new entity. The instance should be created
    * with the provided identifier, if possible. If not, respond with an error.
    * @param request The request to create the entity
    * @return A Create object that can perform the task from an Actor (i.e. in an isolated context)
    */
  def create(request: Request, instance_id: String, rest: Uri.Path) : CreateReactor

  /** Retrieve Command (GET/plural) - Retrieve an existing entity by its identifier
    * This is a command on the entity type's contain to retrieve a specific entity. The full instance should be
    * retrieved, including all retrievable facets.
    * @param request The request to retrieve the entity
    * @return
    */
  def retrieve(request : Request, instance_id: String, rest: Uri.Path) : RetrieveReactor

  /** Update Command (PUT/plural) - Updates all or a few of the fields of an entity
    * @param request The request to update the entity
    * @return
    */
  def update(request : Request, instance_id: String, rest: Uri.Path) : UpdateReactor

  /** Delete Command (DELETE/plural) */
  /** Delete an entity
    * @param request The request to delete the entity
    * @return THe DeleteReaction to generate the response for the Delete request
    */
  def delete(request : Request, instance_id: String, rest: Uri.Path) : DeleteReactor

  /** Query Command (OPTIONS/plural)
    *
    * @param request The request to query the entity
    * @return The QueryReaction to generate the response for the Query request
    */
  def query(request: Request, instance_id: String, rest: Uri.Path) : QueryReactor

  /** Create Facet Command (POST/singular) */
  def add(request: Request, instance_id: String, facet: String, rest: Uri.Path) : AddReactor

  /** RetrieveAspect Command (GET/singular) */
  def get(request: Request, instance_id: String, facet: String, rest: Uri.Path) : GetReactor

  /** UpdateAspect Command (PUT/singular) */
  def set(request: Request, instance_id: String, facet: String, rest: Uri.Path) : SetReactor

  /** XXX Command (DELETE/singular) */
  def remove(request: Request, instance_id: String, facet: String, rest: Uri.Path) : RemoveReactor

  /** XXX Command (OPTIONS/singular) */
  def find(request: Request, instance_id: String, facet: String, rest: Uri.Path) : FindReactor
}

trait CreateReactor extends EntityCollectionReactor {
  val name : String = "Create"
  val description = "Create a specific instance of the entity and insert it in the entity collection."
  val content : JsObject = emptyJsObject
}

trait RetrieveReactor extends EntityCollectionReactor {
  val name : String = "Retrieve"
  val description = "Retrieve a specific instance of the entity from the entity collection."
}

trait UpdateReactor extends EntityCollectionReactor {
  val name : String = "Update"
  val description = "Update a specific instance of the entity."
  val content : JsObject = emptyJsObject
}

trait DeleteReactor extends EntityCollectionReactor {
  val name : String = "Delete"
  val description = "Delete a specific instance from the entity collection."
}

trait QueryReactor extends EntityCollectionReactor {
  val name : String = "Query"
  val description = "Query the entity collection for an entity of a certain id or containing certain data."
}

trait AddReactor extends EntityInstanceReactor {
  val name : String = "Add"
  val description = "Create a facet on a specific entity in the collection."
  val content : JsObject = emptyJsObject
}

trait GetReactor extends EntityInstanceReactor {
  val name : String = "Get"
  val description = "Retrieve a facet from a specific entity in the collection."
}

trait SetReactor extends EntityInstanceReactor {
  val name : String = "Set"
  val description = "Update a facet from a specific entity in the collection."
  val content : JsObject = emptyJsObject
}

trait RemoveReactor extends EntityInstanceReactor {
  val name : String = "Remove"
  val description = "Delete a facet from a specific entity in the collection."
}

trait FindReactor extends EntityInstanceReactor {
  val name : String = "Find"
  val description = "Query a specific entity in the collection for a facet of a certain id or containing certain data."
}

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

  def create(request: Request, instance_id: String, rest: Uri.Path) : CreateReactor = {
    NoopCreateReactor(request, instance_id, rest)
  }

  def retrieve(request: Request, instance_id: String, rest: Uri.Path) : RetrieveReactor = {
    NoopRetrieveReactor(request, instance_id, rest)
  }

  def update(request : Request, instance_id: String, rest: Uri.Path) : UpdateReactor = {
    NoopUpdateReactor(request, instance_id, rest)
  }

  def delete(request : Request, instance_id: String, rest: Uri.Path) : DeleteReactor = {
    NoopDeleteReactor(request, instance_id, rest)
  }

  def query(request : Request, instance_id: String, rest: Uri.Path) : QueryReactor = {
    NoopQueryReactor(request, instance_id, rest)
  }

  def add(request : Request, instance_id: String, facet: String, rest: Uri.Path) : AddReactor = {
    NoopAddReactor(request, instance_id, facet, rest)
  }

  def get(request : Request, instance_id: String, facet: String, rest: Uri.Path) : GetReactor = {
    NoopGetReactor(request, instance_id, facet, rest)
  }

  def set(request : Request, instance_id: String, facet: String, rest: Uri.Path) : SetReactor = {
    NoopSetReactor(request, instance_id, facet, rest)
  }

  def remove(request : Request, instance_id: String, facet: String, rest: Uri.Path) : RemoveReactor = {
    NoopRemoveReactor(request, instance_id, facet, rest)
  }

  def find(request : Request, instance_id: String, facet: String, rest: Uri.Path) : FindReactor = {
    NoopFindReactor(request, instance_id, facet, rest)
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


trait NoopReactor extends Reactor with Describable {
  def apply(request: Request) : Future[Response] = Future.successful( NoopResponse )
}

case class NoopCreateReactor(request : Request, instance_id: String, rest: Uri.Path)
  extends CreateReactor with NoopReactor
case class NoopRetrieveReactor(request : Request, instance_id: String, rest: Uri.Path)
  extends RetrieveReactor with NoopReactor
case class NoopUpdateReactor(request : Request, instance_id: String, rest: Uri.Path)
  extends UpdateReactor with NoopReactor
case class NoopDeleteReactor(request : Request, instance_id: String, rest: Uri.Path)
  extends DeleteReactor with NoopReactor
case class NoopQueryReactor(request : Request, instance_id: String, rest: Uri.Path)
  extends QueryReactor with NoopReactor
case class NoopAddReactor(request : Request, instance_id: String, facet: String, rest: Uri.Path)
  extends AddReactor with NoopReactor
case class NoopGetReactor(request : Request, instance_id: String, facet: String, rest: Uri.Path)
  extends GetReactor with NoopReactor
case class NoopSetReactor(request : Request, instance_id: String, facet: String, rest: Uri.Path)
  extends SetReactor with NoopReactor
case class NoopRemoveReactor(request : Request, instance_id: String, facet: String, rest: Uri.Path)
  extends RemoveReactor with NoopReactor
case class NoopFindReactor(request : Request, instance_id: String, facet: String, rest: Uri.Path)
  extends FindReactor with NoopReactor

