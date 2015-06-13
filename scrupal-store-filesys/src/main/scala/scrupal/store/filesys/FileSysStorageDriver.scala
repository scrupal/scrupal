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

package scrupal.storage.filesys

import java.net.URI

import scrupal.storage.api._
import scrupal.utils.{ TryWith, ScrupalComponent }

import scala.collection.mutable
import scala.util.{ Failure, Success, Try }

/** Title Of Thing.
  *
  * Description of thing
  */
object FileSysStorageDriver extends StorageDriver {
  def id = 'memory
  override val name : String = "Memory"
  override val scheme : String = "scrupal-mem"
  override def canOpen(url : URI) : Boolean = {
    url.getScheme == scheme && url.getAuthority == authority && url.getPort == -1 && url.getPath.length > 0
  }

  override def storeExists(name : String) : Boolean = {
    stores.contains(name)
  }

  def open(url : URI, create : Boolean = false) : Option[FileSysStore] = {
    if (!canOpen(url))
      return None
    stores.get(url.getPath) match {
      case Some(s) ⇒ Some(s)
      case None ⇒
        if (create) {
          val result = new FileSysStore(this, url)
          stores.put(url.getPath, result)
          Some(result)
        } else {
          None
        }
    }
  }

  def withStore[T](uri : URI)(f : Store ⇒ T) : T = {
    stores.get(uri.getPath) match {
      case Some(s) ⇒ f(s)
      case None    ⇒ toss(s"No store found for $uri")
    }
  }

  def makeReference[S <: Storable](coll : Collection[S], id : ID) : Reference[S] = {
    require(coll.isInstanceOf[FileSysCollection[S]])
    FileSysReference[S](coll.asInstanceOf[FileSysCollection[S]], id)
  }

  def makeContext(id : Symbol) : FileSysStorageContext = FileSysStorageContext(id)

  def makeStorage(uri : URI) : Store = FileSysStore(this, uri)

  def makeSchema(store : Store, name : String, design : SchemaDesign) = {
    require(store.isInstanceOf[FileSysStore])
    FileSysSchema(store, name, design)
  }

  def makeCollection[S <: Storable](schema : Schema, name : String) : Collection[S] = {
    require(schema.isInstanceOf[FileSysSchema])
    FileSysCollection[S](schema, name)
  }

  override def close() : Unit = {
    for ((name, s) ← stores) s.close
    stores.clear()
  }

  private val stores : mutable.HashMap[String, FileSysStore] = new mutable.HashMap[String, FileSysStore]
  private val authority : String = "localhost"
}
