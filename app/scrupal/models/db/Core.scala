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
import scrupal.api.Component
import java.sql.Clob
import scala.slick.direct.AnnotationMapper.column

/** Information about one site that Scrupal is serving.
  * Sites are associated with a port number that Play! is configured to listen on. We configure play's ports by
  * scanning this table and collecting all the port numbers that are configured for active sites.
  * @param name The name of the `Thing`
  * @param description A brief description of the `Thing`
  * @param listenPort The port number that Play! should listen on for this site
  * @param urlDomain The domain name to use in generated urls
  * @param urlPort The port number to use in generated urls
  * @param urlHttps The HTTP method to use in generated urls (e.g. https or http)
  * @param enabled Whether the site is enabled for serving or not
  * @param modified The time of last modification of the `Thing`
  * @param created The time at which the `Thing` was created
  * @param id The unique identifier of the `Thing` within its storage realm.
  */
case class Site (
  override val name: Symbol,
  override val description: String,
  listenPort: Short,
  urlDomain: String,
  urlPort: Short,
  urlHttps: Boolean = false,
  override val enabled: Boolean = false,
  override val modified: Option[DateTime] = Some(DateTime.now()),
  override val created: DateTime = DateTime.now(),
  override val id: Option[Long] = None
) extends EnabledThing[Site](name, description, enabled, modified, created, id) {
  def forId(id: Long) = Site(name, description, listenPort, urlDomain, urlPort, urlHttps,
                               enabled, modified, created, Some(id))
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
  majVer: Int,
  minVer: Int,
  enabled: Boolean = false,
  override val modified: Option[DateTime] = Some(DateTime.now()),
  override val created: DateTime = DateTime.now(),
  override val id: Option[Long] = None
) extends Thing[Module](name, description, modified, created, id) {
  def forId(id: Long) = Module(name, description, majVer, minVer, enabled,  modified, created, Some(id))
}

case class Type(
  override val name: Symbol,
  override val description: String,
  moduleId: Long,
  override val created: DateTime = DateTime.now(),
  override val id: Option[Long] = None
) extends DescribedThing[Type](name, description, created, id) {
  def forId(id: Long) = Type(name, description, moduleId, created, Some(id))
}

case class Bundle (
  override val name: Symbol,
  override val description: String,
  typeId: Long,
  payload: Clob,
  override val modified: Option[DateTime] = Some(DateTime.now()),
  override val created: DateTime = DateTime.now(),
  override val id: Option[Long] = None
) extends Thing[Bundle](name, description, modified, created, id) {
  def forId(id: Long) = Bundle(name, description, typeId, payload, modified, created, Some(id))
}

trait CoreComponent extends Component { self: Sketch =>

  import profile.simple._

  // Get the TypeMapper for DateTime
  import CommonTypeMappers._

  object Sites extends EnabledThingTable[Site]("sites") {
    def listenPort = column[Short](tableName + "_listenPort")
    def listenPort_index = index(tableName + "_listenPort_index", listenPort, unique=true)
    def urlDomain = column[String](tableName + "_urlDomain")
    def urlPort = column[Short](tableName + "_urlPort")
    def urlHttps = column[Boolean](tableName + "_urlHttps")
    def * = name ~ description ~ listenPort ~ urlDomain ~ urlPort ~ urlHttps ~ enabled ~ modified.? ~ created ~ id.? <>
      (Site.tupled, Site.unapply _)
  }

  object Modules extends ThingTable[Module]("modules") {
    def majVer = column[Int](tableName + "_majVer")
    def minVer = column[Int](tableName + "_minVer")
    def enabled = column[Boolean](tableName + "_enabled", O.Default(false))
    def * =  name ~ description ~ majVer ~ minVer ~ enabled ~ modified.? ~ created ~ id.?  <>
      (Module.tupled, Module.unapply _)
  }

  object Types extends DescribedThingTable[Type]("types") {
    def moduleId = column[Long](tableName + "_moduleId")
    def moduleId_fkey = foreignKey(tableName + "_moduleId_fkey", moduleId, Modules)(_.id)
    def * = name ~ description ~ moduleId ~ created ~ id.? <> (Type.tupled, Type.unapply _)
  }

  object Bundles extends ThingTable[Bundle]("bundles") {
    def typeId = column[Long](tableName + "_typeId")
    def typeId_fkey = foreignKey(tableName + "_typeId_fkey", typeId, Types)(_.id)
    def payload = column[Clob](tableName + "_payload", O.NotNull)
    def * = name ~ description ~ typeId ~ payload ~ modified.? ~ created ~ id.? <>
      (Bundle.tupled, Bundle.unapply _)
  }

  def coreDDL : DDL =  Modules.ddl ++ Types.ddl ++ Bundles.ddl
}
