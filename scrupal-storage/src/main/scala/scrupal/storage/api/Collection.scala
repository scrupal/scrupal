/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                             *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                             *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                             *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                             *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.storage.api

import scala.concurrent.Future

trait Collection[T, S <: Storable[T, S]] extends AutoCloseable {
  def name : String
  def indices : Seq[Index[T, S]]
  def addIndex(field : String) : Future[WriteResult]
  def removeIndex(field : String) : Future[WriteResult]
  def fetch(id : ID) : Future[Option[S]]
  def find(query : Query) : Future[Seq[S]]
  def insert(obj : S) : Future[WriteResult]
  def update(obj : S, update : Modification) : Future[WriteResult]
  def update(id : ID, update : Modification) : Future[WriteResult]
  def updateWhere(query : Query, update : Modification) : Future[Seq[WriteResult]]
  def delete(obj : S) : Future[WriteResult]
  def delete(id : ID) : Future[WriteResult]
  def delete(ids : Seq[ID]) : Future[Seq[WriteResult]]
}

sealed trait WriteResult { def isSuccess : Boolean; def isFailure : Boolean = !isSuccess }
case class WriteSuccess() extends WriteResult { val isSuccess = true }
case class WriteFailure(failure : Throwable) extends WriteResult { val isSuccess = true }

object WriteResult {
  def failure(x : Throwable) : WriteResult = { WriteFailure(x) }
  def success() : WriteResult = WriteSuccess()
}

trait IndexKind
trait Modification
trait Query
