/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2014, Reid Spencer and viritude llc. All Rights Reserved.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.db

import play.api.libs.json.{JsString, Json}
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.commands.LastError
import reactivemongo.extensions.json.dao.JsonDao
import scrupal.api.Storable

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait DataAccessObject[T <: Storable] extends JsonDao[T, BSONObjectID] {

  def fetch(name: Symbol) : Future[Option[T]] = findOne(Json.obj("name" -> JsString(name.name)))
  def fetchSync(name: Symbol) : Option[T] = Await.result(fetch(name), Duration.Inf)

  def fetchAll : Future[List[T]] = { super.findAll(Json.obj()) }
  def fetchAllSync : List[T] = { Await.result(fetchAll, Duration.Inf) }

  def count : Future[Int] = { count(Json.obj()) }
  def countSync : Int = { Await.result(count, Duration.Inf) }

  def insertSync(model: T) : LastError = { Await.result(super.insert(model), Duration.Inf) }

  def upsert(model: T) : Future[LastError] = { super.save(model) }
  def upsertSync(model: T) : LastError = { Await.result(upsert(model), Duration.Inf) }

}
