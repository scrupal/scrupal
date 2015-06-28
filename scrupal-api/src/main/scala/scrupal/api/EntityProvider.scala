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

import play.api.routing.sird._

trait EntityCollectionReactor extends Reactor
trait EntityInstanceReactor extends Reactor

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


/** Router For Entities
  *
  * This maps
  */
trait EntityProvider extends PluralityProvider {

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

  final val pluralRoutes: ReactionRoutes = {
    case GET(p"/${long(id)}$rest*") ⇒
      retrieve(id, rest)
    case GET(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>$rest*") ⇒
      retrieve(id, rest)
    case OPTIONS(p"/$rest*") ⇒
      query(rest)
    case POST(p"/$rest*") ⇒
      create(rest)
    case PUT(p"/${long(id)}$rest*") ⇒
      update(id, rest)
    case PUT(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>$rest*") ⇒
      update(id, rest)
    case DELETE(p"/${long(id)}$rest*") ⇒
      delete(id, rest)
    case DELETE(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>}$rest*") ⇒
      delete(id, rest)
  }

  final val singularRoutes: ReactionRoutes = {
    case GET(p"/${long(id)}/$facet/$facet_id$rest*") ⇒
      get(id, facet, facet_id, rest)
    case GET(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet/$facet_id$rest*") ⇒
      get(id, facet, facet_id, rest)
    case OPTIONS(p"/${long(id)}/$facet$rest*") ⇒
      find(id, facet, rest)
    case OPTIONS(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet$rest*") ⇒
      find(id, facet, rest)
    case POST(p"/${long(id)}/$facet$rest*") ⇒
      add(id, facet, rest)
    case POST(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet$rest*") ⇒
      add(id, facet, rest)
    case PUT(p"/${long(id)}/$facet/$facet_id$rest*") ⇒
      set(id, facet, facet_id, rest)
    case PUT(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet/$facet_id$rest*") ⇒
      set(id, facet, facet_id, rest)
    case DELETE(p"/${long(id)}/$facet/$facet_id$rest*") ⇒
      remove(id, facet, facet_id, rest)
    case DELETE(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet/$facet_id$rest*") ⇒
      remove(id, facet, facet_id, rest)
  }
}
