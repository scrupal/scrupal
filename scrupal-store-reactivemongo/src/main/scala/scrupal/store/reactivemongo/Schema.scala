/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
 *                                                                                                                    *
 * Copyright © 2015 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
 * except in compliance with the License. You may obtain a copy of the License at                                     *
 *                                                                                                                    *
 *        http://www.apache.org/licenses/LICENSE-2.0                                                                  *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the                          *
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
 * either express or implied. See the License for the specific language governing permissions                         *
 * and limitations under the License.                                                                                 *
 **********************************************************************************************************************/

package scrupal.store.reactivemongo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

/** Abstract Database Schema.
  * A Schema is a collection of related Entities. Each module should only define one Schema. The schema is primarily
  * responsible for making sure that the Mongo DB's collections that correspond to the entities are appropriately sized,
  * indexed, and etc. Each Schema should extend this Schema class to define the various DataAccessObjects used by
  * the Schema.
  */
abstract class Schema(val dbc: DBContext, val dbName: String) {

  def withDB[T](f: ScrupalDB ⇒ T ) : T = {
    dbc.withDatabase[T](dbName) { db: ScrupalDB ⇒ f(db) }
  }

  def daos : Seq[DataAccessInterface[_,_]]

  def create(implicit context: DBContext) : Future[Seq[(String,Boolean)]] = {
    val futures = for (dao <- daos) yield {
      dao.collection.create(true) map { b ⇒ dao.collection.name -> true }
    }
    Future.sequence(futures)
  }


  def validateDao(dao: DataAccessInterface[_,_]) : Boolean

  def collectionNames: Seq[String] = { daos.map { dao ⇒ dao.collection.name } }

  final def validateSchema(implicit ec: ExecutionContext) : Future[Seq[String]] = {
    val futures = for (dao <- daos) yield { dao.validateSchema }
    Future sequence futures
  }

  // FIXME: protected def countCollection(name: String) = { dbc.withDatabase { db ⇒ db.command(new Count(name)) } }
}


