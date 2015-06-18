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

package scrupal.storage.impl

import java.net.URI
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

import scrupal.storage.api._

import scala.collection.JavaConverters._
import scala.collection.concurrent
import scala.concurrent.{ExecutionContext, Future}

trait CommonStorageDriver extends StorageDriver {

  protected val stores : concurrent.Map[URI,Store] = new ConcurrentHashMap[URI,Store]().asScala

  def isDriverFor(uri : URI) : Boolean = {
    uri.getScheme == scheme && uri.getPath.length > 0
  }

  def storeExists(uri: URI) : Boolean = {
    stores.contains(uri)
  }

  def canOpen(uri : URI) : Boolean = {
    isDriverFor(uri) && storeExists(uri)
  }

  override def close() : Unit = {
    for ((name, s) ← stores)
      s.close()
    stores.clear()
  }

  def exists: Boolean = true

  def created: Instant = Instant.now

  def size: ID = stores.size

  def drop(implicit ec: ExecutionContext) : Future[WriteResult] = {
    toss("Cannot drop a driver singleton")
  }

}


