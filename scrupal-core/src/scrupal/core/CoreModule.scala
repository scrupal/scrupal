/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
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

package scrupal.core

import java.net.URL

import reactivemongo.bson.{BSONBoolean, BSONDocument, BSONString, BSONValue}
import scrupal.db.{Schema,DBContext}
import scrupal.core.api._
import scrupal.utils.{OSSLicense, Version}

import CoreFeatures._

/** Scrupal's Core Module.
  * This is the base module of all modules. It provides the various abstractions that permit other modules to extend
  * its functionality. The Core module defines the simple, trait, bundle and entity types
 * Further description here.
 */
object CoreModule extends Module {
  def id = Core
  val description = "Scrupal's Core module for core, essential functionality."
  val version = Version(0,1,0)
  val obsoletes = Version(0,0,0)
  val moreDetailsURL = new URL("http://modules.scrupal.org/doc/" + label)
  val author : String = "Reid Spencer"
  val copyright : String = "(C) 2014 Reactific Systems, Inc. All Rights Reserved"
  val license = OSSLicense.GPLv3

  val notes_key = "Notes"
  override val settingsType =
    BundleType('CoreConfiguration, "The definition of the CoreModule's configuration parameters", Map(
      notes_key → Markdown_t
    ))

  override val settingsDefault = BSONDocument(Map(
    notes_key → BSONString("No notes.")
  ))

  override def features = Seq(
    DebugFooter, DevMode, ConfigWizard, RESTAPIAccess, RESTAPIDocumentation, OnePageApplications
  )

  /** The core types that Scrupal provides to all modules */
  override def types = Seq[Type](
    AnyType_t, AnyString_t, NonEmptyString_t, Password_t, AnyInteger_t, AnyReal_t, AnyTimestamp_t, Boolean_t,
    Identifier_t, Description_t, Markdown_t, DomainName_t, TcpPort_t, URL_t, IPv4Address_t, EmailAddress_t,
    LegalName_t, SiteInfo_t, PageBundle_t, settingsType
  )

  object PageEntity extends Entity {
    def id = 'Page
    def kind = 'Page
    val description =  "An entity for simple HTML5 pages."
    val author = CoreModule.author
    val copyright = CoreModule.copyright
    val license = CoreModule.license
    val instanceType = PageBundle_t
  }

  override def entities = Seq[Entity](
    PageEntity
  )

  override def nodes = Seq[Node]()

  override def handlers = Seq()

  override def schemas(implicit dbc: DBContext) : Seq[Schema] = Seq( new CoreSchema(dbc) )
}
