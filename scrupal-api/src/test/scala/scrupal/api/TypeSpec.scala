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

package scrupal.api

import scrupal.test.{ScrupalApiSpecification, TestTypes}

import scala.language.implicitConversions

/** Test specifications for the abstract Type system portion of the API.  */
class TypeSpec extends ScrupalApiSpecification("TypeSpec") {

  "MiddlePeriod" should {
    "accept 'foo.bar'" in TestTypes(scrupal) { t : TestTypes ⇒
      val result = t.MiddlePeriod.validate(t.vLoc, "foo.bar")
      result.isError must beFalse
    }
    "reject 'foo'" in TestTypes(scrupal) { t: TestTypes ⇒
      t.MiddlePeriod.validate(t.vLoc, "foo").isError must beTrue
    }
    "reject 'foo.barbaz'" in TestTypes(scrupal) { t: TestTypes ⇒
      t.MiddlePeriod.validate(t.vLoc, "foo.barbaz").isError must beTrue
    }
  }

  "RangeType(10,20)" should {
    "accept 17" in TestTypes(scrupal) { t: TestTypes ⇒
      t.rangeTy.validate(t.vLoc, 17).isError must beFalse
    }
    "accept 10" in TestTypes(scrupal) { t: TestTypes ⇒
      t.rangeTy.validate(t.vLoc, 10).isError must beFalse
    }
    "accept 20" in TestTypes(scrupal) { t: TestTypes ⇒
      t.rangeTy.validate(t.vLoc, 20).isError must beFalse
    }
    "reject 9" in TestTypes(scrupal) { t: TestTypes ⇒
      t.rangeTy.validate(t.vLoc, 9).isError must beTrue
    }
    "reject 21" in TestTypes(scrupal) { t: TestTypes ⇒
      t.rangeTy.validate(t.vLoc, 21).isError must beTrue
    }
  }

  "RealType(10.1,20.9)" should {
    "accept 17.0" in TestTypes(scrupal) { t: TestTypes ⇒
      t.realTy.validate(t.vLoc, 17.0).isError must beFalse
    }
    "accept 10.2" in TestTypes(scrupal) { t: TestTypes ⇒
      t.realTy.validate(t.vLoc, 10.2).isError must beFalse
    }
    "accept 20.8" in TestTypes(scrupal) { t: TestTypes ⇒
      t.realTy.validate(t.vLoc, 20.8).isError must beFalse
    }
    "reject 10.01" in TestTypes(scrupal) { t: TestTypes ⇒
      t.realTy.validate(t.vLoc, 10.01).isError must beTrue
    }
    "reject 20.91" in TestTypes(scrupal) { t: TestTypes ⇒
      t.realTy.validate(t.vLoc, 20.91).isError must beTrue
    }
  }

  "EnumType(fibonacci)" should {
    "accept 'five'" in TestTypes(scrupal) { t: TestTypes ⇒
      t.enumTy.validate(t.vLoc, "five").isError must beFalse
    }
    "reject 'seven'" in TestTypes(scrupal) { t: TestTypes ⇒
      t.enumTy.validate(t.vLoc, "seven").isError must beTrue
    }
    "provide 13 for 'six' " in TestTypes(scrupal) { t: TestTypes ⇒
      t.enumTy.valueOf("six").get must beEqualTo(13)
    }
  }

  "BLOBType(4)" should {
    "reject a string that is too long" in TestTypes(scrupal) { t: TestTypes ⇒
      val url = "http://foo.com/bar/baz.bin"
      t.blobTy.validate(t.vLoc, url.getBytes(utf8)).isError must beTrue
    }
    "accept a string that is short enough" in TestTypes(scrupal) { t: TestTypes ⇒
      val url = "http"
      t.blobTy.validate(t.vLoc, url.getBytes(utf8)).isError must beFalse
    }
    "accept BSONBinary" in TestTypes(scrupal) { t: TestTypes ⇒
      val url = Array[Byte](0,3,2,1)
      t.blobTy.validate(t.vLoc, url).isError must beFalse
    }
  }

  "ListType(enumTy)" should {
    "reject Seq(6,7)" in TestTypes(scrupal) { t: TestTypes ⇒
      val js = Seq[Atom]( 6, 7 )
      t.listTy.validate(t.vLoc, js).isError must beTrue
    }
    "accept Seq('six')" in TestTypes(scrupal) { t: TestTypes ⇒
      val js = Seq[Atom]("six")
      t.listTy.validate(t.vLoc, js).isError must beFalse
    }
    "accept Seq()" in TestTypes(scrupal) { t: TestTypes ⇒
      val js = Seq[Atom]()
      t.listTy.validate(t.vLoc, js).isError must beFalse
    }
    "accept Seq(\"one\", \"three\", \"five\")" in TestTypes(scrupal) { t: TestTypes ⇒
      val js = Seq[Atom]("one", "three", "five")
      t.listTy.validate(t.vLoc, js).isError must beFalse
    }
    "reject BSONArray('nine')" in TestTypes(scrupal) { t: TestTypes ⇒
      val js = Seq[Atom]("nine")
      t.listTy.validate(t.vLoc, js).isError must beTrue
    }
  }

  "SetType(t.rangeTy)" should {
    "reject Set(\"foo\")" in TestTypes(scrupal) { t: TestTypes ⇒
      val js = Set[Atom]("foo")
      t.setTy.validate(t.vLoc, js).isError must beTrue
    }
    "accept Set(17)" in TestTypes(scrupal) { t: TestTypes ⇒
      val js = Set[Atom](17)
      t.setTy.validate(t.vLoc, js).isError must beFalse
    }
    "accept Set(17,18)" in TestTypes(scrupal) { t: TestTypes ⇒
      val js = Set[Atom](17, 18)
      t.setTy.validate(t.vLoc, js).isError must beFalse
    }
    "accept Set(17,17)" in TestTypes(scrupal) { t: TestTypes ⇒
      val js = Set[Atom](17, 17)
      val result = t.setTy.validate(t.vLoc, js)
      result.isError must beFalse
    }
    "reject Set(21)" in TestTypes(scrupal) { t: TestTypes ⇒
      val js = Set[Atom](21)
      t.setTy.validate(t.vLoc, js).isError must beTrue
    }
  }

  "MapType(realTy)" should {
    "reject Map('foo' -> 7.0)" in TestTypes(scrupal) { t: TestTypes ⇒
      val js = Map[String,Atom]("foo" -> 7.0)
      t.mapTy.validate(t.vLoc, js).isError must beTrue
    }
    "reject Map('foo' -> \"7.0\")" in TestTypes(scrupal) { t : TestTypes ⇒
      val js = Map[String,Atom]("foo" → "7.0")
      t.mapTy.validate(t.vLoc, js).isError must beTrue
    }
    "accept Map('foo' -> 17.0)" in TestTypes(scrupal) { t: TestTypes ⇒
      val js = Map[String,Atom]("foo" -> 17.0)
      t.mapTy.validate(t.vLoc, js).isError must beFalse
    }
  }

  /* TODO: Reinstate Complex Entity With Traits test
  "Complex Entity With Traits" should {
    "accept matching input" in TestTypes(scrupal) { t: TestTypes ⇒
      val js = Map( "trait1" -> t.js1, "trait2" -> t.js2)
      val result = t.AnEntity.validate(t.vLoc, js)
      result.isError must beFalse
    }
    "reject mismatched input" in TestTypes(scrupal) { t: TestTypes ⇒
      val js = Map( "trait1" -> t.js2, "trait2" -> t.js1)
      val result = t.AnEntity.validate(t.vLoc, js)
      result.isError must beTrue
    }
    "accept reversed input" in TestTypes(scrupal) { t: TestTypes ⇒
      val js = Map( "trait2" -> t.js2, "trait1" -> t.js1)
      val result = t.AnEntity.validate(t.vLoc, js)
      result.isError must beFalse
    }
  }
*/

  "Identifier_t" should {
    "accept ***My-Funky.1d3nt1f13r###" in TestTypes(scrupal) { t: TestTypes ⇒
      Identifier_t.validate(t.vLoc, "***My-Funky.1d3nt1f13r###").isError must beFalse
    }
    "reject 'Not An Identifier'" in TestTypes(scrupal) { t: TestTypes ⇒
      Identifier_t.validate(t.vLoc, "Not An Identifier ").isError must beTrue
    }
  }

  // TODO: Implement more type tests
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
    "accept scrupal.org" in TestTypes(scrupal) { t: TestTypes ⇒
      DomainName_t.validate(t.vLoc, "scrupal.org").isError must beFalse
    }
    "reject ###.999" in TestTypes(scrupal) { t: TestTypes ⇒
      DomainName_t.validate(t.vLoc, "###.999").isError must beTrue
    }
  }

  "URI_t" should {
    "accept http://user:pw@scrupal.org/path?q=where#extra" in TestTypes(scrupal) { t: TestTypes ⇒
      URL_t.validate(t.vLoc, "http://user:pw@scrupal.org/path?q=where#extra").isError must beFalse
    }
    "reject Not\\A@URI" in TestTypes(scrupal) { t: TestTypes ⇒
      URL_t.validate(t.vLoc, "Not\\A@URI").isError must beTrue
    }
  }

  "IPv4Address_t" should {
    "accept 1.2.3.4" in TestTypes(scrupal) { t: TestTypes ⇒
      IPv4Address_t.validate(t.vLoc, "1.2.3.4").isError must beFalse
    }
    "reject 1.2.3.400" in TestTypes(scrupal) { t: TestTypes ⇒
      IPv4Address_t.validate(t.vLoc, "1.2.3.400").isError must beTrue
    }
  }

  "TcpPort_t" should {
    "accept 8088" in TestTypes(scrupal) { t: TestTypes ⇒
      TcpPort_t.validate(t.vLoc, 8088).isError must beFalse
    }
    "reject 65537" in TestTypes(scrupal) { t: TestTypes ⇒
      TcpPort_t.validate(t.vLoc, "65537").isError must beTrue
    }
  }

  "EmailAddress_t" should {
    "accept someone@scrupal.org" in TestTypes(scrupal) { t: TestTypes ⇒
      // println("Email Regex: " + EmailAddress_t.regex.pattern.pattern)
      EmailAddress_t.validate(t.vLoc, "someone@scrupal.org").isError must beFalse
    }
    "reject white space" in TestTypes(scrupal) { t: TestTypes ⇒
      EmailAddress_t.validate(t.vLoc, " \t\r\n").isError must beTrue
    }
    "reject nobody@ scrupal dot org" in TestTypes(scrupal) { t: TestTypes ⇒
      EmailAddress_t.validate(t.vLoc, "nobody@ 24 dot com").isError must beTrue
    }
    "reject no body@scrupal.org" in TestTypes(scrupal) { t: TestTypes ⇒
      EmailAddress_t.validate(t.vLoc, "no body@scrupal.org").isError must beTrue
    }
  }

  "LegalName_t" should {
    "accept 'My Legal Name'" in TestTypes(scrupal) { t: TestTypes ⇒
      LegalName_t.validate(t.vLoc, "My Legal Name").isError must beFalse
    }
    "reject tab char" in TestTypes(scrupal) { t: TestTypes ⇒
      LegalName_t.validate(t.vLoc, "\t").isError must beTrue
    }
  }
}
