/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
 *                                                                                                                    *
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
 * with the License. You may obtain a copy of the License at                                                          *
 *                                                                                                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                                                                     *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
 * the specific language governing permissions and limitations under the License.                                     *
 **********************************************************************************************************************/

package scrupal.core

import java.net.URL

import play.api.Configuration
import scrupal.api._
import scrupal.core.entities.{PageEntity, EchoEntity}
import scrupal.storage.api.SchemaDesign
import scrupal.utils.Version

/** Scrupal's Core Module.
  * This is the base module of all modules. It provides the various abstractions that permit other modules to extend
  * its functionality. The Core module defines the simple, trait, bundle and entity types
  * Further description here.
  */
case class CoreModule(implicit val scrupal : Scrupal) extends Module {
  def id = Core
  val description = "Scrupal's Core module for core, essential functionality."
  val version = Version(0, 1, 0)
  val obsoletes = Version(0, 0, 0)
  val moreDetailsURL = new URL("http://modules.scrupal.org/doc/" + label)
  val author = scrupal.author
  val copyright = scrupal.copyright
  val license = scrupal.license

  val notes_key = "Notes"
  override val _settings = Settings("Core Settings",
    BundleType('CoreSettings, "The definition of the CoreModule's configuration parameters", Map(
      notes_key → Markdown_t
    )),
    Map(notes_key → "No notes." )
  )

  /** Controls whether debug information is displayed at the bottom of page requests.
    * Debug data will also be tacked on to the end of JSON data delivered via the REST api in the "debug" field.
    */
  lazy val DebugFooter = Feature('DebugFooter,
    "Show tables of debug information at bottom of each page.", Some(this))

  /** Developer Mode Controls Some Aspects of Scrupal Functionality
    * The administrator of the site(s) might be a developer building a new module or extending Scrupal itself. For
    * such users, the developer mode can be set to relax some of Scrupal's security restrictions. Most notably when
    * DevMode is false and the site is not configured, every URL will take you to the configuration wizard. This may
    * not be convenient for developers, but saves a lot of confusion for end users as the site directs them towards
    * what they need to know next. :)
    */
  lazy val DevMode = Feature('DeveloperMode,
    "Controls whether development mode facilities are enabled", Some(this))

  /** Controls accessibility of the ConfigWizard
    * The ConfigWizard makes first time configuration easier but should not be enabled for production systems.
    * Disabling means web visitors will simply be redirected to the index if they attempt to use the configuration
    * urls.
    */
  lazy val ConfigWizard = Feature('ConfigWizard,
    "Controls whether configuration by web request is allowed", Some(this))

  /** Controls access to the REST API
    * Administrators may wish to turn off REST API access temporarily to ensure all clients are unable to transact
    * requests.
    */
  lazy val RESTAPIAccess = Feature('RESTAPIAccess,
    "Allows access to entity instances via Scrupal's REST API", Some(this))

  /** Controls access to the REST API Documentation
    * Administrators may wish to disable REST API Documentation
    */
  lazy val RESTAPIDocumentation = Feature('RESTAPIDocumentation,
    "Allows access to auto-generated documentation for REST API entity instances.", Some(this))

  lazy val TopIndexPage = Feature('TopIndexPage,
    "Provides a Page entity to be displayed at the / path for a site.", Some(this))

  /** Controls access to the One Page Application URLs
    * Administrators may wish to selectively disable the One Page Applications.
    */
  lazy val OnePageApplications = Feature('OnePageApplications,
    "A feature that supports AngularJS based one page applications", Some(this), implemented = false)

  def features = Seq(
    DebugFooter, DevMode, ConfigWizard, RESTAPIAccess, RESTAPIDocumentation, OnePageApplications
  )


  def entities = Seq[Entity](
    PageEntity()(scrupal), EchoEntity()(scrupal)
  )

  def nodes = Seq[Node]()

  def handlers = Seq()

  override def schemas : Seq[SchemaDesign] = Seq(CoreSchemaDesign())

  override protected[scrupal] def bootstrap(config : Configuration) = {
    super.bootstrap(config)
    // Make things from the configuration override defaults and database read settings
    // Features
    config.getBoolean("scrupal.developer.mode") map { value ⇒ DevMode.enable(this, value) }
    config.getBoolean("scrupal.developer.footer") map { value ⇒ DebugFooter.enable(this, value) }
    config.getBoolean("scrupal.config.wizard") map { value ⇒ ConfigWizard.enable(this, value) }
  }
}
