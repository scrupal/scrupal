/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                    *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                    *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                    *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                    *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.storage.api

import java.net.URI

import scrupal.utils.ScrupalComponent

trait Store extends AutoCloseable with ScrupalComponent {
  def driver : StorageDriver
  def uri : URI

  /** Returns the mapping of names to Schema instances for this kind of storage */
  def schemas : Map[String, Schema]

  def hasSchema(name: String) : Boolean

  /** Create a new collection for storing objects */
  def addSchema(design : SchemaDesign) : Schema

  def withSchema[T](schema : String)(f : (Schema) ⇒ T) : T

  def withCollection[T, S <: Storable](schema : String, collection : String)(f : (Collection[S]) ⇒ T) : T
}

object Store {

  def open(uri : URI) : Option[Store] = {
    StorageDriver.apply(uri).open(uri)
  }
}

