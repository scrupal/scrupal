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

import java.util.concurrent.ConcurrentHashMap

import scrupal.storage.api._
import scrupal.utils.Validation.Results

import scala.collection.concurrent
import scala.collection.convert.decorateAsScala._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

trait CommonSchema extends Schema {

  protected val colls : concurrent.Map[String,Collection[_]] = new ConcurrentHashMap[String,Collection[_]]().asScala

  override def toString = { s"Schema '$name' in ${store.uri}" }

  def drop(implicit ec: ExecutionContext): Future[WriteResult] = {
    val result = WriteResult.coalesce {
      for ( (name, coll) <- colls) yield {
        coll.drop
      }
    }
    colls.clear()
    result
  }

  def size: ID = colls.size

  def validate : Results[Schema] = { design.validate(this) }

  /** Returns the set of collections that this Storage instance knows about */
  def collections : Map[String, Collection[_]] = colls.toMap

  /** Find and return a Collection of a specific name */
  def collectionFor[S <: Storable](name : String) : Option[Collection[S]] = {
    colls.get(name).asInstanceOf[Option[Collection[S]]]
  }

  /** Get the set of collection names */
  def collectionNames : Iterable[String] = { colls.keys }

  /** Find collections matching a specific name pattern and return a Map of them */
  def collectionsFor(namePattern : Regex) : Map[String, Collection[_]] = {
    colls.filter {
      case (name : String, coll : Collection[_]) ⇒ namePattern.findFirstIn(name).isDefined
    }
  }.toMap

  def withCollection[S <: Storable,T](name : String)(f : Collection[S] ⇒ T) : T = {
    colls.get(name) match {
      case Some(coll) ⇒ f(coll.asInstanceOf[Collection[S]])
      case None ⇒ toss(s"Collection '$name' in schema '${this.name} does not exist")
    }
  }

  override def close() : Unit = {
    for ((name, coll) ← colls) {
      coll.close()
    }
  }
}
