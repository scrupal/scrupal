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

import org.joda.time.DateTime
import scala.slick.lifted.DDL
import scrupal.api._
import scala.Some


/**
 * Information about a site hosted by Scrupal
 */
case class Site(
  override val name: Symbol,
  override val description: String,
  override val modified: Option[DateTime] = Some(DateTime.now()),
  override val created: Option[DateTime] = Some(DateTime.now()),
  override val id: Option[Long] = None
 ) extends Thing[Site](name, description, modified, created, id) {
  def forId(id: Long) = Site(name, description, modified, created, Some(id))
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
case class HostPort (
  override val id: Option[Long],
  override val created: Option[DateTime],
  host : String,
  port: Int
) extends Creatable[HostPort] {
  def forId(id: Long) = HostPort(Some(id), created, host, port)
}

/**
 * Representation of a module in the database. Modules are plug-in extensions to Scrupal.
 * @param name
 * @param description A brief description of the module
 * @param modified
 * @param created
 * @param id
 */
case class Module (
  override val name: Symbol,
  override val description: String,
  override val modified: Option[DateTime] = Some(DateTime.now()),
  override val created: Option[DateTime] = Some(DateTime.now()),
  override val id: Option[Long] = None
) extends Thing[Module](name, description, modified, created, id) {
  def forId(id: Long) = Module(name, description,  modified, created, Some(id))
}

case class Setting (
  override val name : Symbol,
  kind : Long,
  override val created: Option[DateTime] = Some(DateTime.now()),
  override val id: Option[Long] = None
) extends ImmutableThing[Setting](name, created, id) {
  def forId(id: Long) = Setting(name, kind, created, Some(id))
}

trait CoreComponent extends Component {

  // Get the TypeMapper for DateTime
  import CommonTypeMappers._

  object Sites extends ThingTable[Site]("sites") {
    def * = name ~ description ~ modified.? ~ created.? ~ id.? <> (Site.tupled, Site.unapply _)
  }

  object HostPorts extends CreatableTable[HostPort]("hostports") {
    def host = column[String]("host")
    def port = column[Int]("port")
    def * = id.? ~ created.? ~ host ~ port <> (HostPort.tupled, HostPort.unapply _)
  }

  object Sites_HostPorts extends ManyToManyTable[Site,HostPort]("HostPortsOfSites", "sites", "hostports", Sites, HostPorts)

  object Modules extends ThingTable[Module]("modules") {
    def enabled = column[Boolean]("enabled")
    def * =  name ~ description ~ modified.? ~ created.? ~ id.?  <> (Module.tupled, Module.unapply _)
  }

  object Settings extends CreatableTable[Setting]("settings") {
    def name = column[Symbol](tableName + "_name")
    def name_index = index(tableName + "_name_index", name, unique=true)
    def kind = column[Long](tableName + "_kind")
    def * = name ~ kind ~ created.? ~ id.? <> (Setting.tupled, Setting.unapply _ )
  }

  def coreDDL : DDL = Sites.ddl ++ HostPorts.ddl ++ Sites_HostPorts.ddl ++ Modules.ddl
}
