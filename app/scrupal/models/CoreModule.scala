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

import java.net.{URISyntaxException, URI}
import java.sql.{SQLException, DriverManager}

import scala.collection.immutable.HashMap

import play.api.Configuration
import play.api.libs.json._
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsString

import org.joda.time.DateTime

import scala.slick.session.Session

import Patterns._
import scrupal.api._
import scrupal.utils.Version
import scrupal.db.{SupportedDatabases,Sketch,Schema,CoreSchema}
import play.libs.F


/** Scrupal's Core Module.
  * This is the base module of all modules. It provides the various abstractions that permit other modules to extend
  * its functionality. The Core module defines the simple, trait, bundle and entity types
 * Further description here.
 */
object CoreModule extends Module (
  'Core,
  "Scrupal's Core module for core, essential functionality.",
  Version(0,1,0),
  Version(0,0,0),
  true
) {
  /** The Scrupal Type for the identifier of things */
  object Identifier_t extends StringType('Identifier, "Scrupal Identifier", id, anchored(Identifier), 64)

  object Description_t extends StringType('Description, "Scrupal Description", id, anchored(Markdown), 1024)

  object Markdown_t extends StringType('Markdown, "Markdown document type", id, anchored(Markdown))

  /** The Scrupal Type for domain names per  RFC 1035, RFC 1123, and RFC 2181 */
  object DomainName_t extends StringType('DomainName, "RFC compliant Domain Name", id, anchored(DomainName), 253)

  /** The Scrupal Type for TCP port numbers */
  object TcpPort_t extends RangeType('TcpPort, "A type for TCP port numbers", 'Core, 1, 65535)

  /** The Scrupal Type for Uniform Resource Identifiers per http://tools.ietf.org/html/rfc3986 */
  object URI_t extends Type('URI, "Uniform Resource Identifier", id) {
    override def validate(value: JsValue) : JsResult[Boolean]= {
      value match {
        case v: JsString => {
          try { new URI( v.value ); JsSuccess(true) }
          catch { case xcptn: URISyntaxException => JsError(xcptn.getMessage) }
        }
        case x => JsError("Expecting to validate a URI against a string, not " + x.getClass().getSimpleName())
      }
    }
    override def kind = 'URI
  }

  /** What constitutes a valid JDBC URL for Scrupal.
    * Note that we validate this empirically by asking JDBC's DriverManager for the corresponding driver. This means
    * that the validity of JDBC URL types is context sensitive, depending on which drivers are accessible from the
    * classpath.
    */
  object JDBC_URL_t extends Type('JDBC_URL, "Java Database Connection URL", id) {
    override def validate(value: JsValue) : JsResult[Boolean]= {
      value match {
        case v: JsString => {
          try {
            // NOTE: We do not want to do the following here. This is a syntactical check, not a connection check
            // database.createSession().close()
            val supported = SupportedDatabases.forJDBCUrl(v.value)
            if (supported.isEmpty)
              JsError("URL not valid for Scrupal")
            else
              JsSuccess(supported.isDefined)
          }
          catch { case xcptn: Throwable => JsError(xcptn.getMessage) }
        }
        case x => JsError("Expecting to validate a URI against a string, not " + x.getClass().getSimpleName())
      }
    }
    override def kind = 'JDBC_URL
  }

  /** The Scrupal Type for IP version 4 addresses */
  object IPv4Address_t extends StringType('IPv4Address, "A type for IP v4 Addresses", id, anchored(IPv4Address),
    15)

  /** The Scrupal Type for Email addresses */
  object EmailAddress_t extends StringType('EmailAddress, "An email address", id, anchored(EmailAddress), 253)

  object LegalName_t extends StringType('LegalName, "The name of a person or corporate entity", id,
    anchored(LegalName), 128)

  /** The Scrupal Type for information about Sites */
  object SiteInfo_t extends  BundleType('SiteInfo, "Basic information about a site that Scrupal will serve.", id,
    fields = HashMap(
      'name -> Identifier_t,
      'title -> Identifier_t,
      'domain -> DomainName_t,
      'port -> TcpPort_t,
      'admin_email -> EmailAddress_t,
      'copyright -> Identifier_t
    )
  )

  object PageBundle_t extends BundleType('PageBundle, "Information bundle for a page entity.", id, fields = HashMap (
    'name -> Identifier_t,
    'description -> Markdown_t,
    'body -> Markdown_t
  ))

  /** The core types that Scrupal provides to all modules */
  override def types = Seq[Type](
    Identifier_t, Description_t, Markdown_t, DomainName_t, TcpPort_t, URI_t, JDBC_URL_t, IPv4Address_t, EmailAddress_t,
    LegalName_t, SiteInfo_t, PageBundle_t
  )

  object PageEntity extends Entity('Page, "An entity for simple HTML5 pages.", PageBundle_t.id)

  override def entities = Seq[Entity](
    PageEntity
  )
  /** Settings for the core
    */
  override def settings = Configuration.empty

  object DebugFooter extends Feature('DebugFooter, "Show tables of debug information at bottom of each page.", false)

  override def features = Seq(
    DebugFooter
  )

  override def schemas(sketch: Sketch)(implicit session: Session) : Seq[Schema] = Seq( new CoreSchema(sketch) )

 // val schema = new CoreSchema
}
