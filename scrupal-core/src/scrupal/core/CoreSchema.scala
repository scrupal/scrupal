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

package scrupal.core

import scrupal.db.{DataAccessObject, DBContext, Schema}
import scrupal.core.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
 * The basic schema for Scrupal. This is composed by merging together the various Components.
 */
class CoreSchema(dbc: DBContext) extends Schema(dbc) {

  val (features,sites,entities,instances,alerts) = dbc.withDatabase { db ⇒
    (
      Feature.FeatureDao(db),
      Site.SiteDao(db),
      Entity.EntityDao(db),
      Instance.InstanceDao(db),
      Alert.AlertDao(db)
    )
  }

  // case class AliasDao(db: DB) extends JsonDao[String,BSONObjectID](db,"aliases") with DataAccessObject[String]
  // case class TokenDao(db: DB) extends JsonDao[String,BSONObjectID](db,"tokens") with DataAccessObject[String]
  // val aliases = dbc.withDatabase { db => new AliasDao(db) }
  // val tokens = dbc.withDatabase { db => new TokenDao(db) }


  def daos : Seq[DataAccessObject[_,_]] = {
    Seq( features, sites, entities, instances, /* aliases, tokens, */ alerts )
  }

  def validateDao(dao: DataAccessObject[_,_]) : Boolean = {
    // FIXME: this needs to be written properly
    dao.collection.name match {
      case "modules" => true
      case "sites" => true
      case "entities" => true
      case "instances" => true
      case "principals" => true
      case "aliases" => true
      case "tokens" => true
      case "alerts" => true
      case _ => false
    }
  }

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
