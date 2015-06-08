/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                          *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                          *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                          *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                          *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.storage.api

import java.net.URI

import scrupal.utils.{ Registry, Registrable }

trait StorageDriver extends AutoCloseable with Registrable[StorageDriver] {
  def name : String
  def scheme : String
  def canOpen(url : URI) : Boolean
  def open(url : URI, create : Boolean = false) : Option[Storage]
  def storageExists(url : URI) : Boolean = {
    if (!canOpen(url)) return false
    storageExists(url.getPath)
  }
  def storageExists(storage_name : String) : Boolean
  def makeContext(id : Symbol) : StorageContext
  def makeReference[T, S <: Storable[T, S]](coll : Collection[T, S], id : ID) : Reference[T, S]
  def registry = StorageDriver
}

object StorageDriver extends Registry[StorageDriver] {
  override def registryName = "StorageDrivers"
  override def registrantsName = "driver"

  def driverForURI(url : URI) : StorageDriver = {
    _registry.values.find { driver : StorageDriver ⇒ driver.canOpen(url) } match {
      case Some(driver) ⇒
        driver
      case None ⇒
        throw new Exception(s"No StorageDriver for $url")
    }
  }

  lazy val empty = new StorageDriver {
    def id = 'empty
    def name = "empty"
    def scheme = "empty"
    def canOpen(url : URI) = false
    def open(url : URI, create : Boolean) : Option[Storage] = ???
    def storageExists(name : String) = false
    def makeReference[T, S <: Storable[T, S]](coll : Collection[T, S], id : ID) : Reference[T, S] = ???
    def close() = ???
    def makeContext(id : Symbol) : StorageContext = ???
  }
}
