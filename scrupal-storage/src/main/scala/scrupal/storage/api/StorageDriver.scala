/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                  *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                  *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                  *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                  *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.storage.api

import java.net.URI

import scrupal.utils.{Registry, Registrable }

import scala.concurrent.{Future,ExecutionContext}

trait StorageDriver extends StorageLayer with Registrable[StorageDriver] {
  def registry = StorageDriver

  def name : String

  def scheme : String

  def isDriverFor(uri : URI) : Boolean

  def storeExists(uri: URI) : Boolean

  def canOpen(uri : URI) : Boolean

  def open(uri : URI, create : Boolean = false)(implicit ec: ExecutionContext) : Future[Store]

  def addStore(uri: URI)(implicit ec: ExecutionContext) : Future[Store]

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

  def apply(uri : URI)(implicit ec: ExecutionContext) : Future[StorageDriver] = Future {
    _registry.values.find { driver : StorageDriver ⇒ driver.isDriverFor(uri) } match {
      case Some(driver) ⇒
        driver
      case None ⇒
        toss(s"No storage driver found for $uri")
    }
  }
}
