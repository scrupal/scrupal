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

package scrupal.models.db

import scala.slick.driver._
import scala.slick.jdbc.{StaticQuery0, StaticQuery}

/**
 * A Sketch is a simple trait that sketches out some basic things we need to know about a particular database
 * implementation. This is similar to Slick's Profiles, but we call them Sketches here because they're a related but
 * different concept. The subclasses of this trait are concrete, one for each kind of database. Every Sketch
 * has a member that is the Slick ExtendedProfile to be used, specifies  the schema name to be used
 * for the scrupal tables, knows the driver class name, can instantiate that driver, and knows how to create the
 * schema in which the Scrupal tables live. This allows administrators to keep scrupal's tables separate from other
 * modules added to scrupal, while keeping all the data in the same database
 */
trait Sketch {
  val profile : ExtendedProfile
  val driverClass : String
  val schema : Option[String] = None
  def driver = Class.forName(driverClass).newInstance()
  def makeSchema : StaticQuery0[Int] = throw new NotImplementedError("Making DB Schema for " + driverClass )
  override def toString = { "{" + driverClass + ", " + schema + "," + profile }
}

/**
 * The Sketch for H2 Database
 * @param schema - optional schema name for the profile
 */
case class H2Profile(override val schema: Option[String] = None ) extends Sketch
{
  override val profile: ExtendedProfile = H2Driver
  override val driverClass : String = "org.h2.Driver"
  override def makeSchema : StaticQuery0[Int] = StaticQuery.u + "SET TRACE_LEVEL_FILE 4; CREATE SCHEMA IF NOT EXISTS " + schema.get
}

/**
 * The Sketch for H2 Database
 * @param schema - optional schema name for the profile
 */
case class MySQLProfile (override val schema: Option[String] = None )  extends Sketch {
  override val profile: ExtendedProfile = MySQLDriver
  override val driverClass : String = "com.mysql.jdbc.Driver"
}

/**
 * The Sketch for SQLite Database
 * @param schema - optional schema name for the profile
 */
class SQLiteProfile (override val schema: Option[String] = None ) extends Sketch {
  override val profile: ExtendedProfile = SQLiteDriver
  override val driverClass : String = "org.sqlite.JDBC"

}

/**
 * The Sketch for Postgres Database
 * @param schema - optional schema name for the profile
 */
class PostgresProfile (override val schema: Option[String] = None ) extends Sketch {
  override val profile: ExtendedProfile = PostgresDriver
  override val driverClass : String = "org.postgresql.Driver"

}

