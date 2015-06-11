/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                   *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                   *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                   *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                   *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.storage.api

import java.net.URI

import scrupal.utils.{ ScrupalComponent, Registry, Registrable }

trait StorageDriver extends AutoCloseable with Registrable[StorageDriver] with ScrupalComponent {
  def name : String
  def scheme : String
  def canOpen(url : URI) : Boolean
  def open(url : URI, create : Boolean = false) : Option[Store]
  def withStore[T](uri : URI)(f : (Store) ⇒ T) : T
  def storeExists(url : URI) : Boolean = {
    if (!canOpen(url)) return false
    storeExists(url.getPath)
  }
  def storeExists(storage_name : String) : Boolean
  def makeContext(id : Symbol) : StorageContext
  def makeReference[S <: Storable[S]](coll : Collection[S], id : ID) : Reference[S]
  def makeStorage(uri : URI) : Store
  def makeSchema(store : Store, name : String, design : SchemaDesign) : Schema
  def makeCollection[S <: Storable[S]](schema : Schema, name : String) : Collection[S]
  def registry = StorageDriver
}

object StorageDriver extends Registry[StorageDriver] {
  override def registryName = "StorageDrivers"
  override def registrantsName = "driver"

  def apply(url : URI) : StorageDriver = {
    _registry.values.find { driver : StorageDriver ⇒ driver.canOpen(url) } match {
      case Some(driver) ⇒
        driver
      case None ⇒
        toss(s"No StorageDriver for $url")
    }
  }
}
