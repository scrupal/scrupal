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

package scrupal.storage.api

import java.net.URI

import scrupal.utils.ScrupalComponent

import scala.concurrent.{ExecutionContext, Future}

/** An abstract trait for a reference to something that is fetchable back to its original type */
trait Reference[S <: Storable] {
  def fetch(implicit sc: StoreContext) : Future[Option[S]]
}

/** A Fast Resolving Reference
  * Directly contains the collection and ID to use to resolve the reference quickly with a simple fetch by id.
  * Fast Resolving references cannot be stored, they are memory only objects.
 */
case class FastReference[S <: Storable](collection : Collection[S], id : ID) extends Reference[S] {
  def fetch(implicit sc: StoreContext) : Future[Option[S]] = {
    collection.fetch(id)(sc.ec)
  }
}

object FastReference extends ScrupalComponent {

  def apply[S <: Storable](collection: Collection[S], obj : S) : FastReference[S] = {
    new FastReference(collection, obj.getPrimaryId())
  }

  def apply[S <: Storable](schema : String, collection : String, obj : S)(implicit sc: StoreContext) : Reference[S]= {
    sc.withCollection(schema, collection) { coll : Collection[_] ⇒
      new FastReference(coll.asInstanceOf[Collection[S]], obj.getPrimaryId())
    }
  }
}

case class StorableReference[S <: Storable](
  uri: URI,
  schemaName: String,
  collectionName: String,
  id : ID
) extends Reference[S] with Storable {
  def fetch(implicit ec: ExecutionContext) : Future[Option[S]] = {
    Storage.fromURI(uri, create=false).flatMap { context : StoreContext ⇒ this.fetch(context) }
  }
  def fetch(implicit sc: StoreContext) : Future[Option[S]] = {
    sc.withCollection(schemaName, collectionName) { coll : Collection[_] ⇒
      coll.asInstanceOf[Collection[S]].fetch(id)(sc.ec)
    }
  }
}

object StorableReference {
  def apply[S <: Storable](schema : Schema, collection : String, obj : S) : StorableReference[S]= {
    StorableReference(schema.store.uri, schema.name, collection, obj.getPrimaryId())
  }

  def apply[S <: Storable](store: Store, schema: String, collection: String, obj: S) : StorableReference[S] = {
   StorableReference(store.uri, schema, collection, obj.getPrimaryId())
  }

  def apply[S <:Storable](coll: Collection[S], obj: S) : StorableReference[S] = {
    StorableReference[S](coll.schema.store.uri, coll.schema.name, coll.name, obj.getPrimaryId())
  }

  def apply[S <:Storable](coll: Collection[S], objId: ID) : StorableReference[S] = {
    StorableReference[S](coll.schema.store.uri, coll.schema.name, coll.name, objId)
  }
}

