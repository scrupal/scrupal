/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

import reactivemongo.core.commands.{LastError, Count}

import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

/** Abstract Database Schema.
  * A Schema is a collection of related Entities. Each module should only define one Schema each. The schema is primarily
  * responsible for making sure that the Mongo DB's collections that correspond to the entities are appropriately sized,
  * indexed, and etc.  Each Schema should extend this Schema class to handle the various
  */
abstract class Schema(val dbc: DBContext)
{

  def daos : Seq[DataAccessObject[_,_]]

  def create(implicit context: DBContext) : Future[Seq[LastError]] = {
    val futures = for (dao <- daos) yield {
      dao.collection.create(true) map { b =>
        if (b)
          LastError(true, None, None, None, None, 1, false)
        else
          LastError(false, Some("Collection '" + dao.collection.name + "' already exists."), None, None, None, 0, false)
      }
    }
    Future.sequence(futures)
  }


  def validateDao(dao: DataAccessObject[_,_]) : Boolean

  def collectionNames: Seq[String] = daos map { dao => dao.collection.name }

  final def validate: Try[Boolean] = Try {
    val results : Seq[Boolean] = for (dao <- daos) yield {
      validateDao(dao)
    }
    results.foldLeft(true) { (result,each) => result & each }
  }

  protected def countCollection(name: String) = { dbc.withDatabase { db => db.command(new Count(name)) } }
}


