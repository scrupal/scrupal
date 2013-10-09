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

/** The Scrupal Type for the identifier of things */
object Identifier_t extends StringType('Identifier, "A type for Scrupal Identifiers", "^[-A-Za-z0-9_+=|!.^@#%*?]+$".r,64)

/** The Scrupal Type for domain names per  RFC 1035, RFC 1123, and RFC 2181 */
object DomainName_t extends StringType('DomainName, "A type for domain names",
  "^(([a-zA-Z0-9][-a-zA-Z0-9]*[a-zA-Z0-9])\\.){0,126}([A-Za-z0-9][-A-Za-z0-9]*[A-Za-z0-9])$".r, 253)

/** The Scrupal Type for Uniform Resource Identifiers per http://tools.ietf.org/html/rfc3986 */
object URI_t extends Type('URI, "A type for validating URI strings.") {
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

/** The Scrupal Type for IP version 4 addresses */
object IPv4Address_t extends StringType('IPv4Address, "A type for IP v4 Addresses",
  "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$".r, 15)

/** The Scrupal Type for TCP port numbers */
object TcpPort_t extends RangeType('TcpPort, "A type for TCP port numbers", 1, 65535)

/** The Scrupal Type for Email addresses */
object EmailAddress_t extends StringType('EmailAddress, "An email address",
  "^([a-z0-9_.-]+)@([-0-9a-z.]+)\\.([a-z.]{2,6})$".r, 253)


