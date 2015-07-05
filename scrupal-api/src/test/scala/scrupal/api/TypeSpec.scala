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

import java.time.Instant

import org.specs2.execute.Result
import scrupal.test.{TestTypes, ScrupalSpecification}
import scrupal.utils.Validation
import scrupal.utils.Validation.SimpleLocation

import scala.concurrent.duration.Duration
import scala.language.implicitConversions

/** Test specifications for the abstract Type system portion of the API.  */
class TypeSpec extends ScrupalSpecification("TypeSpec") {

  def atomGauntlet(t : Type[Atom], values: List[Atom], results:List[Boolean]) : Result ={
    val location = SimpleLocation(t.getClass.getSimpleName)
    val zipped : List[(Atom,Boolean)] = values.zipAll(results,atomFromInt(0), false)
    for ((v,e) ← zipped ) {
      val result = t.validate(location, v)
      if (result.isError == e)
        if (result.isError)
          failure(result.message)
        else
          failure(s"Validation of $v should have failed but didn't")
    }
    success
  }

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
    "run atomGauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(t.rangeTy,
        List[Atom](true, 7.toByte, 20, 8L, 17.toShort, 20.0F, 18.0D, Instant.now(), Duration(10,"seconds")),
        List(false, false, true, false, true, true, true, false, false))
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
    "run atomGauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(t.realTy,
        List[Atom](true, 7.toByte, 20, 8L, 17.toShort, 20.0F, 18.0D, Instant.now(), Duration(5,"seconds")),
        List(false, false, true, false, true, true, true, false))
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
    "reject Instant and Duration values" in TestTypes(scrupal) { t: TestTypes ⇒
      t.enumTy.validate(t.vLoc, Instant.now()).isError must beTrue
    }
    "reject Instant and Duration values" in TestTypes(scrupal) { t: TestTypes ⇒
      t.enumTy.validate(t.vLoc, Duration(2,"seconds")).isError must beTrue
    }

    "run atomGauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(t.enumTy,
        List[Atom](true, 7.toByte, 20, 8L, 13.toShort, 20.0F, 18.0D),
        List(true, false, false, true, true, false, false))
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

  "AnyType_t" should {
    "accept pretty much anything" in TestTypes(scrupal) { t: TestTypes ⇒
      AnyType_t.validate(t.vLoc, 42).isError must beFalse
      AnyType_t.validate(t.vLoc, 42.0F).isError must beFalse
      AnyType_t.validate(t.vLoc, 42.0D).isError must beFalse
      AnyType_t.validate(t.vLoc, 42L).isError must beFalse
      AnyType_t.validate(t.vLoc, 42.toShort).isError must beFalse
      AnyType_t.validate(t.vLoc, 42.toByte).isError must beFalse
      AnyType_t.validate(t.vLoc, true).isError must beFalse
      AnyType_t.validate(t.vLoc, "42.0").isError must beFalse
      AnyType_t.validate(t.vLoc, Instant.now()).isError must beFalse
    }
  }

  "AnyString_t" should {
    "accept some Atom types" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(AnyString_t,
        List[Atom](true, 42, 42.0F, 42.0D, 42L, 42.toShort, 42.toByte, true, "42.0",
          Instant.now(), Duration(2, "seconds"), Symbol("42")),
        List(true, true, true, true, true, true, true, true, true, true, true, true)
      )
    }
  }

  "AnyInteger_t" should {
    "accept 42" in TestTypes(scrupal) { t: TestTypes ⇒
      AnyInteger_t.validate(t.vLoc, 42).isError must beFalse
    }
    "reject non-numeric string" in TestTypes(scrupal) { t: TestTypes ⇒
      AnyInteger_t.validate(t.vLoc, "Hello, World!").isError must beTrue
    }
    "run the atom gauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(AnyInteger_t,
        List[Atom](true, 42, 42.0F, 42.0D, 42L, 42.toShort, 42.toByte, true, "42",
          Instant.ofEpochMilli(42), Duration(2,"seconds"), Symbol("42")),
        List(true, true, true, true, true, true, true, true, true, true, true, true)
      )
    }
  }

  "AnyReal_t" should {
    "accept 42.0" in TestTypes(scrupal) { t: TestTypes ⇒
      AnyReal_t.validate(t.vLoc, 42.0D).isError must beFalse
      AnyReal_t.validate(t.vLoc, 42.0F).isError must beFalse
    }
    "reject non-numeric string" in TestTypes(scrupal) { t: TestTypes ⇒
      AnyReal_t.validate(t.vLoc, "Hello, World!").isError must beTrue
    }
    "run the atom gauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(AnyReal_t,
        List[Atom](true, 42, 42.0F, 42.0D, 42L, 42.toShort, 42.toByte, true, "42.0",
          Instant.now(), Duration(2,"seconds"), Symbol("42")),
        List(true, true, true, true, true, true, true, true, true, true, true, true)
      )
    }
  }

  "AnyTimestamp_t" should {
    "accept Instant and Long" in TestTypes(scrupal) { t: TestTypes ⇒
      AnyTimestamp_t.validate(t.vLoc, Instant.now()).isError must beFalse
      AnyTimestamp_t.validate(t.vLoc, 141231512L).isError must beFalse
    }
    "run the atom gauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(AnyTimestamp_t,
        List[Atom](true, 42, 42.0F, 42.0D, 42L, 42.toShort, 42.toByte, false, "42",
          Instant.now(), Duration(2,"seconds"), "42.0", Symbol("42")),
        List(true, true, true, true, true, true, true, true, true, true, true, false, true)
      )
    }
  }

  "Boolean_t" should {
    "accept verity and falsity words" in TestTypes(scrupal) { t: TestTypes ⇒
      for (word ← BooleanType.verity ++ BooleanType.falsity) {
        Boolean_t.validate(t.vLoc, word).isError must beFalse
      }
      success
    }
    "accept Boolean true/false values" in TestTypes(scrupal) { t: TestTypes ⇒
      Boolean_t.validate(t.vLoc, true).isError must beFalse
      Boolean_t.validate(t.vLoc, false).isError must beFalse
    }
    "run the atom gauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(Boolean_t,
        List[Atom](false, 1, 1.0F, 1.0D, 1L, 1.toShort, 1.toByte, true, "off",
          Instant.now(), Duration(2,"seconds"), Symbol("42")),
        List(true, true, true, true, true, true, true, true, true, true, true, true)
      )
    }
  }

  "NonEmptyString_t" should {
    "accept a non-empty string" in TestTypes(scrupal) { t: TestTypes ⇒
      NonEmptyString_t.validate(t.vLoc, "not empty").isError must beFalse
    }
    "reject an empty string" in TestTypes(scrupal) { t: TestTypes ⇒
      NonEmptyString_t.validate(t.vLoc, "").isError must beTrue
    }
    "run the atom gauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(NonEmptyString_t,
        List[Atom](42, 42.0F, 42.0D, 42L, 42.toShort, 42.toByte, true, "42.0",
          Instant.now(), Duration(2,"seconds"), Symbol("42")),
        List(true, true, true, true, true, true, true, true, true, true, true)
      )
    }
  }

  "Password_t" should {
    "accept a valid password" in TestTypes(scrupal) { t: TestTypes ⇒
      Password_t.validate(t.vLoc, "ABC123!@#").isError must beFalse
    }
    "reject an invalid password" in TestTypes(scrupal) { t: TestTypes ⇒
      Password_t.validate(t.vLoc, "short").isError must beTrue
    }
    "run the atom gauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(Password_t,
        List[Atom](424242, 424242.0F, 424242.0D, 424242L, "42.0000",
          Instant.now(), Duration(2,"seconds"), Symbol("42")),
        List(true, false, false, true, false, true, false, false)
      )
    }
  }

  "Description_t" should {
    "accept" in TestTypes(scrupal) { t: TestTypes ⇒
      Description_t.validate(t.vLoc, "A Long Enough Description").isError must beFalse
    }
    "reject" in TestTypes(scrupal) { t: TestTypes ⇒
      Description_t.validate(t.vLoc, "Too short").isError must beTrue
    }
    "run the atom gauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(Description_t,
        List[Atom](42, 42.0F, 42.0D, 42L, 42.toShort, 42.toByte, true, "42.0000000",
          Instant.now(), Duration(2,"seconds"), Symbol("42")),
        List(false, false, false, false, false, false, false, true, true, false, false)
      )
    }
  }

  "Markdown_t" should {
    "accept '# Title'" in TestTypes(scrupal) { t: TestTypes ⇒
      Markdown_t.validate(t.vLoc, "# Title").isError must beFalse
    }
    "reject empty string" in TestTypes(scrupal) { t: TestTypes ⇒
      Markdown_t.validate(t.vLoc, "").isError must beTrue
    }
    "run the atom gauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(Markdown_t,
        List[Atom](42, 42.0F, 42.0D, 42L, 42.toShort, 42.toByte, true, "# Title",
          Instant.now(), Duration(2,"seconds"), Symbol("42")),
        List(true, false, false, true, true, true, true, true, true, true, true)
      )
    }
  }

  "DomainName_t" should {
    "accept scrupal.org" in TestTypes(scrupal) { t: TestTypes ⇒
      DomainName_t.validate(t.vLoc, "scrupal.org").isError must beFalse
    }
    "reject ###.999" in TestTypes(scrupal) { t: TestTypes ⇒
      DomainName_t.validate(t.vLoc, "###.999").isError must beTrue
    }
    "reject ab" in TestTypes(scrupal) { t: TestTypes ⇒
      DomainName_t.validate(t.vLoc, "ab").isError must beTrue
    }
    "run the atom gauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(DomainName_t,
        List[Atom](420, 420L, 420.toShort, 126.toByte, true, "42.com", "42.0",
                   Instant.now(), Duration(2,"seconds"), Symbol("42")),
        List(true, true, true, true, true, true, false, true, true, false)
      )
    }
  }

  "URI_t" should {
    "accept http://user:pw@scrupal.org/path?q=where#extra" in TestTypes(scrupal) { t: TestTypes ⇒
      URL_t.validate(t.vLoc, "http://user:pw@scrupal.org/path?q=where#extra").isError must beFalse
    }
    "reject Not\\A@URI" in TestTypes(scrupal) { t: TestTypes ⇒
      URL_t.validate(t.vLoc, "Not\\A@URI").isError must beTrue
    }
    "run the atom gauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(URL_t,
        List[Atom](42, 42.0F, 42.0D, 42L, 42.toShort, 42.toByte, true, "42.0",
                   Instant.now(), Duration(2,"seconds"), Symbol("42")),
        List(false, false, false, false, false, false, false, false, false, false, false)
      )
    }
  }

  "IPv4Address_t" should {
    "accept 1.2.3.4" in TestTypes(scrupal) { t: TestTypes ⇒
      IPv4Address_t.validate(t.vLoc, "1.2.3.4").isError must beFalse
    }
    "reject 1.2.3.400" in TestTypes(scrupal) { t: TestTypes ⇒
      IPv4Address_t.validate(t.vLoc, "1.2.3.400").isError must beTrue
    }
    "run the atom gauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(IPv4Address_t,
        List[Atom](42, 42.0F, 42.0D, 42L, 42.toShort, 42.toByte, true, "42.0",
                   Instant.now(), Duration(2,"seconds"), Symbol("42")),
        List(false, false, false, false, false, false, false, false, false, false, false, false)
      )
    }
  }

  "TcpPort_t" should {
    "accept 8088" in TestTypes(scrupal) { t: TestTypes ⇒
      TcpPort_t.validate(t.vLoc, 8088).isError must beFalse
    }
    "reject 65537" in TestTypes(scrupal) { t: TestTypes ⇒
      TcpPort_t.validate(t.vLoc, "65537").isError must beTrue
    }
    "run the atom gauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(TcpPort_t,
        List[Atom](42, 42.0F, 42.0D, 42L, 42.toShort, 42.toByte, true, "42.0", "42",
                   Instant.now(), Duration(2,"seconds"), Symbol("42")),
        List(true, true, true, true, true, true, true, false, true, false, true, true)
      )
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
    "run the atom gauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(EmailAddress_t,
        List[Atom](4242421, 42.42422F, 42.42423D, 4242424L, 42.toShort, 42.toByte, true, "42.0", "enough@long",
                   Instant.now(), Duration(2,"seconds"), Symbol("42")),
        List(false, false, false, false, false, false, false, false, true, false, false, false )
      )
    }
  }

  "LegalName_t" should {
    "accept 'My Legal Name'" in TestTypes(scrupal) { t: TestTypes ⇒
      LegalName_t.validate(t.vLoc, "My Legal Name").isError must beFalse
    }
    "reject tab char" in TestTypes(scrupal) { t: TestTypes ⇒
      LegalName_t.validate(t.vLoc, "\t").isError must beTrue
    }
    "run the atom gauntlet" in TestTypes(scrupal) { t: TestTypes ⇒
      atomGauntlet(LegalName_t,
        List[Atom](42, 42.0F, 42.0D, 42L, 42.toShort, 42.toByte, true, "42.0",
                   Instant.now(), Duration(2,"seconds"), Symbol("42")),
        List(true, true, true, true, true, true, true, true, true, true, true)
      )
    }
  }
}
