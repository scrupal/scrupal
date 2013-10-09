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

import org.specs2.mutable.Specification
import play.api.libs.json.{JsNumber, JsString}

/**
 * One line sentence description here.
 * Further description here.
 */
class TypesSpec extends Specification {

  "DomainName_t" should {
    "accept scrupal.org" in {
      DomainName_t.validate(JsString("scrupal.org")).asOpt.isDefined must beTrue
    }
    "reject ###.999" in {
      DomainName_t.validate(JsString("###.999")).asOpt.isDefined must beFalse
    }
  }

  "Identifier_t" should {
    "accept ***My-Funky.1d3nt1f13r###" in {
      Identifier_t.validate(JsString("***My-Funky.1d3nt1f13r###")).asOpt.isDefined must beTrue
    }
    "reject 'Not An Identifier'" in {
      Identifier_t.validate(JsString("Not An Identifier ")).asOpt.isDefined must beFalse
    }
  }

  "URI_t" should {
    "accept http://user:pw@scrupal.org/path?q=where#extra" in {
      URI_t.validate(JsString("http://user:pw@scrupal.org/path?q=where#extra")).asOpt.isDefined must beTrue
    }
    "reject Not\\A@URI" in {
      URI_t.validate(JsString("Not\\A@URI")).asOpt.isDefined must beFalse
    }
  }

  "JDBC_URL_t" should {
    "accept jdbc:h2:mem:test_url" in {
      JDBC_URL_t.validate(JsString("jdbc:h2:mem:test_url")).asOpt.isDefined must beTrue
    }
    "reject http://user:pw@scrupal.org/path?q=where#extra" in {
      JDBC_URL_t.validate(JsString("http://user:pw@scrupal.org/path?q=where#extra")).asOpt.isDefined must beFalse
    }
  }

  "IPv4Address_t" should {
    "accept 1.2.3.4" in {
      IPv4Address_t.validate(JsString("1.2.3.4")).asOpt.isDefined must beTrue
    }
    "reject 1.2.3.400" in {
      IPv4Address_t.validate(JsString("1.2.3.400")).asOpt.isDefined must beFalse
    }
  }

  "TcpPort_t" should {
    "accept 8088" in {
      TcpPort_t.validate(JsNumber(8088)).asOpt.isDefined must beTrue
    }
    "reject 65537" in {
      TcpPort_t.validate(JsString("65537")).asOpt.isDefined must beFalse
    }
  }

  "EmailAddress_t" should {
    "accept someone@scrupal.org" in {
      println("Email Regex: " + EmailAddress_t.regex.pattern.pattern)
      EmailAddress_t.validate(JsString("someone@scrupal.org")).asOpt.isDefined must beTrue
    }
    "reject white space" in {
      EmailAddress_t.validate(JsString(" \t\r\n")).asOpt.isDefined must beFalse
    }
    "reject nobody@ scrupal dot org" in {
      EmailAddress_t.validate(JsString("nobody@ 24 dot com")).asOpt.isDefined must beFalse
    }
    "reject no body@scrupal.org" in {
      EmailAddress_t.validate(JsString("no body@scrupal.org")).asOpt.isDefined must beFalse
    }
  }

  "LegalName_t" should {
    "accept 'My Legal Name'" in {
      LegalName_t.validate(JsString("My Legal Name")).asOpt.isDefined must beTrue
    }
    "reject tab char" in {
      LegalName_t.validate(JsString("\t")).asOpt.isDefined must beFalse
    }
  }
}
