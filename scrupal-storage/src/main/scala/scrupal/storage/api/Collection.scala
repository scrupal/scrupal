/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                      *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                      *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                      *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                      *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.storage.api

import scala.concurrent.{ExecutionContext, Future}

trait Collection[S <: Storable] extends StorageLayer {
  type SF <: StorageFormat
  implicit val formatter : StorageFormatter[SF,S]
  def name : String
  def schema : Schema
  override def toString = { s"Collection $name in ${schema.name}" }
  def indexOf(field : Seq[Indexable]) : Option[Index]
  def addIndex(index : Index)(implicit ec: ExecutionContext) : Future[WriteResult]
  def removeIndex(index : Index)(implicit ec: ExecutionContext) : Future[WriteResult]
  def indices : Seq[Index]
  def fetch(id : ID)(implicit ec: ExecutionContext) : Future[Option[S]]
  def fetchAll()(implicit ec: ExecutionContext) : Future[Iterable[S]]
  def find(query : Query[S])(implicit ec: ExecutionContext) : Future[Seq[S]]
  def findOne(query : Query[S])(implicit ec: ExecutionContext) : Future[Option[S]] = find(query).map { seq ⇒ seq.headOption }
  def insert(obj : S, update : Boolean = false)(implicit ec: ExecutionContext) : Future[WriteResult]
  def queriesFor[T <: Queries[S]] : T
  def update(obj : S)(implicit ec: ExecutionContext) : Future[WriteResult] = update(obj, IdentityModification(obj))
  def update(obj : S, update : Modification[S])(implicit ec: ExecutionContext) : Future[WriteResult]
  def update(id : ID, update : Modification[S])(implicit ec: ExecutionContext) : Future[WriteResult]
  def updateWhere(query : Query[S], update : Modification[S])(implicit ec: ExecutionContext) : Future[Seq[WriteResult]]
  def delete(obj : S)(implicit ec: ExecutionContext) : Future[WriteResult]
  def delete(id : ID)(implicit ec: ExecutionContext) : Future[WriteResult]
  def delete(ids : Seq[ID])(implicit ec: ExecutionContext) : Future[WriteResult]
}

trait IndexKind
trait Modification[S <: Storable] { def apply(s : S) : S = s }
case class IdentityModification[S <: Storable](s : S) extends Modification[S]
