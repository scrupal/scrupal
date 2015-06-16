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

import scrupal.storage.api._

import scala.collection.mutable

abstract class CommonStorageDriver extends StorageDriver {

  protected val stores : mutable.HashMap[URI, Store] = new mutable.HashMap[URI, Store]

  protected def storeExists(uri: URI) : Boolean = {
    stores.get(uri) match {
      case Some(store) ⇒
        store.exists
      case None ⇒
        makeStore(uri).exists
    }
  }

  override def canOpen(uri : URI) : Boolean = {
    super.canOpen(uri) && storeExists(uri)
  }

  def open(uri : URI, create : Boolean = false) : Option[Store] = {
    if (!isDriverFor(uri))
      None
    if (!storeExists(uri) && !create)
      None

    stores.get(uri) orElse {
      if (create) {
        val result = makeStore(uri)
        stores.put(uri, result)
        Some(result)
      } else {
        None
      }
    }
  }

  override def close() : Unit = {
    for ((name, s) ← stores)
      s.close()
    stores.clear()
  }

  def withStore[T](uri : URI, create : Boolean = false)(f : Store ⇒ T) : T = {
    stores.get(uri) match {
      case Some(store) ⇒
        f(store)
      case None ⇒
        open(uri, create) match {
          case Some(store) ⇒
            f(store)
          case None ⇒
            toss(s"No store found for $uri")
        }
    }
  }
}


