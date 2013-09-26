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

package scrupal.models

import scrupal.models.db._
import org.joda.time.{Duration, DateTime}
import scrupal.utils.Icons
import scala.slick.direct.AnnotationMapper.column

/**
 * Representation of a module in the database. Modules are plug-in extensions to Scrupal.
 * @param id
 * @param created
 * @param label
 * @param description A brief description of the module
 */
case class Module(
  override val id: Option[Long],
  override val created: DateTime,
  override val label: String,
  override val description: String,
  enabled : Boolean
) extends Entity[Module] {
  def forId(id: Long) = Module(Some(id), created, label, description, enabled)
}

trait CoreComponent extends Component { self : Component =>

  import profile.simple._

  // Get the TypeMapper for DateTime
  import CommonTypeMappers._

  // This allows you to use AlertKind.Value as a type argument to the column method in a table definition
  implicit val iconTM = MappedTypeMapper.base[Icons.Value,Int]( { icon => icon.id }, { id => Icons(id)})

  // This allows you to use AlertKind.Value as a type argument to the column method in a table definition
  implicit val alertTM = MappedTypeMapper.base[AlertKind.Value,Int]( { alert => alert.id }, { id => AlertKind(id) } )

  object Modules extends EntityTable[Module]("modules") {
    def enabled = column[Boolean]("enabled")
    def * =  id.? ~ created ~ label ~ description ~ enabled <> (Module, Module.unapply _)
  }

  object Alerts extends EntityTable[Alert]("alerts") {
    def message =     column[String]("message")
    def alertKind =   column[AlertKind.Value]("alertKind")
    def iconKind =    column[Icons.Value]("iconKind")
    def prefix =      column[String]("prefix")
    def cssClass =    column[String]("css")
    def expires =     column[DateTime]("expires")
    def expires_index = index("expires_index", expires, unique=false)
    def * = id.? ~ created ~ label ~ description  ~ message ~ alertKind  ~ iconKind ~ prefix ~ cssClass ~ expires <>
       (Alert, Alert.unapply _ )

    lazy val unexpiredQuery = for { expires <- Parameters[DateTime] ; alrt <- this if alrt.expires > expires  } yield alrt

    def findUnexpired(implicit session: Session) : List[Alert] =  {  unexpiredQuery(DateTime.now()).list }

    def renew(theID: Long, howLong: Duration)(implicit session: Session) = {
      val query = for {  alrt <- this if alrt.id === theID } yield alrt.expires
      query.update(DateTime.now().plus(howLong))
    }
  }
}
