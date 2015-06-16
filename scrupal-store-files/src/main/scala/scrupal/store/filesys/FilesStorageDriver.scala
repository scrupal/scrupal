/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
  *                                                                                                                   *
  * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
  *                                                                                                                   *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
  * with the License. You may obtain a copy of the License at                                                          *
  *                                                                                                                   *
  *    http://www.apache.org/licenses/LICENSE-2.0                                                                     *
  *                                                                                                                   *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
  * the specific language governing permissions and limitations under the License.                                     *
  * ********************************************************************************************************************
  */

package scrupal.store.files

import java.net.URI

import scrupal.storage.api._
import scrupal.storage.mem.MemoryStorageDriver._

import scala.collection.mutable

/** Title Of Thing.
  *
  * Description of thing
  */
object FilesStorageDriver extends StorageDriver {
  def id = 'files
  override val name : String = "Files"
  override val scheme : String = "scrupal-files"
  private val authority : String = "localhost"
  private val stores : mutable.HashMap[URI, FilesStore] = new mutable.HashMap[URI, FilesStore]

  override def canOpen(uri : URI) : Boolean = {
    super.canOpen(uri) && uri.getAuthority == authority && uri.getPort == -1 && storeExists(uri)
  }

  def storeExists(uri : URI) : Boolean = {
    stores.get(uri) match {
      case Some(fileStore) ⇒ fileStore.exists
      case None ⇒
        val fs = FilesStore(this, uri)
        fs. exists
    }
  }

  def open(uri : URI, create : Boolean = false) : Option[FilesStore] = {
    if (!canOpen(uri))
      return None
    stores.get(uri) match {
      case Some(s) ⇒ Some(s)
      case None ⇒
        if (create) {
          val result = new FilesStore(this, uri)
          stores.put(uri, result)
          Some(result)
        } else {
          None
        }
    }
  }

  def withStore[T](uri : URI, create: Boolean = false)(f : Store ⇒ T) : T = {
    stores.get(uri) match {
      case Some(s) ⇒ f(s)
      case None ⇒
        open(uri, create) match {
          case Some(store) ⇒ f(store)
          case None ⇒
            toss(s"No store found for $uri")
        }

    }
  }

  def makeReference[S <: Storable](coll : Collection[S], id : ID) : Reference[S] = {
    require(coll.isInstanceOf[FilesCollection[S]])
    StorableReference[S](coll, id)
  }

  def makeContext(id : Symbol, uri : URI, create: Boolean) : FilesStorageContext = {
    withStore(uri, create) { store ⇒
      FilesStorageContext(id, uri, store.asInstanceOf[FilesStore])
    }
  }

  def makeStorage(uri : URI) : Store = FilesStore(this, uri)

  def makeSchema(store : Store, name : String, design : SchemaDesign) = {
    require(store.isInstanceOf[FilesStore])
    FilesSchema(store, name, design)
  }

  def makeCollection[S <: Storable](schema : Schema, name : String) : Collection[S] = {
    require(schema.isInstanceOf[FilesSchema])
    FilesCollection[S](schema, name)
  }

  override def close() : Unit = {
    for ((name, s) ← stores) s.close
    stores.clear()
  }

}
