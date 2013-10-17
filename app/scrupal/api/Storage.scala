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

import scala.collection.mutable
import scrupal.utils.{Registrable, Registry}

/** A generic trait for representing the multitude of ways a thing can be found for a storage system. */
trait FinderOf[Storable] {
  def apply() : Seq[Storable]
}

trait AbstractStorage[ID, OID, T <: Identifiable[OID]] {
  def fetch(id: ID) : Option[T]
  def findAll() : Seq[T]
  def insert(entity: T) : ID
  def update(entity: T) : Int
  def delete(id: ID) : Boolean
  def delete(entity: T) : Boolean

  final def fetch(oid: Option[ID]) : Option[T] = {
    oid match { case None => None ; case Some(id) => fetch(id) }
  }

  final def find( finder: FinderOf[T] ) : Seq[T] = {
    finder()
  }

}

/** A mechanims for storing, retrieving, updating and deleting Storable things from some Storage system
  * Everything we Scrupal stores can be stored through an interface like this. It is even possible to compose the
  * storage so that a given entity can be stored to multiple locations concurrently. For example,
  * to the cache and to the database. Modules decide how their entity types are stored.
  * @tparam S
  */
trait StorageFor[S <: NumericIdentifiable] extends AbstractStorage[Identifier,Option[Identifier],S] {
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
    if (entity.id.isDefined) delete(entity.id.get) else false
  }

  def delete(id: Identifier) : Boolean = {
    store.remove(id).isDefined
  }
}

class SimpleMemoryStorageFor[S <: Storable] extends AbstractMemoryStorageFor[S,S]( {x => x}, {y => y} )

abstract class Store extends Registrable  {

}

object Stores extends Registry[Store] {
  val registryName = "Stores"
  val registrantsName = "store"
}

