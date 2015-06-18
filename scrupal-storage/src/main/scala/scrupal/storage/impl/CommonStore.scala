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

import java.util.concurrent.{Semaphore, ConcurrentHashMap}
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.concurrent
import scala.collection.convert.decorateAsScala._

import scrupal.storage.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

trait CommonStore extends Store {

  protected val _schemas : concurrent.Map[String,Schema] = new ConcurrentHashMap[String,Schema]().asScala

  def size : Long = _schemas.size

  def drop(implicit ec: ExecutionContext) : Future[WriteResult] = {
    val result = WriteResult.coalesce {
      for ( (name, schema) <- _schemas) yield {
        schema.drop
      }
    }
    _schemas.clear()
    result
  }

  /** Returns the mapping of names to Schema instances for this kind of storage */
  def schemas : Map[String, Schema] = _schemas.toMap

  def hasSchema(name: String) : Boolean = _schemas.contains(name)

  protected def makeNewSchema(design: SchemaDesign) : Schema

  /** Create a new collection for storing objects */
  def addSchema(design: SchemaDesign)(implicit ec: ExecutionContext) : Future[Schema] = Future {
    _schemas.get(design.name) match {
      case Some(s) ⇒
        toss(s"Schema ${design.name} already exists.")
      case None ⇒
        val schema = makeNewSchema(design)
        _schemas.put(design.name, schema)
        design.construct(schema)
        schema
    }
  }

  def dropSchema(name : String)(implicit ec: ExecutionContext) : Future[WriteResult] = Future {
    _schemas.remove(name) match {
      case Some(schema) ⇒
        WriteResult.success()
      case None ⇒
        WriteResult.error(s"No schema named $name to remove")
    }
  }

  def withSchema[T](schema : String)(f : Schema ⇒ T) : T = {
    _schemas.get(schema) match {
      case Some(s) ⇒
        f(s)
      case None  ⇒
        toss(s"Schema '$schema' not found in $uri ")
    }
  }

  def withCollection[S <: Storable,T](schema : String, collection : String)(f : (Collection[S]) ⇒ T) : T = {
    _schemas.get(schema) match {
      case Some(s) ⇒ s.withCollection[S,T](collection)(f)
      case None    ⇒ toss(s"Schema '$schema' not found in $uri ")
    }
  }

  override def close() : Unit = {
    // TODO: Implement a semaphore or something to prevent closing this while there are active contexts
    for ((name, s) ← _schemas) {
      s.close()
    }
    _schemas.clear()
  }
}
