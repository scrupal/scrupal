/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                        *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                        *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                        *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                        *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.storage.api

import java.net.URI

import scrupal.utils.ScrupalComponent

import scala.concurrent.Future

/** A reference to a specific object */
abstract class Reference[S <: Storable](collection : Collection[S], id : ID) extends AutoCloseable {
  def fetch : Future[Option[S]]
}

object Reference extends ScrupalComponent {
  def apply[T, S <: Storable](schema : String, collection : String, obj : S)(implicit sc: StorageContext) : Reference[S] = {
    sc.withCollection(schema, collection) { coll : Collection[_] ⇒
      sc.driver.makeReference[S](coll.asInstanceOf[Collection[S]], obj.primaryId)
    }
  }
}
