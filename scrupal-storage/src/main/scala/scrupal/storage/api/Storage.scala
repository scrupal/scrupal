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

import play.api.Configuration
import scrupal.storage.impl.StorageConfigHelper
import scrupal.store.mem.MemoryStorageDriver
import scrupal.utils.{ConfigHelpers, ScrupalComponent}

import scala.concurrent.{ExecutionContext, Future}

/** Title Of Thing.
  *
  * Description of thing
  */
object Storage extends ScrupalComponent {

  require(MemoryStorageDriver.isRegistered, "MemoryStorageDriver failed to register")

  def apply(uri : URI, create: Boolean = true)(implicit ec: ExecutionContext)  : Future[StoreContext] = {
    fromURI(uri, create)
  }

  def fromConfigFile(path: String, name : String = "scrupal", create: Boolean = false)(implicit ec: ExecutionContext) : Future[StoreContext] = {
    val helper = StorageConfigHelper.fromConfigFile(path)
    val config = helper.getStorageConfig
    val config_name = s"storage.$name"
    config.getConfig(config_name) match {
      case Some(cfg) ⇒
        fromSpecificConfig(cfg, create )
      case None ⇒
        Future { toss(s"Failed to find storage configuration for $config_name") }
    }
  }

  def fromConfiguration(
    conf: Option[Configuration] = None, name : String = "scrupal", create: Boolean = false)(implicit ec: ExecutionContext) : Future[StoreContext] = {
    val config = conf.getOrElse{
      val dflt = ConfigHelpers.default()
      val helper = new StorageConfigHelper(dflt)
      helper.getStorageConfig
    }
    val config_name = s"storage.$name"
    config.getConfig(config_name) match {
      case Some(cfg) ⇒
        fromSpecificConfig(cfg, create)
      case None ⇒
        Future { toss(s"Failed to find storage configuration for $config_name") }
    }
  }

  def fromSpecificConfig(config : Configuration, create: Boolean = false)(implicit ec: ExecutionContext) : Future[StoreContext] = {
    config.getString("uri") match {
      case Some(uri) ⇒
        fromURI(uri, create)
      case None ⇒
        Future { toss("No 'uri' element of configuration") }
    }
  }

  def fromURI(uriStr : String, create: Boolean)(implicit ec: ExecutionContext) : Future[StoreContext] = {
    fromURI(new URI(uriStr), create)
  }

  def fromURI(uri: URI, create: Boolean = false)(implicit ec: ExecutionContext) : Future[StoreContext] = {
    StorageDriver(uri).flatMap { driver ⇒
      driver.open(uri, create) map { store ⇒
        StoreContext(driver, store)(ec)
      }
    }
  }
}
