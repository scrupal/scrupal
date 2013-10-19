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

package scrupal.api

import scala.slick.lifted.DDL

import scala.slick.driver._
import java.sql.{Clob, Timestamp}
import play.api.libs.json.{Json, JsValue}
import scala.Predef._
import scala.slick.session.Session
import play.api.Logger





/**
 * Abstract Database Schema.
 * A Schema is a collection of Components. Each module or the Scrupal Core should only define on Schema each.
 * Each Schema should extend this Schema class and then mixin all the components involved in the schema. This class
 * provides a little bit of magic. It extends Sketch, which endows it with the "profile" member which is an instance
 * of the Slick EmbeddedProfile class. This provides access to the kind of database chosen to represent the data.
 * However, we don't know which database that is at compile time so this class also takes a Sketch constructor
 * argument that it uses to populate its Sketch members such as profile, schema and driverClass. In this way we
 * can abstractly provide access to all the things needed to access the database, without actually locking in on
 * any particular database, schema name or DB driver.
 * @param sketch
 */
abstract class Schema(val sketch: Sketch )(override implicit val session: Session) extends  Component
{
  // The primary reason for this class to exist is to give concrete values to ScrupalSketch's members based on
  // an instance not inheritance. ScrupalSketch is abstract and has no concrete members. We want to have those members
  // be filled out from
  val profile: ExtendedProfile = sketch.profile
  val schema: Option[String] = sketch.schema
  val driverClass : String = sketch.driver

  // override implicit val session : Session = sketch.database.createSession()

  // This is where the magic happens :)
  import profile.simple._

  def toClob(js: JsValue) : Clob = {
    val clob = session.conn.createClob()
    clob.setString(1, Json.stringify(js));
    clob
  }

  def toJson(clob: Clob) : JsValue = Json.parse(clob.getSubString(1,Int.MaxValue))
  // FIXME: Can't we parse an InputStream?


  /**
   * Provides the set of DDL from all the tables, etc that this Schema offers. Subclasses must override this so this
   * class can provide functionality around the DDL.
   */
  val ddl : DDL

  def create(implicit session: Session): Unit = {
    if (schema.isDefined) {
      val update = sketch.makeSchema
      update.execute
    }
    Logger.debug("Creating Schema: \n" + (ddl.createStatements mkString("\n")))
    ddl.create
  }
}


