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

import scala.slick.lifted.MappedTypeMapper
import org.joda.time.DateTime
import java.sql.Timestamp
import org.joda.time.DateTimeZone._

/**
 * This object just collects together a variety of Slick TypeMappers that are used to convert between database
 * types and Scala types. All TypeMappers should be delcared implicit lazy vals so they only get instantiated
 * when they are used. To use them just "import CommonTypeMappers._"
 */
object CommonTypeMappers {


  implicit lazy val dateTimeMapper = MappedTypeMapper.base[DateTime,Timestamp](
    { d => new Timestamp( d getMillis ) },
    { t => new DateTime (t getTime, UTC)  }
  )

}
