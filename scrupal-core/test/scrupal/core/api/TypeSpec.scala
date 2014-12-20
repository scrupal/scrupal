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

package scrupal.core.api

import reactivemongo.bson.Subtype.GenericBinarySubtype
import reactivemongo.bson._
import scrupal.core.types._
import scrupal.test.{ScrupalSpecification, FakeContext}
import scrupal.utils.Patterns._



/** Test specifications for the abstract Type system portion of the API.  */
class TypeSpec extends ScrupalSpecification("TypeSpec") {

  case class TestTypes() extends FakeContext[TestTypes] {
    /** The Scrupal Type for Uniform Resource Identifiers per http://tools.ietf.org/html/rfc3986 */
    object MiddlePeriod extends AnyType(sym("MiddlePeriod"), "A type for validating URI strings.") {
      override def validate(value: BSONValue) = single(value) {
        case v: BSONString => {
          val a = v.value.split('.')
          if (a.size > 2)
            Some("Too many periods")
          else if (a.size < 2)
            Some("Must have at least one period")
          else if (a(0).length != a(1).length)
            Some("Strings on each side of . must have same length")
          else
            None
        }
        case x: BSONValue => wrongClass("BSONString", x)
      }
    }

    object rangeTy extends RangeType(sym("aRange"), "Ten from 10", 10, 20)
    object realTy extends RealType(sym("aReal"), "Ten from 10", 10.1, 20.9)
    object enumTy extends EnumType(sym("enumTy"), "Enum example", Map(
      'one -> 1, 'two -> 2, 'three -> 3, 'four -> 5, 'five -> 8, 'six -> 13
    ))

    object blobTy extends BLOBType(sym("blobTy"), "Blob example", "application/binary", 4)
    object listTy extends ListType(sym("listTy"), "List example", enumTy)

    object setTy extends SetType(sym("setTy"), "Set example", rangeTy)

    object mapTy extends MapType(sym("mapTy"), "Map example", realTy)

    object emailTy extends StringType(sym("EmailAddress"), "An email address", anchored(EmailAddress), 253)

    object trait1 extends BundleType(sym("trait1"), "Trait example 1",
      fields = Map (
        "even" -> MiddlePeriod,
        "email" -> emailTy,
        "range" -> rangeTy,
        "real" -> realTy,
        "enum" -> enumTy
      )
    )

    object trait2 extends BundleType(sym("trait2"), "Trait example 2",
      fields = Map(
        "list" -> listTy,
        "set" -> setTy,
        "map" -> mapTy
      )
    )

    object AnEntity extends BundleType(sym("AnEntity"), "Entity example",
      fields = Map("trait1" -> trait1, "trait2" -> trait2)
    )

    val js1 = BSONDocument(
      "even" -> "foo.bar",
      "email" -> "somebody@example.com",
      "range" -> 17,
      "real" -> 17.0,
      "enum" -> "three"
    )

    val js2 = BSONDocument(
      "list" -> BSONArray("one", "three", "five"),
      "set" -> BSONArray(17, 18),
      "map" -> BSONDocument("foo" -> 17.0)
    )
  }



  "MiddlePeriod" should {
    "accept 'foo.bar'" in TestTypes() { t : TestTypes ⇒
      val result = t.MiddlePeriod.validate(BSONString("foo.bar"))
      result.isDefined must beFalse
    }
    "reject 'foo'" in TestTypes() { t: TestTypes ⇒
      t.MiddlePeriod.validate(BSONString("foo")).isDefined must beTrue
    }
    "reject 'foo.barbaz'" in TestTypes() { t: TestTypes ⇒
      t.MiddlePeriod.validate(BSONString("foo.barbaz")).isDefined must beTrue
    }
  }


  "RangeType(10,20)" should {
    "accept 17" in TestTypes() { t: TestTypes ⇒
      t.rangeTy.validate(BSONInteger(17)).isDefined must beFalse
    }
    "accept 10" in TestTypes() { t: TestTypes ⇒
      t.rangeTy.validate(BSONInteger(10)).isDefined must beFalse
    }
    "accept 20" in TestTypes() { t: TestTypes ⇒
      t.rangeTy.validate(BSONInteger(20)).isDefined must beFalse
    }
    "reject 9" in TestTypes() { t: TestTypes ⇒
      t.rangeTy.validate(BSONInteger(9)).isDefined must beTrue
    }
    "reject 21" in TestTypes() { t: TestTypes ⇒
      t.rangeTy.validate(BSONInteger(21)).isDefined must beTrue
    }
  }


  "RangeType(10.1,20.9)" should {
    "accept 17.0" in TestTypes() { t: TestTypes ⇒
      t.realTy.validate(BSONDouble(17.0)).isDefined must beFalse
    }
    "accept 10.2" in TestTypes() { t: TestTypes ⇒
      t.realTy.validate(BSONDouble(10.2)).isDefined must beFalse
    }
    "accept 20.8" in TestTypes() { t: TestTypes ⇒
      t.realTy.validate(BSONDouble(20.8)).isDefined must beFalse
    }
    "reject 10.01" in TestTypes() { t: TestTypes ⇒
      t.realTy.validate(BSONDouble(10.01)).isDefined must beTrue
    }
    "reject 20.99" in TestTypes() { t: TestTypes ⇒
      t.realTy.validate(BSONDouble(20.99)).isDefined must beTrue
    }
  }

  "EnumType(fibonacci)" should {
    "accept 'five'" in TestTypes() { t: TestTypes ⇒
      t.enumTy.validate(BSONString("five")).isDefined must beFalse
    }
    "reject 'seven'" in TestTypes() { t: TestTypes ⇒
      t.enumTy.validate(BSONString("seven")).isDefined must beTrue
    }
    "provide 13 for 'six' " in TestTypes() { t: TestTypes ⇒
      t.enumTy.valueOf("six").get must beEqualTo(13)
    }
  }

  "BLOBType(4)" should {
    "reject a string that is too long" in TestTypes() { t: TestTypes ⇒
      val url = BSONString("http://foo.com/bar/baz.bin")
      t.blobTy.validate(url).isDefined must beTrue
    }
    "accept a string that is short enough" in TestTypes() { t: TestTypes ⇒
      val url = BSONString("http")
      t.blobTy.validate(url).isDefined must beFalse
    }
    "accept BSONBinary" in TestTypes() { t: TestTypes ⇒
      val url = BSONBinary(Array[Byte](0,3,2,1), GenericBinarySubtype)
      t.blobTy.validate(url).isDefined must beFalse
    }
  }

  "ListType(enumTy)" should {
    "reject BSONArray(6,7)" in TestTypes() { t: TestTypes ⇒
      val js :BSONValue = BSONArray( 6, 7 )
      t.listTy.validate(js).isDefined must beTrue
    }
    "accept BSONArray('six')" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray("six")
      t.listTy.validate(js).isDefined must beFalse
    }
    "accept BSONArray()" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray()
      t.listTy.validate(js).isDefined must beFalse
    }
    "accept BSONArray(\"one\", \"three\", \"five\")" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray("one", "three", "five")
      t.listTy.validate(js).isDefined must beFalse
    }
    "reject BSONArray('nine')" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray("nine")
      t.listTy.validate(js).isDefined must beTrue
    }
  }

  "SetType(t.rangeTy)" should {
    "reject BSONArray(\"foo\")" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray(BSONString("foo"))
      t.setTy.validate(js).isDefined must beTrue
    }
    "accept BSONArray(17)" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray(BSONInteger(17))
      t.setTy.validate(js).isDefined must beFalse
    }
    "accept BSONArray(17,18)" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray(17, 18)
      t.setTy.validate(js).isDefined must beFalse
    }
    "accept BSONArray(17,17)" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray(17, 17)
      t.setTy.validate(js).isDefined must beFalse
    }
    "reject BSONArray(21)" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray(21)
      t.setTy.validate(js).isDefined must beTrue
    }
  }

  "MapType(realTy)" should {
    "reject JsObject('foo' -> 17)" in TestTypes() { t: TestTypes ⇒
      val js = BSONDocument("foo" -> 17L)
      t.mapTy.validate(js).isDefined must beFalse
    }
    "accept JsObject('foo' -> 17.0)" in TestTypes() { t: TestTypes ⇒
      val js = BSONDocument("foo" -> 17.0)
      t.mapTy.validate(js).isDefined must beFalse
    }
    "reject BSONArray('foo', 17.0)" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray("foo", 17.0)
      t.mapTy.validate(js).isDefined must beTrue
    }
  }

  "Complex Entity With Traits" should {
    "accept matching input" in TestTypes() { t: TestTypes ⇒
      val js = BSONDocument( "trait1" -> t.js1, "trait2" -> t.js2)
      val result = t.AnEntity.validate(js)
      result.isDefined must beFalse
    }
    "reject mismatched input" in TestTypes() { t: TestTypes ⇒
      val js = BSONDocument( "trait1" -> t.js2, "trait2" -> t.js1)
      val result = t.AnEntity.validate(js)
      result.isDefined must beTrue
    }
    "accept reversed input" in TestTypes() { t: TestTypes ⇒
      val js = BSONDocument( "trait2" -> t.js2, "trait1" -> t.js1)
      val result = t.AnEntity.validate(js)
      result.isDefined must beFalse
    }
  }

  "Identifier_t" should {
    "accept ***My-Funky.1d3nt1f13r###" in {
      Identifier_t.validate(BSONString("***My-Funky.1d3nt1f13r###")).isDefined must beFalse
    }
    "reject 'Not An Identifier'" in {
      Identifier_t.validate(BSONString("Not An Identifier ")).isDefined must beTrue
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

  "Types" should {
    "not spoof registration in a module via Type.moduleof" in TestTypes() { t: TestTypes ⇒
      val mod = t.AnEntity.moduleOf // Make sure AnEntity is referenced her lest it be garbage collected!
      mod.isDefined must beFalse
    }
  }
}
