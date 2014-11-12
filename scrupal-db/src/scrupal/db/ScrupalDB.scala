/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
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

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{FailoverStrategy, MongoConnection, DefaultDB}

import scala.concurrent.{ExecutionContext, Future}

/** Scrupal Database Abstraction
  *
  * This just extends ReactiveMongo's DefaultDB with a few functions suited for Scrupal
  * Created by reid on 11/9/14.
  */
class ScrupalDB(name: String, connection: MongoConnection,
  failoverStrategy : FailoverStrategy = DefaultFailoverStrategy) extends DefaultDB(name, connection, failoverStrategy) {

  def emptyDatabase(implicit ec: ExecutionContext) : Future[List[(String, Boolean)]] = {
    collectionNames flatMap { names =>
      val futures = for (cName <- names if !cName.startsWith("system.")) yield {
        val coll = collection[BSONCollection](cName)
        coll.drop() map { b => cName -> true }
      }
      Future sequence futures
    }
  }

  def dropCollection(collName: String)(implicit ec: ExecutionContext) : Future[Boolean] = {
    collection[BSONCollection](collName).drop map { foo => true } // FIXME: Deal with result code better here
  }

  def hasCollection(collName: String)(implicit ec: ExecutionContext) : Future[Boolean] = {
    collectionNames.map { list ⇒ list.contains(collName) }
  }

  def isEmpty(implicit ec: ExecutionContext) : Future[Boolean] = {
    collectionNames.map { list =>
      val names = list.filterNot { name ⇒ name.startsWith("system.") }
      names.isEmpty
    }
  }
}
