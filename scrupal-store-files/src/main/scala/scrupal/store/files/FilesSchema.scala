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

import scala.collection.mutable
import scala.util.matching.Regex

import scrupal.storage.api._

/** A Schema type for the Memory storage system */
case class FilesSchema private[files] (store : FilesStore, name : String, design : SchemaDesign) extends Schema {
  require(store.isInstanceOf[FilesStore])

  private val colls = new mutable.HashMap[String, FilesCollection[_]]


  /** Returns the set of collections that this Storage instance knows about */
  def collections : Map[String, Collection[_]] = colls.toMap

  /** Find and return a Collection of a specific name */
  def collectionFor[S <: Storable](name : String) : Option[Collection[S]] = {
    colls.get(name).asInstanceOf[Option[Collection[S]]]
  }

  /** Find collections matching a specific name pattern and return a Map of them */
  def collectionsFor(namePattern : Regex) : Map[String, Collection[_]] = {
    colls.filter {
      case (name : String, coll : Collection[_]) ⇒ namePattern.findFirstIn(name).isDefined
    }
  }.toMap

  override def addCollection[S <: Storable](name : String) : Collection[S] = {
    val coll = store.driver.makeCollection(this, name).asInstanceOf[FilesCollection[S]]
    colls.put(name, coll)
    coll.asInstanceOf[Collection[S]]
  }

  override def close() : Unit = {
    for ((name, coll) ← colls) { coll.close() }
  }
}
