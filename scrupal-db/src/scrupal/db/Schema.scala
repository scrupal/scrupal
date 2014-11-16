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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

/** Abstract Database Schema.
  * A Schema is a collection of related Entities. Each module should only define one Schema. The schema is primarily
  * responsible for making sure that the Mongo DB's collections that correspond to the entities are appropriately sized,
  * indexed, and etc. Each Schema should extend this Schema class to define the various DataAccessObjects used by
  * the Schema.
  */
abstract class Schema(val dbc: DBContext)
{

  def daos : Seq[DataAccessInterface[_,_]]

  def create(implicit context: DBContext) : Future[Seq[(String,Boolean)]] = {
    val futures = for (dao <- daos) yield {
      dao.collection.create(true) map { b => dao.collection.name -> true }
    }
    Future.sequence(futures)
  }


  def validateDao(dao: DataAccessInterface[_,_]) : Boolean

  def collectionNames: Seq[String] = daos map { dao => dao.collection.name }

  final def validateSchema(implicit ec: ExecutionContext) :  Future[Seq[String]] = {
    val futures = for (dao <- daos) yield { dao.validateSchema }
    Future sequence futures
  }

  // FIXME: protected def countCollection(name: String) = { dbc.withDatabase { db => db.command(new Count(name)) } }
}


