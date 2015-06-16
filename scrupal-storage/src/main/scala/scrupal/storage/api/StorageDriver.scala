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

import com.typesafe.config.Config
import play.api.Configuration
import scrupal.storage.impl.StorageConfigHelper
import scrupal.utils.{ ScrupalComponent, Registry, Registrable }

import scala.concurrent.{ExecutionContext, Future}

trait StorageDriver extends AutoCloseable with Registrable[StorageDriver] with ScrupalComponent {
  def name : String
  def scheme : String

  def isDriverFor(uri : URI) : Boolean = {
    uri.getScheme == scheme && uri.getPath.length > 0
  }

  def canOpen(uri : URI) : Boolean = {
    isDriverFor(uri)
  }

  def open(uri : URI, create : Boolean = false) : Option[Store]
  def withStore[T](uri : URI, create : Boolean = false)(f : (Store) ⇒ T) : T
  def makeContext(id : Symbol, uri: URI, create: Boolean = false) : StorageContext
  def makeReference[S <: Storable](coll : Collection[S], id : ID) : Reference[S]
  def makeStorage(uri : URI) : Store
  def makeSchema(store : Store, name : String, design : SchemaDesign) : Schema
  def makeCollection[S <: Storable](schema : Schema, name : String) : Collection[S]
  def registry = StorageDriver
  def isOpen : Boolean = true
  def closeF(implicit ec: ExecutionContext) = Future {
    if (isOpen)
      close()
    isOpen
  }
}

object StorageDriver extends Registry[StorageDriver] {
  override def registryName = "StorageDrivers"
  override def registrantsName = "driver"

  def apply(config: Config) : Option[StorageDriver] = {
    apply(Configuration(config))
  }

  def apply(config : Configuration) : Option[StorageDriver] = {
    apply(config, "scrupal")
  }

  def apply(configuration : Configuration, name : String) : Option[StorageDriver] = {
    val config = StorageConfigHelper(configuration).getStorageConfig
    config.getString(name + ".uri") match {
      case Some(str) ⇒
        apply(new URI(str))
      case None ⇒
        None
    }
  }

  def apply(uri : URI) : Option[StorageDriver] = {
    _registry.values.find { driver : StorageDriver ⇒ driver.isDriverFor(uri) }
  }
}
