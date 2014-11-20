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

import java.util.Date

import reactivemongo.bson._
import scrupal.api._
import scrupal.utils.Patterns._
import spray.http.{MediaTypes, MediaType}

import scala.concurrent.duration.Duration
import scala.util.matching.Regex
import scala.language.existentials

object AnyType_t extends AnyType('Any, "A type that accepts any value")

object AnyString_t extends StringType('AnyString, "A type that accepts any string input", ".*".r, 1024*1024)

object TheBoolean_t extends BooleanType('TheBoolean, "A type that accepts true/false values")

object NonEmptyString_t extends
  StringType('NonEmptyString, "A type that accepts any string input except empty", ".+".r, 1024*1024)

object Password_t extends
  StringType('Password, "A type for human written passwords", anchored(Password), 64)

object AnyInteger_t extends
  RangeType('AnyInteger, "A type that accepts any integer value", Int.MinValue, Int.MaxValue)

object AnyReal_t extends
  RealType('AnyReal, "A type that accepts any double floating point value", Double.MinValue, Double.MaxValue)

object AnyTimestamp_t extends
  TimestampType('AnyTimestamp, "A type that accepts any timestamp value")

object Boolean_t extends
  BooleanType('Boolean, "A Boolean truth value accepting 0/1 values or true/false, on/off, etc.")

/** The Scrupal Type for the identifier of things */
object Identifier_t
  extends StringType('Identifier, "Scrupal Identifier", anchored(Identifier), 64)

object Description_t
  extends StringType('Description, "Scrupal Description", anchored(Markdown), 1024)

object Markdown_t
  extends StringType('Markdown, "Markdown document type", anchored(Markdown))

/** The Scrupal Type for domain names per  RFC 1035, RFC 1123, and RFC 2181 */
object DomainName_t
  extends StringType('DomainName, "RFC compliant Domain Name", anchored(DomainName), 253)

/** The Scrupal Type for TCP port numbers */
object TcpPort_t
  extends RangeType('TcpPort, "A type for TCP port numbers", 1, 65535) {
}

/** The Scrupal Type for Uniform Resource Locators.
  * We should probably have one for URIs too,  per http://tools.ietf.org/html/rfc3986
  */
object URL_t
  extends StringType ('URL, "Uniform Resource Locator", anchored(UniformResourceLocator))

/** The Scrupal Type for IP version 4 addresses */
object IPv4Address_t
  extends StringType('IPv4Address, "A type for IP v4 Addresses", anchored(IPv4Address), 15)

/** The Scrupal Type for Email addresses */
object EmailAddress_t
  extends StringType('EmailAddress, "An email address", anchored(EmailAddress), 253)

object LegalName_t
  extends StringType('LegalName, "The name of a person or corporate entity", anchored(LegalName), 128)

object Title_t
  extends StringType('Title, "A string that is valid for a page title", anchored(Title), 70)

/** The Scrupal Type for information about Sites */
object SiteInfo_t
  extends  BundleType('SiteInfo, "Basic information about a site that Scrupal will serve.",
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
