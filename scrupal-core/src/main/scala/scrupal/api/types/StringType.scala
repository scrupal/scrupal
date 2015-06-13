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

package scrupal.api.types

import scrupal.api._
import scrupal.utils.Patterns._
import scrupal.utils.Validation.Location

import scala.util.matching.Regex

/** A String type constrains a string by defining its content with a regular expression and a maximum length.
  *
  * @param id THe name of the string type
  * @param description A brief description of the string type
  * @param regex The regular expression that specifies legal values for the string type
  * @param maxLen The maximum length of this string type
  */
case class StringType(
  id : Identifier,
  description : String,
  regex : Regex,
  maxLen : Int = Int.MaxValue,
  patternName : String = "pattern"
) extends Type[String] {
  override type ValueType = String
  require(maxLen >= 0)
  def validate(ref : Location, value : String) = {
    simplify(ref, value, "String") {
      case s : String if s.length > maxLen ⇒
        Some(s"String of length ${s.length} exceeds maximum of $maxLen.")
      case s : String if !regex.pattern.matcher(s).matches() ⇒
        Some(s"'$s' does not match $patternName.")
      case s : String ⇒
        None
      case _ ⇒
        Some("")
    }
  }
  override def kind = 'String
}

object AnyString_t
  extends StringType('AnyString, "A type that accepts any string input", ".*".r, 1024 * 1024)

object NonEmptyString_t
  extends StringType('NonEmptyString, "A type that accepts any string input except empty", ".+".r, 1024 * 1024)

/** The Scrupal Type for the identifier of things */
object Identifier_t
  extends StringType('Identifier, "Scrupal Identifier", anchored(Identifier), 64, "an identifier")

object Password_t
  extends StringType('Password, "A type for human written passwords", anchored(Password), 64, "a password") {
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

object Description_t
  extends StringType('Description, "Scrupal Description", anchored(".+".r), 1024)

object Markdown_t
  extends StringType('Markdown, "Markdown document type", anchored(Markdown), patternName = "markdown formatting")

/** The Scrupal Type for domain names per  RFC 1035, RFC 1123, and RFC 2181 */
object DomainName_t
  extends StringType('DomainName, "RFC compliant Domain Name", anchored(DomainName), 253, "a domain name")

/** The Scrupal Type for Uniform Resource Locators.
  * We should probably have one for URIs too,  per http://tools.ietf.org/html/rfc3986
  */
object URL_t
  extends StringType('URL, "Uniform Resource Locator", anchored(UniformResourceLocator))

/** The Scrupal Type for IP version 4 addresses */
object IPv4Address_t
  extends StringType('IPv4Address, "A type for IP v4 Addresses", anchored(IPv4Address), 15, "an IPv4 address")

/** The Scrupal Type for Email addresses */
object EmailAddress_t
  extends StringType('EmailAddress, "An email address", anchored(EmailAddress), 253, "an e-mail address")

object LegalName_t
  extends StringType('LegalName, "The name of a person or corporate entity", anchored(LegalName), 128, "a legal name.")

object Title_t
  extends StringType('Title, "A string that is valid for a page title", anchored(Title), 70, "a page title")

