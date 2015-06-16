/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                     *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                     *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                     *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                     *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.storage.api

import java.io.Closeable
import java.net.URI
import play.api.Configuration

import scrupal.storage.impl.StorageConfigHelper
import scrupal.utils.{ ConfigHelpers, Registry, ScrupalComponent, Registrable }

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success, Try }

/** Context For Storage
  *
  * When connecting to a storage provider, subclasses of this instance provide all the details
  */
trait StorageContext extends Registrable[StorageContext] with Closeable with ScrupalComponent {
  implicit val executionContext : ExecutionContext = ExecutionContext.global
  def registry = StorageContext
  def driver : StorageDriver
  def uri : URI
  def store : Store

  def withStore[T](f : (Store) ⇒ T) : T = { f(store) }

  def hasSchema(name: String) : Boolean = store.hasSchema(name)

  def addSchema(design: SchemaDesign) = store.addSchema(design)

  def withSchema[T](schema : String)(f : (Schema) ⇒ T) : T = {
    store.withSchema(schema)(f)
  }

  def withCollection[T, S <: Storable](schema : String, collection : String)(f : (Collection[S]) ⇒ T) : T = {
    store.withCollection(schema,collection)(f)
  }

  def close() = Try {
    log.debug("Closing storage driver:" + driver.name)
    driver.close()
  } match {
    case Success(x) ⇒ log.debug("Closed StorageContext '" + id.name + "' successfully.")
    case Failure(x) ⇒ log.error("Failed to close StorageContext '" + id.name + "': ", x)
  }
}

object StorageContext extends Registry[StorageContext] with ScrupalComponent {
  val registrantsName : String = "storageContext"
  val registryName : String = "StorageContexts"

  def apply(id : Symbol, uri : URI, create: Boolean = true) : Option[StorageContext] = {
    StorageDriver(uri) map { driver ⇒ driver.makeContext(id, uri, create) }
  }

  def fromConfiguration(id : Symbol, conf : Option[Configuration] = None) : Option[StorageContext] = {
    val topConfig = conf.getOrElse(ConfigHelpers.default())
    val helper = new StorageConfigHelper(topConfig)
    val config = helper.getStorageConfig
    config.getConfig("db.scrupal") flatMap { cfg ⇒ fromSpecificConfig(id, cfg) } orElse {
      fromURI(id, "scrupal_mem://localhost/scrupal")
    }
  }

  def fromSpecificConfig(id : Symbol, config : Configuration) : Option[StorageContext] = {
    config.getString("uri") flatMap { uri ⇒ fromURI(id, uri) }
  }

  def fromURI(id : Symbol, uriStr : String) : Option[StorageContext] = {
    getRegistrant(id) filter { ctxt ⇒ ctxt.uri.toString == uriStr } orElse {
      val uri = new URI(uriStr)
      StorageDriver.apply(uri).map { driver ⇒ driver.makeContext(id,uri) }
    }
  }
}

