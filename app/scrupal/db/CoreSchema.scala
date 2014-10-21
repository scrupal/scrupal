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

import reactivemongo.api.DB
import reactivemongo.core.commands.LastError
import scrupal.models.CoreModule

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import scrupal.api._
import reactivemongo.extensions.json.dao.JsonDao


/**
 * The basic schema for Scrupal. This is composed by merging together the various Components.
 */
class CoreSchema(dbc: DBContext) extends Schema(dbc) {

  case class ModuleDao(db: DB) extends JsonDao[Module,Identifier](db, "modules") with DataAccessObject[Module,Identifier]
  case class SiteDao(db: DB) extends JsonDao[SiteData,Identifier](db,"sites") with DataAccessObject[SiteData,Identifier]
  case class EntityDao(db: DB) extends JsonDao[Entity,Identifier](db, "entities") with DataAccessObject[Entity,Identifier]
  case class InstanceDao(db: DB) extends JsonDao[Instance,Identifier](db, "instances") with DataAccessObject[Instance,Identifier]
  // case class AliasDao(db: DB) extends JsonDao[String,BSONObjectID](db,"aliases") with DataAccessObject[String]
  // case class TokenDao(db: DB) extends JsonDao[String,BSONObjectID](db,"tokens") with DataAccessObject[String]
  case class AlertDao(db: DB) extends JsonDao[Alert,Identifier](db, "alerts") with DataAccessObject[Alert,Identifier]

  val modules : DataAccessObject[Module,Identifier] = dbc.withDatabase { db => new ModuleDao(db) }
  val sites : DataAccessObject[SiteData,Identifier] = dbc.withDatabase { db => new SiteDao(db) }
  val entities : DataAccessObject[Entity,Identifier] = dbc.withDatabase { db => new EntityDao(db) }
  val instances = dbc.withDatabase { db => new InstanceDao(db) }
  // val aliases = dbc.withDatabase { db => new AliasDao(db) }
  // val tokens = dbc.withDatabase { db => new TokenDao(db) }
  val alerts = dbc.withDatabase { db => new AlertDao(db) }


  def daos : Seq[DataAccessObject[_,_]] = {
    Seq( modules, sites, entities, instances, /* aliases, tokens, */ alerts )
  }

  def validateDao(dao: DataAccessObject[_,_]) : Boolean = {
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

  override def create(implicit context: DBContext): Future[Seq[LastError]] = {
    // First, call our super class to install our schema
    val futures = super.create

    // First, install the CoreModule itself
    val f1 = modules.insert(CoreModule)

    // Now, install all the CoreModule's types and entities
    // val f2 = for (ty <- CoreModule.types) yield { types.insert( ty ) }
    val f3 = for (en <- CoreModule.entities) yield { entities.insert( en ) }

    val combined = Future sequence (Seq(f1)  ++ f3)

    for (f <- futures; c <- combined) yield { f ++ c }
  }

}
