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

import play.api.libs.json.JsObject
import scala.collection.mutable
import scrupal.utils.{Registrable, Registry}

/** How Scala things are identified
  * A long integer has a large enough domain and is fast for comparison. Yet, it may ultimately prove to be limiting.
  * So, in case we ever have to change this, we're calling it an Identifier throughout the api
  */

// class Identifier extends AbstractIdentifier

/** Some Thing that is storable and identifiable by an Identifier (long integer) as a unique ID within some storage
  * realm (e.g. adatabase).
  */
trait Storable  {

  /** The identifier for this Identifiable.
    * Note that it is optional and private and a variable. There's a reason for that. The storage system, not the
    * creator of this object, gets to specify the ID that works for that storage system. If this was a public value
    * then updating the ID after it was created in the database means we would have to create a whole new object,
    * which could be HUGE, just to update on Identifier integer. The cost of that purely functional copying is just too
    * high in this case and the clutter it introduces in the constructors of subclasses is unforgiving. So,
    * we make it an optional var. Optional so it doesn't have to be specified at construction time. A var so it can
    * be updated by the storage system once the thing is created in the DB. Private so that nobody but this class can
    * do the modification -- i.e. if we're going to break the mutability rule, let's constrain the heck out of it!
    * This is our attempt to not spam server memory with lots of database object duplication.
    */
  val id : Option[Identifier] = None

  /** Mutation test.
    * @return true if the object has been identified (had its id value set)
    */
  final def isIdentified = id.isDefined

  /** All things are inherently convertible to Json.
    * We allow each subclass to define the most efficient way to convert itself to Json. Only JsObject may be
    * returned.
    * This default implementation yields a NotImplemented exception .. by design.
    * @return The JsObject representing this "thing"
    */
  def toJson : JsObject = ???
}

/** A generic trait for representing the multitude of ways a thing can be found for a storage system. */
trait FinderOf[Storable] {
  def apply() : Seq[Storable]
}

/** A mechanims for storing, retrieving, updating and deleting Storable things from some Storage system
  * Everything we Scrupal stores can be stored through an interface like this. It is even possible to compose the
  * storage so that a given entity can be stored to multiple locations concurrently. For example,
  * to the cache and to the database. Modules decide how their entity types are stored.
  * @tparam S
  */
trait StorageFor[S <: Storable] {

  def fetch(id: Identifier) : Option[S]

  def findAll() : Seq[S]

  def insert(entity: S) : Identifier

  def update(entity: S): Int

  def delete(entity: S) : Boolean

  final def fetch(oid: Option[Identifier]) : Option[S] = {
    oid match { case None => None ; case Some(id) => fetch(id) }
  }

  final def find( finder: FinderOf[S] ) : Seq[S] = {
    finder()
  }

  final def upsert(entity: S) : Identifier = {
    if (entity.id.isDefined) {
      update(entity) ; entity.id.get
    } else {
      insert(entity)
    }
  }
}

abstract class Reference[T <: Type]

   /*
case class MemoryStorage() extends Storage

case class CacheStorage() extends Storage

case class DatabaseStorage() extends Storage
     */

/** Implements storage for a Storable as a memory hash. Fast, cheap, and completely unreliable :)
  * This is the base class of a family of memory based storage classes. It uses a mutable hash to keep track of the
  * values and can convert them to/from some other format in memory. For example, we might store it as a compressed
  * string of Json. While this could be a concrete class it is declared abstract to encourage its users to provide
  * names for the INMEM type and the mapping functions. See [[scrupal.api.SimpleMemoryStorageFor]]
  * @param to THe mapping function from S to INMEM
  * @param from The mapping function from INMEM to S
  * @tparam S The type of Storable to be stored in this StorageFor object
  * @tparam INMEM The type of data that is stored in memory
  */
class AbstractMemoryStorageFor[S <:Storable, INMEM]( to: S => INMEM, from: INMEM => S) extends StorageFor[S] {
  private val store = mutable.HashMap[Long,INMEM]()
  private var nextIdValue : Long = 0L
  private def nextId() = { nextIdValue = nextIdValue + 1; nextIdValue  }

  def fetch(id: Identifier) : Option[S] = {
    val t : Option[INMEM] = store.get(id)
    if (t.isDefined) Some(from(t.get)) else None
  }

  def findAll() : Seq[S] = (store.values map { v => from(v) } ).toSeq

  def insert(entity: S): Identifier = {
    assert(entity.id.isEmpty)
    val id = nextId()
    store.put(id, to(entity) )
    id
  }

  def update(entity: S): Int = {
    assert(entity.id.isDefined)
    val id = entity.id.get
    store.put(id, to(entity));
    from(store.get(id).get)
    1
  }

  def delete(entity: S): Boolean = {
    assert(entity.id.isDefined)
    store.remove(entity.id.get).isDefined
  }
}

class SimpleMemoryStorageFor[S <: Storable] extends AbstractMemoryStorageFor[S,S]( {x => x}, {y => y} )

abstract class Store extends Registrable  {

}

object Stores extends Registry[Store] {
  val registryName = "Stores"
  val registrantsName = "store"
}

