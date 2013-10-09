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

import play.api.libs.json._

import scrupal.api._
import scala.collection.immutable.HashMap
import scala.util.matching.Regex
import java.sql.{SQLException, DriverManager}
import specs2.text
import views.html.helper.repeat
import play.api.libs.ws.WS

object Patterns {
  /** Join a sequence of patterns together */
  def join(r: Regex*) : Regex = {
    val result: StringBuffer = new StringBuffer(1024)
    r.foldLeft(result){
      case (result:StringBuffer, regex:Regex) => result.append(regex.unanchored.pattern.pattern); result
    }.toString().r
  }

  /** Make a pattern into various repeating forms of itself  */
  def anchored(r:Regex) = ("^" + r.pattern.pattern + "$").r
  def group(r:Regex) = ("(?:" + r.pattern.pattern + ")").r
  def capture(r:Regex) = ("(" + r.pattern.pattern + ")").r
  def optional(r: Regex)  = ("(?:" + r.pattern.pattern + ")?").r
  def atLeastOne(r:Regex) = ("(?:" + r.pattern.pattern + ")+").r
  def zeroOrMore(r:Regex) = ("(?:" + r.pattern.pattern + ")*").r
  def alternate(r1:Regex, r2:Regex) = ("(?:" + r1.pattern.pattern + ")|(?:" + r2.pattern.pattern + ")").r

  // From RFC 2822. The pattern names below match the grammar production names and definitions from the RFC
  val atext = "[-A-Za-z0-9!#$%&'*+/=?^_`|~]".r
  val atom = atLeastOne(atext)
  val dot_atom = join(atom, zeroOrMore(join("[.]".r,atom)))
  val no_ws_ctl = "[\\u0001-\\u0008\\u000B-\\u000C\\u000E-\\u001F\\u007F]".r
  val qtext = alternate(no_ws_ctl, "[!\\u0023-\\u005B\\u005D-\\u007E]".r)
  val quoted_string = join("\"".r, zeroOrMore(qtext), "\"".r)
  val local_part = alternate(dot_atom,quoted_string)
  val quoted_pair = "\\\\.".r
  val dtext = join(no_ws_ctl, "[!-Z^-~]".r)
  val dcontent = alternate(dtext,quoted_pair)
  val domain_literal = join("\\[]".r, zeroOrMore(dcontent), "]".r )
  val domain_part = alternate(dot_atom,domain_literal)
  val addr_spec = join(group(local_part), "@".r, group(domain_part))

  val EmailAddress = addr_spec
  val Identifier = "[-A-Za-z0-9_+=|!.^@#%*?]+".r
  val LegalName =  join(Identifier,zeroOrMore(join(" ".r, Identifier)))
  val DomainName = "(?:(?:[a-zA-Z0-9][-a-zA-Z0-9]*[a-zA-Z0-9])\\.){0,126}(?:[A-Za-z0-9][-A-Za-z0-9]*[A-Za-z0-9])".r
  val TcpPort =
    "(?:\\d|\\d{2}|\\d{3}|\\d{4}|[0-5]\\d{4}|6(?:(?:[0-4]\\d{3})|5(?:(?:[0-4]\\d{2})|5(?:(?:[0-2]\\d)|3[0-5]))))".r
  val IPv4Address =
    "(?:(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])".r
}

import Patterns._

/** The Scrupal Type for the identifier of things */
object Identifier_t extends StringType('Identifier, "Scrupal Identifier", anchored(Identifier),64)

/** The Scrupal Type for domain names per  RFC 1035, RFC 1123, and RFC 2181 */
object DomainName_t extends StringType('DomainName, "RFC compliant Domain Name", anchored(DomainName), 253)

/** The Scrupal Type for TCP port numbers */
object TcpPort_t extends RangeType('TcpPort, "A type for TCP port numbers", 1, 65535)

/** The Scrupal Type for Uniform Resource Identifiers per http://tools.ietf.org/html/rfc3986 */
object URI_t extends Type('URI, "Uniform Resource Identifier") {
  override def validate(value: JsValue) : JsResult[Boolean]= {
    value match {
      case v: JsString => {
        try { new URI( v.value ); JsSuccess(true) }
        catch { case xcptn: URISyntaxException => JsError(xcptn.getMessage) }
      }
      case x => JsError("Expecting to validate a URI against a string, not " + x.getClass().getSimpleName())
    }
  }
}

/** What constitutes a valid JDBC URL for Scrupal.
  * Note that we validate this empirically by asking JDBC's DriverManager for the corresponding driver. This means
  * that the validity of JDBC URL types is context sensitive, depending on which drivers are accessible from the
  * classpath.
  */
object JDBC_URL_t extends Type('JDBC_URL, "Java Database Connection URL") {
  override def validate(value: JsValue) : JsResult[Boolean]= {
    value match {
      case v: JsString => {
        try {
          DriverManager.getDriver(v.value)
          JsSuccess(true)
        }
        catch { case xcptn: SQLException => JsError(xcptn.getMessage) }
      }
      case x => JsError("Expecting to validate a URI against a string, not " + x.getClass().getSimpleName())
    }
  }
}

/** The Scrupal Type for IP version 4 addresses */
object IPv4Address_t extends StringType('IPv4Address, "A type for IP v4 Addresses", anchored(IPv4Address), 15)

/** The Scrupal Type for Email addresses */
object EmailAddress_t extends StringType('EmailAddress, "An email address", anchored(EmailAddress), 253)

object LegalName_t extends StringType('LegalName, "The name of a person or corporate entity", anchored(LegalName), 128)

/** The Scrupal Type for information about Sites */
object SiteInfo_t extends TraitType('SiteInfo, "Basic information about a site that Scrupal will serve.",
  HashMap(
    'name -> Identifier_t,
    'title -> Identifier_t,
    'domain -> DomainName_t,
    'port -> TcpPort_t,
    'admin_email -> EmailAddress_t,
    'copyright -> Identifier_t
  ))
