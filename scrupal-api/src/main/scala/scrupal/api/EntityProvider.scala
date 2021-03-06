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

trait EntityCreate extends EntityCollectionReactor {
  val description = "Create a specific instance of the entity and insert it in the entity collection."
}

trait EntityRetrieve extends EntityCollectionReactor {
  val description = "Retrieve a specific instance of the entity from the entity collection."
}

trait EntityInfo extends EntityCollectionReactor {
  val description = "Get information about a specific instance of the entity from the entity collection."
}

trait EntityUpdate extends EntityCollectionReactor {
  val description = "Update a specific instance of the entity."
}

trait EntityDelete extends EntityCollectionReactor {
  val description = "Delete a specific instance from the entity collection."
}

trait EntityQuery extends EntityCollectionReactor {
  val description = "Query the entity collection for an entity of a certain id or containing certain data."
}

trait EntityAdd extends EntityInstanceReactor {
  val description = "Create a facet on a specific entity in the collection."
}

trait EntityGet extends EntityInstanceReactor {
  val description = "Retrieve a facet from a specific entity in the collection."
}

trait EntityFacetInfo extends EntityInstanceReactor {
  val description = "Get information about a facet from a specific entity in the collection."
}

trait EntitySet extends EntityInstanceReactor {
  val description = "Update a facet from a specific entity in the collection."
}

trait EntityRemove extends EntityInstanceReactor {
  val description = "Delete a facet from a specific entity in the collection."
}

trait EntityFind extends EntityInstanceReactor {
  val description = "Query a specific entity in the collection for a facet of a certain id or containing certain data."
}


/** Router For Entities
  *
  * This maps
  */
trait EntityProvider extends PluralProvider {

  final val pluralRoutes: ReactionRoutes = {
    case GET(p"/${long(id)}$rest*") ⇒
      retrieve(id, rest)
    case GET(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>$rest*") ⇒
      retrieve(id, rest)
    case HEAD(p"/${long(id)}$rest*") ⇒
      info(id, rest)
    case HEAD(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>$rest*") ⇒
      info(id, rest)
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
    case DELETE(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>$rest*") ⇒
      delete(id, rest)
  }
  final val singularRoutes: ReactionRoutes = {
    case GET(p"/${long(id)}/$facet/$facet_id$rest*") ⇒
      get(id, facet, facet_id, rest)
    case GET(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet/$facet_id$rest*") ⇒
      get(id, facet, facet_id, rest)
    case HEAD(p"/${long(id)}/$facet/$facet_id$rest*") ⇒
      facetInfo(id, facet, facet_id, rest)
    case HEAD(p"/$id<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet/$facet_id$rest*") ⇒
      facetInfo(id, facet, facet_id, rest)
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

  def create(details: String) : EntityCreate

  /** Query Command (OPTIONS/plural)
    * @return The QueryReaction to generate the response for the Query request
    */
  def query(details: String) : EntityQuery

  /** Retrieve Command (GET/plural) - Retrieve an existing entity by its identifier
    * This is a command on the entity type's contain to retrieve a specific entity. The full instance should be
    * retrieved, including all retrievable facets.
    * @return
    */
  def retrieve(instance_id: String, details: String) : EntityRetrieve

  def retrieve(instance_id: Long, details: String) : EntityRetrieve

  def info(instance_id: String, details: String) : EntityInfo

  def info(instance_id: Long, details: String) : EntityInfo

  def update(instance_id: String, details: String) : EntityUpdate

  def update(instance_id: Long, details: String) : EntityUpdate

  def delete(instance_id: String, details: String) : EntityDelete

  def delete(instance_id: Long, details: String) : EntityDelete

  def find(instance_id: String, facet: String, details: String) : EntityFind

  def find(instance_id: Long, facet: String, details: String) : EntityFind

  def add(instance_id: String, facet: String, details: String) : EntityAdd

  def add(instance_id: Long, facet: String, details: String) : EntityAdd

  def get(instance_id: String, facet: String, facet_id: String, details: String) : EntityGet

  def get(instance_id: Long, facet: String, facet_id: String, details: String) : EntityGet

  def facetInfo(instance_id: String, facet: String, facet_id: String, details: String) : EntityFacetInfo

  def facetInfo(instance_id: Long, facet: String, facet_id: String, details: String) : EntityFacetInfo

  def set(instance_id: String, facet: String, facet_id: String, details: String) : EntitySet

  def set(instance_id: Long, facet: String, facet_id: String, details: String) : EntitySet

  def remove(instance_id: String, facet: String, facet_id: String, details: String) : EntityRemove

  def remove(instance_id: Long, facet: String, facet_id: String, details: String) : EntityRemove
}
