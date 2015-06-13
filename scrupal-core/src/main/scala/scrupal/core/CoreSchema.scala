/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.core

import scrupal.db.{DBContext, DataAccessInterface}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
 * The basic schema for Scrupal. This is composed by merging together the various Components.
 */
class CoreSchema(dbc: DBContext, dbName: String) extends scrupal.core.api.Schema(dbc, dbName) {

  // case class AliasDao(db: DB) extends JsonDao[String,BSONObjectID](db,"aliases") with DataAccessObject[String]
  // case class TokenDao(db: DB) extends JsonDao[String,BSONObjectID](db,"tokens") with DataAccessObject[String]
  // val aliases = dbc.withDatabase { db => new AliasDao(db) }
  // val tokens = dbc.withDatabase { db => new TokenDao(db) }


  override def daos : Seq[DataAccessInterface[_,_]] = super.daos

  override def validateDao(dao: DataAccessInterface[_,_]) : Boolean = { super.validateDao(dao) }


  override def create(implicit context: DBContext): Future[Seq[(String,Boolean)]] = {
    // FIXME: This needs to be written again.

    // First, call our super class to install our schema
    val futures = super.create

    // First, install the CoreModule itself
    // val f1 = modules.insert(CoreModule)

    // Now, install all the CoreModule's types and entities
    // val f2 = for (ty <- CoreModule.types) yield { types.insert( ty ) }
    /*val f3 = for (en <- CoreModule.entities) yield { entities.insert( en ).map { wr ⇒ en.label → !wr.hasErrors } }

    val combined = Future sequence (/*Seq(f1)  ++*/ f3)

    for (f <- futures; c <- combined) yield { f ++ c }
    */
    Future[Seq[(String,Boolean)]](Seq())

  }

}
