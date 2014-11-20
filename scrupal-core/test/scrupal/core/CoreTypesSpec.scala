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

import org.specs2.mutable.Specification
import reactivemongo.bson.{BSONInteger, BSONString}

/**
 * One line sentence description here.
 * Further description here.
 */
class CoreTypesSpec extends Specification {

  "DomainName_t" should {
    "accept scrupal.org" in {
      DomainName_t.validate(BSONString("scrupal.org")).isDefined must beFalse
    }
    "reject ###.999" in {
      DomainName_t.validate(BSONString("###.999")).isDefined must beTrue
    }
  }

  "URI_t" should {
    "accept http://user:pw@scrupal.org/path?q=where#extra" in {
      URL_t.validate(BSONString("http://user:pw@scrupal.org/path?q=where#extra")).isDefined must beFalse
    }
    "reject Not\\A@URI" in {
      URL_t.validate(BSONString("Not\\A@URI")).isDefined must beTrue
    }
  }

  "IPv4Address_t" should {
    "accept 1.2.3.4" in {
      IPv4Address_t.validate(BSONString("1.2.3.4")).isDefined must beFalse
    }
    "reject 1.2.3.400" in {
      IPv4Address_t.validate(BSONString("1.2.3.400")).isDefined must beTrue
    }
  }

  "TcpPort_t" should {
    "accept 8088" in {
      TcpPort_t.validate(BSONInteger(8088)).isDefined must beFalse
    }
    "reject 65537" in {
      TcpPort_t.validate(BSONString("65537")).isDefined must beTrue
    }
  }

  "EmailAddress_t" should {
    "accept someone@scrupal.org" in {
      // println("Email Regex: " + EmailAddress_t.regex.pattern.pattern)
      EmailAddress_t.validate(BSONString("someone@scrupal.org")).isDefined must beFalse
    }
    "reject white space" in {
      EmailAddress_t.validate(BSONString(" \t\r\n")).isDefined must beTrue
    }
    "reject nobody@ scrupal dot org" in {
      EmailAddress_t.validate(BSONString("nobody@ 24 dot com")).isDefined must beTrue
    }
    "reject no body@scrupal.org" in {
      EmailAddress_t.validate(BSONString("no body@scrupal.org")).isDefined must beTrue
    }
  }

  "LegalName_t" should {
    "accept 'My Legal Name'" in {
      LegalName_t.validate(BSONString("My Legal Name")).isDefined must beFalse
    }
    "reject tab char" in {
      LegalName_t.validate(BSONString("\t")).isDefined must beTrue
    }
  }
}
