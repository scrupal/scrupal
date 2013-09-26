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

import scrupal.models.ScrupalSchema

/**
 * A collection of utilities for dealing with the Scrupal database. This is where we find the initialization code
 * and various things to make using the database even easier.
 */
object DB {

  /**
   * Convert a URL into a Database Sketch by examining its prefix
   * @param url - The JDBC Connection URL for the database we should connect to
   * @return A Sketch for the corresponding database type
   */
  def sketch4URL(url: String, schema: Option[String] = None ) : Sketch = {
    url match {
      case s if s.startsWith("jdbc:h2:") => return new H2Profile(schema)
      case s if s.startsWith("jdbc:mysql:") => return new MySQLProfile(schema)
      case s if s.startsWith("jdbc:sqllite:") => return new SQLiteProfile(schema)
      case s if s.startsWith("jdbc:postgresql:") => return new PostgresProfile(schema)
      case _ => throw new UnsupportedOperationException("JDBC Url (" + url + ") is not for a supported database.")
    }
  }

  val scrupal_schema : Option[ScrupalSchema] = None

}
