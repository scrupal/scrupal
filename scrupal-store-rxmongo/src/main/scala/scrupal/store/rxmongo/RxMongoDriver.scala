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

package scrupal.store.rxmongo

import java.net.URI
import java.time.Instant

import scrupal.storage.api.{Store, WriteResult, StorageDriver}

import scala.concurrent.{Future, ExecutionContext}

class RXMongoDriver extends StorageDriver {
  override def isDriverFor(uri: URI): Boolean = false

  override def canOpen(uri: URI): Boolean = false

  override def addStore(uri: URI)(implicit ec: ExecutionContext): Future[Store] = ???

  override def scheme: String = ???

  override def name: String = ???

  override def storeExists(uri: URI): Boolean = ???

  override def open(uri: URI, create: Boolean)(implicit ec: ExecutionContext): Future[Store] = ???

  override def created: Instant = Instant.now()

  override def drop(implicit ec: ExecutionContext): Future[WriteResult] = ???

  override def size: Long = ???

  override def close(): Unit = ???

  override def id: Symbol = ???
}
