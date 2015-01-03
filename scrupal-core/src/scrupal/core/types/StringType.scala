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

package scrupal.core.types

import reactivemongo.bson.{BSONString, BSONValue}
import scrupal.core.api.Identifier
import scrupal.core.api._
import scrupal.utils.Patterns._

import scala.util.matching.Regex

/** A String type constrains a string by defining its content with a regular expression and a maximum length.
  *
  * @param id THe name of the string type
  * @param description A brief description of the string type
  * @param regex The regular expression that specifies legal values for the string type
  * @param maxLen The maximum length of this string type
  */
case class StringType (
  id : Identifier,
  description : String,
  regex : Regex,
  maxLen : Int = Int.MaxValue
  ) extends Type {
  override type ScalaValueType = String
  require(maxLen >= 0)
  def validate(value: BSONValue) = {
    simplify(value, "String") {
      case bs: BSONString if bs.value.length > maxLen =>
        Some(s"String of length ${bs.value.length} exceeds maximum of $maxLen")
      case bs: BSONString if !regex.pattern.matcher(bs.value).matches() =>
        Some(s"String '${bs.value}' does not match pattern '${regex.pattern.pattern()}")
      case bs: BSONString ⇒
        None
      case x: BSONValue =>
        Some("")
    }
  }
  override def kind = 'String
}

object AnyString_t
  extends StringType('AnyString, "A type that accepts any string input", ".*".r, 1024*1024)

object NonEmptyString_t
  extends StringType('NonEmptyString, "A type that accepts any string input except empty", ".+".r, 1024*1024)

/** The Scrupal Type for the identifier of things */
object Identifier_t
  extends StringType('Identifier, "Scrupal Identifier", anchored(Identifier), 64)

object Password_t
  extends StringType('Password, "A type for human written passwords", anchored(Password), 64)

object Description_t
  extends StringType('Description, "Scrupal Description", anchored(".+".r), 1024)

object Markdown_t
  extends StringType('Markdown, "Markdown document type", anchored(Markdown))

/** The Scrupal Type for domain names per  RFC 1035, RFC 1123, and RFC 2181 */
object DomainName_t
  extends StringType('DomainName, "RFC compliant Domain Name", anchored(DomainName), 253)


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

