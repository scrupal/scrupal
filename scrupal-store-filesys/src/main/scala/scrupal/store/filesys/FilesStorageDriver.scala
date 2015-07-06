/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
  *                                                                                                                  *
  * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
  *                                                                                                                  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
  * with the License. You may obtain a copy of the License at                                                          *
  *                                                                                                                  *
  *   http://www.apache.org/licenses/LICENSE-2.0                                                                     *
  *                                                                                                                  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
  * the specific language governing permissions and limitations under the License.                                     *
  * ********************************************************************************************************************
  */

package scrupal.store.files

import java.net.URI

import scrupal.storage.api._
import scrupal.storage.impl.CommonStorageDriver

import java.io.File

import scala.concurrent.{Future, ExecutionContext}

/** Title Of Thing.
  *
  * Description of thing
  */
object FilesStorageDriver extends CommonStorageDriver {
  def id = 'files
  override val name : String = "Files"
  override val scheme : String = "scrupal-files"
  private val authority : String = "localhost"

  {
    FilesStorageInfo.registerSerializers
  }

  override def isDriverFor(uri: URI) : Boolean = {
    super.isDriverFor(uri) && uri.getAuthority == authority && uri.getPort == -1
  }

  override def storeExists(uri : URI) : Boolean = {
    stores.get(uri) match {
      case Some(fileStore) ⇒
        fileStore.asInstanceOf[FilesStore].dir.canRead
      case None ⇒
        val dir = new File(uri.getPath)
        dir.canRead
    }
  }

  def addStore(uri: URI)(implicit ec: ExecutionContext) : Future[Store] = Future {
    stores.get(uri) match {
      case Some(store) ⇒
        toss("Store at $uri already exists")
      case None ⇒
        val result = FilesStore(uri)
        stores.put(uri, result)
        result
    }
  }

  def open(uri : URI, create : Boolean = false)(implicit ec: ExecutionContext) : Future[Store] = Future {
    if (!isDriverFor(uri))
      toss(s"Wrong URI type for FilesStorageDriver. Expected '$scheme' scheme but got: $uri")
    stores.get(uri) match {
      case Some(store) ⇒
        store
      case None ⇒
        if (!create)
          toss(s"Store at $uri does not exist and create was not requested")
        else {
          val result = FilesStore(uri)
          stores.put(uri, result)
          result
        }
    }
  }
}
