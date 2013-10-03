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

import org.joda.time.{Duration, DateTime}
import scrupal.utils.Icons
import scala.slick.lifted.DDL

/**
 * Information about a site hosted by Scrupal
 */
case class Site(
  override val id: Option[Long],
  override val created: DateTime,
  override val label: String,
  override val description: String
 ) extends Entity[Site] {
  def forId(id: Long) = Site(Some(id), created, label, description)
}

/**
 * The Host:Port portion of a URL to be associated with a Site that Scrupal will serve. Host can either be an IP address
 * or a domain name. A given site can be accessed through multiple HostPorts and behave differently depending which
 * way it was addrssed.
 * @param id Unique identifier of the HostPort
 * @param created Creation timestamp
 * @param host Domain name or IP address
 * @param port TCP Port number
 */
case class HostPort(
  override val id: Option[Long],
  override val created: DateTime,
  host : String,
  port: Int
) extends Identifiable[HostPort] {
  def forId(id: Long) = HostPort(Some(id), created, host, port)
}

case class Site_HostPort (
  site_id : Long,
  hostport_id : Long
)
;

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

trait CoreComponent extends Component {

  object Sites extends EntityTable[Site]("sites") {
    def * =  id.? ~ created ~ label ~ description <> (Site, Site.unapply _)
  }

  object HostPorts extends IdentifiableTable[HostPort]("hostports") {
    def host = column[String]("host")
    def port = column[Int]("port")
    def * = id.? ~ created ~ host ~ port <> (HostPort, HostPort.unapply _)
  }

  object Sites_HostPorts extends ManyToManyTable[Site,HostPort]("sites", "hostports", Sites, HostPorts)

  object Modules extends EntityTable[Module]("modules") {
    def enabled = column[Boolean]("enabled")
    def * =  id.? ~ created ~ label ~ description ~ enabled <> (Module, Module.unapply _)
  }

  def coreDDL : DDL = Sites.ddl ++ HostPorts.ddl ++ Sites_HostPorts.ddl ++ Modules.ddl
}
