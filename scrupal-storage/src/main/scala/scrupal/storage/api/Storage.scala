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
import scrupal.utils.{ConfigHelpers, ScrupalComponent}

import scala.concurrent.{ExecutionContext, Future}

/** Title Of Thing.
  *
  * Description of thing
  */
object Storage extends ScrupalComponent {

  def apply(id : Symbol, uri : URI, create: Boolean = true)(implicit ec: ExecutionContext)  : Future[StoreContext] = {
    fromURI(id, uri, create)
  }

  def fromConfigFile(id : Symbol, path: String, name : String = "scrupal", create: Boolean = false)(implicit ec: ExecutionContext) : Future[StoreContext] = {
    val helper = StorageConfigHelper.fromConfigFile(path)
    val config = helper.getStorageConfig
    val config_name = s"storage.$name"
    config.getConfig(config_name) match {
      case Some(cfg) ⇒
        fromSpecificConfig(id, cfg, create )
      case None ⇒
        Future { toss(s"Failed to find storage configuration for $config_name") }
    }
  }

  def fromConfiguration(id : Symbol,
    conf: Option[Configuration] = None, name : String = "scrupal", create: Boolean = false)(implicit ec: ExecutionContext) : Future[StoreContext] = {
    val topConfig = conf.getOrElse(ConfigHelpers.default())
    val helper = new StorageConfigHelper(topConfig)
    val config = helper.getStorageConfig
    val config_name = s"storage.$name"
    config.getConfig(config_name) match {
      case Some(cfg) ⇒
        fromSpecificConfig(id, cfg)
      case None ⇒
        Future { toss(s"Failed to find storage configuration for $config_name") }
    }
  }

  def fromSpecificConfig(id : Symbol, config : Configuration, create: Boolean = false)(implicit ec: ExecutionContext) : Future[StoreContext] = {
    config.getString("uri") match {
      case Some(uri) ⇒
        fromURI(id, uri, create)
      case None ⇒
        Future { toss("No 'uri' element of configuration") }
    }
  }

  def fromURI(id : Symbol, uriStr : String, create: Boolean)(implicit ec: ExecutionContext) : Future[StoreContext] = {
    fromURI(id, new URI(uriStr), create)
  }

  def fromURI(id : Symbol, uri: URI, create: Boolean = false)(implicit ec: ExecutionContext) : Future[StoreContext] = {
    StorageDriver(uri).flatMap { driver ⇒
      driver.open(uri, create) map { store ⇒
        StoreContext(id, driver, store)(ec)
      }
    }
  }
}
