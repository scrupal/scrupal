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
import org.joda.time.DateTime
import scrupal.api.{Thing,  Component}
import java.sql.Clob
import scala.slick.direct.AnnotationMapper.column

case class Entity (
  override val name : Symbol,
  override val description : String,
  typ : Symbol,
  override val modified : Option[DateTime] = Some(DateTime.now()),
  override val created : Option[DateTime] = Some(DateTime.now()),
  override val id : Option[Long] = None
) extends Thing[Entity](name, description, modified, created, id) {
  def forId(id: Long) = Entity(name, description, typ, modified, created, Some(id))
}

/**
 * One line sentence description here.
 * Further description here.
 */
trait EntityComponent extends Component {

  import profile.simple._

  object Entities extends ThingTable[Entity]("entities") {
    def typ = column[Symbol](tableName + "typ")
    def * = name ~ description ~ typ ~ modified.? ~ created.? ~ id.? <> (Entity, Entity.unapply _)
  }

  object Traits extends Table[(Long,Long,Symbol,Clob)]("traits") {
    def tid = column[Long](tableName + "_tid", O.PrimaryKey)
    def eid = column[Long](tableName + "_eid")
    def eid_fkey = foreignKey(tableName + "_eid_fkey", eid, Entities)(_.id)
    def typ = column[Symbol](tableName + "_typ")
    def payload = column[Clob](tableName + "_payload")
    def * = tid ~ eid ~ typ ~ payload
  }

  def entityDDL : DDL = Entities.ddl ++ Traits.ddl
}
