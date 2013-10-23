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

import play.api.Logger

object SupportedDatabases extends Enumeration {
  type Kind = Value
  val H2 = Value
  val MySQL = Value
  val SQLite = Value
  val Postgres = Value

  def forJDBCUrl(url: String) : Option[Kind] = {
    url match {
      case s if s.startsWith("jdbc:h2") => Some(H2)
      case s if s.startsWith("jdbc:mysql") => Some(MySQL)
      case s if s.startsWith("jdbc:sqllite:") => Some(SQLite)
      case s if s.startsWith("jdbc:postgresql:") => Some(Postgres)
      case _ => None
    }
  }

  def defaultDriverFor(kind: Kind) : String = {
    kind match {
      case H2 =>  "org.h2.Driver"
      case MySQL => "com.mysql.jdbc.Driver"
      case SQLite =>  "org.sqlite.JDBC"
      case Postgres => "org.postgresql.Driver"
      case _ => Logger.warn("Unrecognized SupportedDatabase.Kind !"); "not.a.db.driver"
    }
  }

  def defaultDriverFor(kind: Option[Kind]) : String = {
    kind match {
      case Some(x) => defaultDriverFor(x)
      case None => "org.h2.Driver" // Just because that's the default one Play uses
    }
  }
}
