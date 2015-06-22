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

import scrupal.core.CoreModule._
import scrupal.test.TestTypes
import scrupal.api._
import scrupal.api.types._
import scrupal.test.ScrupalApiSpecification

/** Title Of Thing.
  *
  * Description of thing
  */
class TypesSpec extends ScrupalApiSpecification("Types") {


  "Identifier_t" should {
    "accept ***My-Funky.1d3nt1f13r###" in TestTypes() { t: TestTypes ⇒
      Identifier_t.validate(t.vLoc, "***My-Funky.1d3nt1f13r###").isError must beFalse
    }
    "reject 'Not An Identifier'" in TestTypes() { t: TestTypes ⇒
      Identifier_t.validate(t.vLoc, "Not An Identifier ").isError must beTrue
    }
  }

  "AnyType_t" should {
    "have some test cases" in { pending }
  }
  "AnyString_t" should {
    "have some test cases" in { pending }
  }
  "AnyInteger_t" should {
    "have some test cases" in { pending }
  }
  "AnyReal_t" should {
    "have some test cases" in { pending }
  }
  "AnyTimestamp_t" should {
    "have some test cases" in { pending }
  }
  "Boolean_t" should {
    "have some test cases" in { pending }
  }
  "NonEmptyString_t" should {
    "have some test cases" in { pending }
  }
  "Password_t" should {
    "have some test cases" in { pending }
  }
  "Description_t" should {
    "have some test cases" in { pending }
  }
  "Markdown_t" should {
    "have some test cases" in { pending }
  }


  "DomainName_t" should {
    "accept scrupal.org" in TestTypes() { t: TestTypes ⇒
      DomainName_t.validate(t.vLoc, "scrupal.org").isError must beFalse
    }
    "reject ###.999" in TestTypes() { t: TestTypes ⇒
      DomainName_t.validate(t.vLoc, "###.999").isError must beTrue
    }
  }

  "URI_t" should {
    "accept http://user:pw@scrupal.org/path?q=where#extra" in TestTypes() { t: TestTypes ⇒
      URL_t.validate(t.vLoc, "http://user:pw@scrupal.org/path?q=where#extra").isError must beFalse
    }
    "reject Not\\A@URI" in TestTypes() { t: TestTypes ⇒
      URL_t.validate(t.vLoc, "Not\\A@URI").isError must beTrue
    }
  }

  "IPv4Address_t" should {
    "accept 1.2.3.4" in TestTypes() { t: TestTypes ⇒
      IPv4Address_t.validate(t.vLoc, "1.2.3.4").isError must beFalse
    }
    "reject 1.2.3.400" in TestTypes() { t: TestTypes ⇒
      IPv4Address_t.validate(t.vLoc, "1.2.3.400").isError must beTrue
    }
  }

  "TcpPort_t" should {
    "accept 8088" in TestTypes() { t: TestTypes ⇒
      TcpPort_t.validate(t.vLoc, 8088).isError must beFalse
    }
    "reject 65537" in TestTypes() { t: TestTypes ⇒
      TcpPort_t.validate(t.vLoc, "65537").isError must beTrue
    }
  }

  "EmailAddress_t" should {
    "accept someone@scrupal.org" in TestTypes() { t: TestTypes ⇒
      // println("Email Regex: " + EmailAddress_t.regex.pattern.pattern)
      EmailAddress_t.validate(t.vLoc, "someone@scrupal.org").isError must beFalse
    }
    "reject white space" in TestTypes() { t: TestTypes ⇒
      EmailAddress_t.validate(t.vLoc, " \t\r\n").isError must beTrue
    }
    "reject nobody@ scrupal dot org" in TestTypes() { t: TestTypes ⇒
      EmailAddress_t.validate(t.vLoc, "nobody@ 24 dot com").isError must beTrue
    }
    "reject no body@scrupal.org" in TestTypes() { t: TestTypes ⇒
      EmailAddress_t.validate(t.vLoc, "no body@scrupal.org").isError must beTrue
    }
  }

  "LegalName_t" should {
    "accept 'My Legal Name'" in TestTypes() { t: TestTypes ⇒
      LegalName_t.validate(t.vLoc, "My Legal Name").isError must beFalse
    }
    "reject tab char" in TestTypes() { t: TestTypes ⇒
      LegalName_t.validate(t.vLoc, "\t").isError must beTrue
    }
  }


}
