/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.core

import java.net.URL

import reactivemongo.bson.{BSONDocument, BSONString}
import scrupal.core.api._
import scrupal.core.entities.EchoEntity
import scrupal.core.types._
import scrupal.db.DBContext
import scrupal.utils.{Configuration, OSSLicense, Version}

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
  val copyright : String = "(C) 2014 Reactific Software LLC. All Rights Reserved"
  val license = OSSLicense.GPLv3

  val notes_key = "Notes"
  override val settingsType =
    BundleType('CoreConfiguration, "The definition of the CoreModule's configuration parameters", Map(
      notes_key → Markdown_t
    ))

  override val settingsDefault = BSONDocument(Map(
    notes_key → BSONString("No notes.")
  ))

  /** Controls whether debug information is displayed at the bottom of page requests.
    * Debug data will also be tacked on to the end of JSON data delivered via the REST api in the "debug" field.
    */
  lazy val DebugFooter = Feature('DebugFooter,
    "Show tables of debug information at bottom of each page.", Some(CoreModule))

  /** Developer Mode Controls Some Aspects of Scrupal Functionality
    * The administrator of the site(s) might be a developer building a new module or extending Scrupal itself. For
    * such users, the developer mode can be set to relax some of Scrupal's security restrictions. Most notably when
    * DevMode is false and the site is not configured, every URL will take you to the configuration wizard. This may
    * not be convenient for developers, but saves a lot of confusion for end users as the site directs them towards
    * what they need to know next. :)
    */
  lazy val DevMode = Feature('DeveloperMode,
    "Controls whether development mode facilities are enabled", Some(CoreModule))

  /** Controls accessibility of the ConfigWizard
    * The ConfigWizard makes first time configuration easier but should not be enabled for production systems.
    * Disabling means web visitors will simply be redirected to the index if they attempt to use the configuration
    * urls.
    */
  lazy val ConfigWizard = Feature('ConfigWizard,
    "Controls whether configuration by web request is allowed", Some(CoreModule))

  /** Controls access to the REST API
    * Administrators may wish to turn off REST API access temporarily to ensure all clients are unable to transact
    * requests.
    */
  lazy val RESTAPIAccess = Feature('RESTAPIAccess,
    "Allows access to entity instances via Scrupal's REST API", Some(CoreModule))

  /** Controls access to the REST API Documentation
    * Administrators may wish to disable REST API Documentation
    */
  lazy val RESTAPIDocumentation = Feature('RESTAPIDocumentation,
    "Allows access to auto-generated documentation for REST API entity instances.", Some(CoreModule))

  lazy val TopIndexPage = Feature('TopIndexPage,
    "Provides a Page entity to be displayed at the / path for a site.", Some(CoreModule))

  /** Controls access to the One Page Application URLs
    * Administrators may wish to selectively disable the One Page Applications.
    */
  lazy val OnePageApplications = Feature('OnePageApplications,
    "A feature that supports AngularJS based one page applications", Some(CoreModule),implemented=false)

  def features = Seq(
    DebugFooter, DevMode, ConfigWizard, RESTAPIAccess, RESTAPIDocumentation, OnePageApplications
  )

  /** The core types that Scrupal provides to all modules */
  def types = Seq[Type](
    AnyType_t, AnyString_t, NonEmptyString_t, Password_t, AnyInteger_t, AnyReal_t, AnyTimestamp_t, Boolean_t,
    Identifier_t, Description_t, Markdown_t, DomainName_t, TcpPort_t, URL_t, IPv4Address_t, EmailAddress_t,
    LegalName_t, SiteInfo_t, PageBundle_t, settingsType
  )

  object PageEntity extends Entity('Page) {
    def kind = 'Page
    val key = "Page"
    val description =  "An entity for simple HTML5 pages."
    val author = CoreModule.author
    val copyright = CoreModule.copyright
    val license = CoreModule.license
    val instanceType = PageBundle_t
  }

  def entities = Seq[Entity](
    PageEntity, EchoEntity
  )

  def nodes = Seq[Node]()

  def handlers = Seq()

  override def schemas(implicit dbc: DBContext) : Seq[CoreSchema] = Seq( new CoreSchema(dbc, "Scrupal") )

  override protected[scrupal] def bootstrap(config: Configuration) = {
    super.bootstrap(config)
    // Make things from the configuration override defaults and database read settings
    // Features
    config.getBoolean("scrupal.developer.mode") map { value => DevMode.enable(this, value)}
    config.getBoolean("scrupal.developer.footer") map { value => DebugFooter.enable(this, value)}
    config.getBoolean("scrupal.config.wizard") map { value => ConfigWizard.enable(this, value)}
  }
}
