/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                          *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                          *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                          *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                          *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.storage.api

import java.net.URI

import scala.util.matching.Regex

trait Storage extends AutoCloseable {
  def driver : StorageDriver
  def url : URI
  /** Returns the set of collections that this Storage instance knows about */
  def collections : Map[String, Collection[_]]

  /** Find and return a Collection of a specific name */
  def collectionFor[S <: Storable[S]](name : String) : Option[Collection[S]]

  /** Find collections matching a specific name pattern and return a Map of them */
  def collectionsFor(namePattern : Regex) : Map[String, Collection[_]]

  /** Create a new collection for storing objects */
  def makeCollection[S <: Storable[S]](name : String) : Collection[S]
}

object Storage {

  def open(url : URI) : Option[Storage] = {
    StorageDriver.driverForURI(url).open(url)
  }
}

