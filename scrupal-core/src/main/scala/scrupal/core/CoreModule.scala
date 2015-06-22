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
import scrupal.api.types._
import scrupal.core.entities.{PageEntity, EchoEntity}
import scrupal.storage.api.SchemaDesign
import scrupal.utils.Patterns.{Identifier, _}
import scrupal.utils.Validation.Location
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
  override val settingsType =
    BundleType('CoreConfiguration, "The definition of the CoreModule's configuration parameters", Map(
      notes_key → Markdown_t
    ))

  override val settingsDefaults = Map(
    notes_key → "No notes."
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

  object AnyType_t extends AnyType('Any, "A type that accepts any value")

  object Boolean_t extends BooleanType('TheBoolean, "A type that accepts true/false values")

  /** The Scrupal Type for information about Sites */
  object SiteInfo_t
    extends BundleType('SiteInfo, "Basic information about a site that Scrupal will serve.",
      fields = Map(
        "name" -> Identifier_t,
        "title" -> Identifier_t,
        "domain" -> DomainName_t,
        "port" -> TcpPort_t,
        "admin_email" -> EmailAddress_t,
        "copyright" -> Identifier_t
      )
    )

  object PageBundle_t
    extends BundleType('PageBundle, "Information bundle for a page entity.",
      fields = Map (
        "title" -> Title_t,
        "body" -> Markdown_t
        // TODO: Figure out how to structure a bundle to factor in constructing a network of nodes
        // 'master -> Node_t,
        // 'defaultLayout -> Node_t,
        // 'body -> Node_t,
        // 'blocks ->
      )
    )
  object AnyReal_t extends RealType('AnyReal,
    "A type that accepts any double floating point value", Double.MinValue, Double.MaxValue)

  object Regex_t extends RegexType('Regex,
    "Regular expression type")

  object AnyString_t extends StringType('AnyString,
    "A type that accepts any string input", ".*".r, 1024 * 1024)

  object NonEmptyString_t extends StringType('NonEmptyString,
    "A type that accepts any string input except empty", ".+".r, 1024 * 1024)

  /** The Scrupal Type for the identifier of things */
  object Identifier_t extends StringType('Identifier,
    "Scrupal Identifier", anchored(Identifier), 64, "an identifier")

  object Password_t extends StringType('Password,
    "A type for human written passwords", anchored(Password), 64, "a password") {
    override def validate(ref : Location, value : String) = {
      simplify(ref, value, "String") {
        case bs : String if bs.length > maxLen ⇒
          Some(s"Value is too short for a password.")
        case bs : String if !regex.pattern.matcher(bs).matches() ⇒
          Some(s"Value is not legal for a password.")
        case bs : String ⇒
          None
        case _ ⇒
          Some("")
      }
    }
  }

  object Description_t extends StringType('Description,
    "Scrupal Description", anchored(".+".r), 1024)

  object Markdown_t extends StringType('Markdown,
    "Markdown document type", anchored(Markdown), patternName = "markdown formatting")

  /** The Scrupal Type for domain names per  RFC 1035, RFC 1123, and RFC 2181 */
  object DomainName_t extends StringType('DomainName,
    "RFC compliant Domain Name", anchored(DomainName), 253, "a domain name")

  /** The Scrupal Type for Uniform Resource Locators.
    * We should probably have one for URIs too,  per http://tools.ietf.org/html/rfc3986
    */
  object URL_t extends StringType('URL,
    "Uniform Resource Locator", anchored(UniformResourceLocator))

  /** The Scrupal Type for IP version 4 addresses */
  object IPv4Address_t extends StringType('IPv4Address,
    "A type for IP v4 Addresses", anchored(IPv4Address), 15, "an IPv4 address")

  /** The Scrupal Type for Email addresses */
  object EmailAddress_t extends StringType('EmailAddress,
    "An email address", anchored(EmailAddress), 253, "an e-mail address")

  object LegalName_t extends StringType('LegalName,
    "The name of a person or corporate entity", anchored(LegalName), 128, "a legal name.")

  object Title_t extends StringType('Title,
    "A string that is valid for a page title", anchored(Title), 70, "a page title")

  object AnyTimestamp_t extends TimestampType('AnyTimestamp,
    "A type that accepts any timestamp value")

  object AnyInteger_t extends RangeType('AnyInteger,
    "A type that accepts any integer value", Int.MinValue, Int.MaxValue)

  /** The Scrupal Type for TCP port numbers */
  object TcpPort_t extends RangeType('TcpPort,
    "A type for TCP port numbers", 1, 65535) {
  }

  object UnspecificQuantity_t extends SelectionType('UnspecificQuantity,
    "A simple choice of quantities that do not specifically designate a number",
    Seq("None", "Some", "Any", "Both", "Few", "Several", "Most", "Many", "All")
  )

  object Theme_t extends SelectionType('Theme, "Choice of themes", DataCache.themes)

  object Site_t extends SelectionType('Site, "Choice of sites", DataCache.sites)

  /** The core types that Scrupal provides to all modules */
  def types = Seq[Type[_]](
    AnyType_t, AnyString_t, NonEmptyString_t, Password_t, AnyInteger_t, AnyReal_t, AnyTimestamp_t, Boolean_t,
    Identifier_t, Description_t, Markdown_t, DomainName_t, TcpPort_t, URL_t, IPv4Address_t, EmailAddress_t,
    LegalName_t, SiteInfo_t, PageBundle_t, settingsType, UnspecificQuantity_t
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
