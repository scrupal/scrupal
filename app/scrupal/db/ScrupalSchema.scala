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

import scala.slick.lifted.DDL
import scrupal.api.{Sketch, Schema}
import scala.slick.session.Session

/**
 * The basic schema for Scrupal. This is composed by merging together the various Components.
 */
class ScrupalSchema(sketch: Sketch)(implicit session: Session) extends Schema (sketch)
  with CoreComponent with UserComponent  with NotificationComponent
{

  // Super class Schema requires us to provide the DDL from our tables
  override val ddl : DDL = {
    coreDDL ++ userDDL ++ notificationDDL
  }

}
