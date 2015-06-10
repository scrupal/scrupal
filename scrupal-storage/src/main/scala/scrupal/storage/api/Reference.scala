/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                            *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                            *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                            *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                            *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.storage.api

import java.net.URI

import scala.concurrent.Future

/** A reference to a specific object
  *
  * Description of thing
  */
abstract class Reference[S <: Storable[S]](collection : Collection[S], id : ID) {
  def fetch : Future[Option[S]]
}

object Reference {
  def apply[T, S <: Storable[S]](storage_url : URI, collection_name : String, obj : S) : Reference[S] = {
    val driver = StorageDriver.driverForURI(storage_url)
    driver.open(storage_url) match {
      case Some(storage) ⇒
        storage.collectionFor[S](collection_name) match {
          case Some(coll) ⇒ driver.makeReference(coll, obj.primary_id)
          case None ⇒ throw new Exception(s"Collection named '$collection_name' not found in $storage_url")
        }
      case None ⇒ throw new Exception(s"No storage for $storage_url")
    }
  }
}
