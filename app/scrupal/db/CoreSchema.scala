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

import scala.slick.lifted.DDL
import scala.slick.session.Session
import scala.slick.jdbc.meta.MTable

import scrupal.models.CoreModule
import scrupal.api.{Entity, Type}

/**
 * The basic schema for Scrupal. This is composed by merging together the various Components.
 */
class CoreSchema(sketch: Sketch)(implicit session: Session) extends Schema (sketch)
  with ITEMSComponent with AAAComponent  with NotificationComponent
{

  // This is where the magic happens :)
  import profile.simple._

  // Super class Schema requires us to provide the DDL from our tables
  override val ddl : DDL = {
    coreDDL ++ aaaDDL ++ notificationDDL
  }

  // Tables In This Schema
  val CoreTables = List(Types, Modules, Sites, Entities, Instances)
  val UserTables = List(Principals, Handles, Tokens )
  val NotificationTables = List( Alerts )

  val tableNames : Seq[String] =
    (for (t <- CoreTables) yield t.tableName) :::
    (for (t <- UserTables) yield t.tableName ) :::
    (for (t <- NotificationTables) yield t.tableName)

  override def validateTables( tables: Map[String,MTable] )(implicit session: Session) : Boolean = {
    // Simply validating that each of the table names we expect exists is sufficient for now
    val meta_tables_scan_results =
      for (tname: String <- tableNames )
      yield tables.contains(tname)
    val num_in_meta_tables = meta_tables_scan_results.count { b: Boolean => b }
    num_in_meta_tables == tableNames.size
  }

  def createCoreTables(implicit session: Session) : Unit = {
    val ddl: DDL = Instances.ddl ++ Types.ddl ++ Entities.ddl ++ Modules.ddl ++ Sites.ddl
    ddl.create
  }

  override def create(implicit session: Session): Unit = {
    // First, call our super class to install our schema
    super.create

    // First, install the CoreModule itself
    Modules.insert(CoreModule)

    // Now, install all the CoreModule's types and entities
    CoreModule.types foreach { ty : Type => Types.insert( ty ) }
    CoreModule.entities foreach { en : Entity => Entities.insert( en ) }

    // That's it!
  }

}
