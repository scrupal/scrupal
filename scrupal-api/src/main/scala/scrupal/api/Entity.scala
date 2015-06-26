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

import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.server.PathMatcher.{Matched, Unmatched}
import akka.http.scaladsl.server.{PathMatcher1, PathMatcher}
import akka.http.scaladsl.server.PathMatchers._
import scrupal.storage.api.Storable
import scrupal.utils.{Enablee, Registrable}

import scala.concurrent.Future
import scala.language.implicitConversions

trait EntityCollectionReactor extends Reactor
trait EntityInstanceReactor extends Reactor

/** Provider of EntityCommands for command parameters
  * This
  * TODO: Finish documenting EntityCommandProvider interface as it is necessary to understanding entities
  */
trait EntityProvider extends PluralityProvider {

  val pluralEntityMatcher: PathMatcher1[String] = pluralMatcher / /*instance_id=*/Segment

  val singularEntityMatcher : PathMatcher[(String,String)] = singularMatcher / /*instance_id=*/Segment / /*facet_name*/ Segment

  override def provide(request: Request) : Option[Reactor] = {
    None
    /* FIXME: EntityProvider.provide
    singularEntityMatcher(request.path) match {
      case Matched(rest, extractions) ⇒
        val (instance_id, facet_name) = extractions
        val details : String = rest.toString()
        request.method match {
          case HttpMethods.POST ⇒ Some(add(instance_id, facet_name, details))
          case HttpMethods.GET ⇒ Some(get(instance_id, facet_name, details))
          case HttpMethods.PUT ⇒  Some(set(rinstance_id, facet_name, details))
          case HttpMethods.DELETE ⇒ Some(remove(instance_id, facet_name, details))
          case HttpMethods.OPTIONS ⇒  Some(find(instance_id, facet_name, details))
          case _ ⇒ noSuchMessage(request, s"$request not supported on entity $label.")
        }
      case Unmatched ⇒
        pluralEntityMatcher(request.path) match {
          case Matched(rest, extractions) ⇒
            val instance_id = extractions._1
            request.method match {
              case HttpMethods.POST ⇒ Some(create(request, instance_id, details))
              case HttpMethods.GET ⇒ Some(retrieve(request, instance_id, details))
              case HttpMethods.PUT ⇒ Some(update(request, instance_id, details))
              case HttpMethods.DELETE ⇒ Some(delete(request, instance_id, details))
              case HttpMethods.OPTIONS ⇒ Some(query(request, instance_id, details))
              case _ ⇒ noSuchMessage(request, s"$request not supported on entitty $label.")
            }
          case Unmatched ⇒ None
        }
    } */
  }

  def noSuchMessage(req : Request, msg: String) : Option[Reactor] = {
    Some(new Reactor {
      val name = "Unavailable"
      val description = "A Reactor that generates an Unavailable response"
      val request : Request = req
      def apply(request: DetailedRequest): Future[Response] = { Future.successful( ErrorResponse(msg, Unavailable) ) }
    })
  }

  /** CREATE ENTITY (POST/plural) - Create a new entity from scratch
    * This is a command on the entity type's container to insert a new entity. The instance should be created
    * with the provided identifier, if possible. If not, respond with an error.
    * @return A Create object that can perform the task from an Actor (i.e. in an isolated context)
    */
  def create(details: String) : CreateReactor

  /** Query Command (OPTIONS/plural)
    * @return The QueryReaction to generate the response for the Query request
    */
  def query(details: String) : QueryReactor


  /** Retrieve Command (GET/plural) - Retrieve an existing entity by its identifier
    * This is a command on the entity type's contain to retrieve a specific entity. The full instance should be
    * retrieved, including all retrievable facets.
    * @return
    */
  def retrieve(instance_id: String, details: String) : RetrieveReactor
  def retrieve(instance_id: Long, details: String) : RetrieveReactor

  /** Update Command (PUT/plural) - Updates all or a few of the fields of an entity
    * @return
    */
  def update(instance_id: String, details: String) : UpdateReactor
  def update(instance_id: Long, details: String) : UpdateReactor

  /** Delete Command (DELETE/plural) */
  /** Delete an entity
    * @return THe DeleteReaction to generate the response for the Delete request
    */
  def delete(instance_id: String, details: String) : DeleteReactor
  def delete(instance_id: Long, details: String) : DeleteReactor

  /** XXX Command (OPTIONS/singular) */
  def find(instance_id: String, facet: String, details: String) : FindReactor
  def find(instance_id: Long, facet: String, details: String) : FindReactor

  /** Create Facet Command (POST/singular) */
  def add(instance_id: String, facet: String, details: String) : AddReactor
  def add(instance_id: Long, facet: String, details: String) : AddReactor

  /** RetrieveAspect Command (GET/singular) */
  def get(instance_id: String, facet: String, facet_id: String, details: String) : GetReactor
  def get(instance_id: Long, facet: String, facet_id: String, details: String) : GetReactor

  /** UpdateAspect Command (PUT/singular) */
  def set(instance_id: String, facet: String, facet_id: String, details: String) : SetReactor
  def set(instance_id: Long, facet: String, facet_id: String, details: String) : SetReactor

  /** XXX Command (DELETE/singular) */
  def remove(instance_id: String, facet: String, facet_id: String, details: String) : RemoveReactor
  def remove(instance_id: Long, facet: String, facet_id: String, details: String) : RemoveReactor
}

trait CreateReactor extends EntityCollectionReactor {
  val name : String = "Create"
  val description = "Create a specific instance of the entity and insert it in the entity collection."
}

trait RetrieveReactor extends EntityCollectionReactor {
  val name : String = "Retrieve"
  val description = "Retrieve a specific instance of the entity from the entity collection."
}

trait UpdateReactor extends EntityCollectionReactor {
  val name : String = "Update"
  val description = "Update a specific instance of the entity."
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
}

trait GetReactor extends EntityInstanceReactor {
  val name : String = "Get"
  val description = "Retrieve a facet from a specific entity in the collection."
}

trait SetReactor extends EntityInstanceReactor {
  val name : String = "Set"
  val description = "Update a facet from a specific entity in the collection."
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

  def create(details: String) : CreateReactor = { NoopCreateReactor(details) }

  def retrieve(instance_id: String, details: String) : RetrieveReactor = {
    NoopRetrieveReactor(instance_id, details)
  }
  def retrieve(instance_id: Long, details: String) : RetrieveReactor = {
    retrieve (instance_id.toString, details)
  }

  def update(instance_id: String, details: String) : UpdateReactor = {
    NoopUpdateReactor(instance_id, details)
  }
  def update(instance_id: Long, details: String) : UpdateReactor = {
    update(instance_id.toString, details)
  }

  def delete(instance_id: String, details: String) : DeleteReactor = {
    NoopDeleteReactor(instance_id, details)
  }
  def delete(instance_id: Long, details: String) : DeleteReactor = {
    delete(instance_id.toString, details)
  }

  def query(details: String) : QueryReactor = {
    NoopQueryReactor(details)
  }

  def add(instance_id: String, facet: String, details: String) : AddReactor = {
    NoopAddReactor(instance_id, facet, details)
  }
  def add(instance_id: Long, facet: String, details: String) : AddReactor = {
    add(instance_id.toString, facet, details)
  }

  def get(instance_id: String, facet: String, facet_id: String, details: String) : GetReactor = {
    NoopGetReactor(instance_id, facet, facet_id, details)
  }
  def get(instance_id: Long, facet: String, facet_id: String, details: String) : GetReactor = {
    get(instance_id.toString, facet, facet_id, details)
  }

  def set(instance_id: String, facet: String, facet_id: String, details: String) : SetReactor = {
    NoopSetReactor(instance_id, facet, facet_id, details)
  }
  def set(instance_id: Long, facet: String, facet_id: String, details: String) : SetReactor = {
    set(instance_id.toString, facet, facet_id, details)
  }

  def remove(instance_id: String, facet: String, facet_id: String, details: String) : RemoveReactor = {
    NoopRemoveReactor(instance_id, facet, facet_id, details)
  }
  def remove(instance_id: Long, facet: String, facet_id: String, details: String) : RemoveReactor = {
    remove(instance_id.toString, facet, facet_id, details)
  }

  def find(instance_id: String, facet: String, details: String) : FindReactor = {
    NoopFindReactor(instance_id, facet, details)
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


trait NoopReactor extends Reactor with Describable {
  def apply(request: DetailedRequest) : Future[Response] = Future.successful( NoopResponse )
}

case class NoopCreateReactor(details: String) extends CreateReactor with NoopReactor
case class NoopRetrieveReactor(instance_id: String, details: String)
  extends RetrieveReactor with NoopReactor
case class NoopUpdateReactor(instance_id: String, details: String)
  extends UpdateReactor with NoopReactor
case class NoopDeleteReactor(instance_id: String, details: String)
  extends DeleteReactor with NoopReactor
case class NoopQueryReactor(details: String)
  extends QueryReactor with NoopReactor
case class NoopAddReactor(instance_id: String, facet: String, details: String)
  extends AddReactor with NoopReactor
case class NoopGetReactor(instance_id: String, facet: String, facet_id: String, details: String)
  extends GetReactor with NoopReactor
case class NoopSetReactor(instance_id: String, facet: String, facet_id: String, details: String)
  extends SetReactor with NoopReactor
case class NoopRemoveReactor(instance_id: String, facet: String, facet_id: String, details: String)
  extends RemoveReactor with NoopReactor
case class NoopFindReactor(instance_id: String, facet: String, details: String)
  extends FindReactor with NoopReactor

