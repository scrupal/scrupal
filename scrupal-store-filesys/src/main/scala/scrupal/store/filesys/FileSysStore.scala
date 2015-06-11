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

package scrupal.storage.filesys

import java.net.URI

import scrupal.storage.api._
import scrupal.utils.TryWith

import scala.collection.mutable
import scala.util.{ Failure, Success }
import scala.util.matching.Regex

/** Title Of Thing.
  *
  * Description of thing
  */
case class FileSysStore private[filesys] (driver : StorageDriver, uri : URI) extends Store {
  require(driver == FileSysStorageDriver)

  private val _schemas = new mutable.HashMap[String, FileSysSchema]

  override def close : Unit = {
    for ((name, s) ← _schemas) { s.close() }
    _schemas.clear()
  }

  /** Returns the mapping of names to Schema instances for this kind of storage */
  def schemas : Map[String, Schema] = _schemas.toMap

  def addSchema(schema : Schema) : Schema = {
    _schemas.put(schema.name, schema.asInstanceOf[FileSysSchema])
    schema
  }

  def withSchema[T](schema : String)(f : Schema ⇒ T) : T = {
    _schemas.get(schema) match {
      case Some(s) ⇒ f(s.asInstanceOf[FileSysSchema])
      case None    ⇒ toss(s"Schema '$schema' not found in $uri ")
    }
  }

  def withCollection[T, S <: Storable[S]](schema : String, collection : String)(f : (Collection[S]) ⇒ T) : T = {
    _schemas.get(schema) match {
      case Some(s) ⇒ s.withCollection[T, S](collection)(f)
      case None    ⇒ toss(s"Schema '$schema' not found in $uri ")
    }
  }
}
