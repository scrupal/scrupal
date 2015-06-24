/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
  *                                                                                                            *
  * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
  *                                                                                                            *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
  * with the License. You may obtain a copy of the License at                                                          *
  *                                                                                                            *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                     *
  *                                                                                                            *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
  * the specific language governing permissions and limitations under the License.                                     *
  * ********************************************************************************************************************
  */

package scrupal.store.mem

import java.time.Instant

import scrupal.storage.impl.CommonSchema

import scrupal.storage.api._

import scala.concurrent.{ExecutionContext, Future}

/** A Schema of Collections Stored In Memory */
case class MemorySchema private[mem] (store : MemoryStore, name : String, design : SchemaDesign) extends CommonSchema {
  val created = Instant.now()

  def dropCollection[S <: Storable](name: String)(implicit ec: ExecutionContext): Future[WriteResult] = Future {
    colls.remove(name)
    WriteResult.success()
  }

  def addCollection[S <: Storable](name: String)(implicit ec: ExecutionContext): Future[Collection[S]] = Future {
    val coll = MemoryCollection[S](this, name)
    colls.put(name, coll)
    coll
  }
}
