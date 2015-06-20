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

import akka.http.scaladsl.model.MediaTypes
import scrupal.api.types._
import scrupal.test.{ScrupalApiSpecification, FakeContext}
import scrupal.utils.Patterns._
import scrupal.utils.Validation.{DefaultLocation, Location}

import scala.language.implicitConversions

/** Test specifications for the abstract Type system portion of the API.  */
class TypeSpec extends ScrupalApiSpecification("TypeSpec") {

  case class TestTypes() extends FakeContext[TestTypes]("TestTypes") {
    /** The Scrupal Type for Uniform Resource Identifiers per http://tools.ietf.org/html/rfc3986 */
    object MiddlePeriod extends AnyType(sym("MiddlePeriod"), "A type for validating URI strings.") {
      override def validate(ref: Location, value: Any) = simplify(ref, value, "String") {
        case v: String => {
          val a = v.split('.')
          if (a.size > 2)
            Some("Too many periods")
          else if (a.size < 2)
            Some("Must have at least one period")
          else if (a(0).length != a(1).length)
            Some("Strings on each side of . must have same length")
          else
            None
        }
        case _ => Some("")
      }
    }

    val vLoc = DefaultLocation

    object rangeTy extends RangeType(sym("aRange"), "Ten from 10", 10, 20)
    object realTy extends RealType(sym("aReal"), "Ten from 10", 10.1, 20.9)
    object enumTy extends EnumType(sym("enumTy"), "Enum example", Map(
      'one -> 1, 'two -> 2, 'three -> 3, 'four -> 5, 'five -> 8, 'six -> 13
    ))

    object blobTy extends BLOBType(sym("blobTy"), "Blob example", MediaTypes.`application/octet-stream`, 4)
    object listTy extends ListType(sym("listTy"), "List example", enumTy)

    object setTy extends SetType(sym("setTy"), "Set example", rangeTy)

    object mapTy extends MapType[RealType.ILDFS](sym("mapTy"), "Map example", Seq("foo"), realTy)

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

    val js1 = Map(
      "even" -> "foo.bar",
      "email" -> "somebody@example.com",
      "range" -> 17,
      "real" -> 17.0,
      "enum" -> "three"
    )

    val js2 = Map(
      "list" -> Seq("one", "three", "five"),
      "set" -> Seq(17, 18),
      "map" -> Map("foo" -> 17.0)
    )
  }



  "MiddlePeriod" should {
    "accept 'foo.bar'" in TestTypes() { t : TestTypes ⇒
      val result = t.MiddlePeriod.validate(t.vLoc, "foo.bar")
      result.isError must beFalse
    }
    "reject 'foo'" in TestTypes() { t: TestTypes ⇒
      t.MiddlePeriod.validate(t.vLoc, "foo").isError must beTrue
    }
    "reject 'foo.barbaz'" in TestTypes() { t: TestTypes ⇒
      t.MiddlePeriod.validate(t.vLoc, "foo.barbaz").isError must beTrue
    }
  }


  "RangeType(10,20)" should {
    import RangeType._
    "accept 17" in TestTypes() { t: TestTypes ⇒
      t.rangeTy.validate(t.vLoc, 17).isError must beFalse
    }
    "accept 10" in TestTypes() { t: TestTypes ⇒
      t.rangeTy.validate(t.vLoc, 10).isError must beFalse
    }
    "accept 20" in TestTypes() { t: TestTypes ⇒
      t.rangeTy.validate(t.vLoc, 20).isError must beFalse
    }
    "reject 9" in TestTypes() { t: TestTypes ⇒
      t.rangeTy.validate(t.vLoc, 9).isError must beTrue
    }
    "reject 21" in TestTypes() { t: TestTypes ⇒
      t.rangeTy.validate(t.vLoc, 21).isError must beTrue
    }
  }


  "RealType(10.1,20.9)" should {
    import RealType._
    "accept 17.0" in TestTypes() { t: TestTypes ⇒
      t.realTy.validate(t.vLoc, 17.0).isError must beFalse
    }
    "accept 10.2" in TestTypes() { t: TestTypes ⇒
      t.realTy.validate(t.vLoc, 10.2).isError must beFalse
    }
    "accept 20.8" in TestTypes() { t: TestTypes ⇒
      t.realTy.validate(t.vLoc, 20.8).isError must beFalse
    }
    "reject 10.01" in TestTypes() { t: TestTypes ⇒
      t.realTy.validate(t.vLoc, 10.01).isError must beTrue
    }
    "reject 20.99" in TestTypes() { t: TestTypes ⇒
      t.realTy.validate(t.vLoc, 20.99).isError must beTrue
    }
  }

  import EnumType._

  "EnumType(fibonacci)" should {
    import EnumType._
    "accept 'five'" in TestTypes() { t: TestTypes ⇒
      t.enumTy.validate(t.vLoc, "five").isError must beFalse
    }
    "reject 'seven'" in TestTypes() { t: TestTypes ⇒
      t.enumTy.validate(t.vLoc, "seven").isError must beTrue
    }
    "provide 13 for 'six' " in TestTypes() { t: TestTypes ⇒
      t.enumTy.valueOf("six").get must beEqualTo(13)
    }
  }

  "BLOBType(4)" should {
    "reject a string that is too long" in TestTypes() { t: TestTypes ⇒
      val url = "http://foo.com/bar/baz.bin"
      t.blobTy.validate(t.vLoc, url.getBytes(utf8)).isError must beTrue
    }
    "accept a string that is short enough" in TestTypes() { t: TestTypes ⇒
      val url = "http"
      t.blobTy.validate(t.vLoc, url.getBytes(utf8)).isError must beFalse
    }
    "accept BSONBinary" in TestTypes() { t: TestTypes ⇒
      val url = Array[Byte](0,3,2,1)
      t.blobTy.validate(t.vLoc, url).isError must beFalse
    }
  }

  "ListType(enumTy)" should {
    import EnumType._
    "reject Seq(6,7)" in TestTypes() { t: TestTypes ⇒
      val js = Seq( 6, 7 )
      t.listTy.validate(t.vLoc, js).isError must beTrue
    }
    "accept Seq('six')" in TestTypes() { t: TestTypes ⇒
      val js = Seq("six")
      t.listTy.validate(t.vLoc, js).isError must beFalse
    }
    "accept Seq()" in TestTypes() { t: TestTypes ⇒
      val js = Seq()
      t.listTy.validate(t.vLoc, js).isError must beFalse
    }
    "accept Seq(\"one\", \"three\", \"five\")" in TestTypes() { t: TestTypes ⇒
      val js = Seq("one", "three", "five")
      t.listTy.validate(t.vLoc, js).isError must beFalse
    }
    "reject BSONArray('nine')" in TestTypes() { t: TestTypes ⇒
      val js = Seq("nine")
      t.listTy.validate(t.vLoc, js).isError must beTrue
    }
  }

  "SetType(t.rangeTy)" should {
    import RangeType._
    "reject Set(\"foo\")" in TestTypes() { t: TestTypes ⇒
      val js = Set("foo")
      t.setTy.validate(t.vLoc, js).isError must beTrue
    }
    "accept Set(17)" in TestTypes() { t: TestTypes ⇒
      val js = Set(17)
      t.setTy.validate(t.vLoc, js).isError must beFalse
    }
    "accept Set(17,18)" in TestTypes() { t: TestTypes ⇒
      val js = Set(17, 18)
      t.setTy.validate(t.vLoc, js).isError must beFalse
    }
    "accept Set(17,17)" in TestTypes() { t: TestTypes ⇒
      val js = Set(17, 17)
      val result = t.setTy.validate(t.vLoc, js)
      result.isError must beTrue
      result.message.toString must contain("non-distinct")
    }
    "reject Set(21)" in TestTypes() { t: TestTypes ⇒
      val js = Set(21)
      t.setTy.validate(t.vLoc, js).isError must beTrue
    }
  }

  "MapType(realTy)" should {
    import RealType._
    "reject Map('foo' -> 17)" in TestTypes() { t: TestTypes ⇒
      val js = Map("foo" -> 7.0)
      t.mapTy.validate(t.vLoc, js).isError must beFalse
    }
    "accept Map('foo' -> 17.0)" in TestTypes() { t: TestTypes ⇒
      val js = Map("foo" -> 17.0)
      t.mapTy.validate(t.vLoc, js).isError must beFalse
    }
  }

  /* FIXME: Reinstate Complex Entity With Traits test case
  "Complex Entity With Traits" should {
    "accept matching input" in TestTypes() { t: TestTypes ⇒
      val js = Map( "trait1" -> t.js1, "trait2" -> t.js2)
      val result = t.AnEntity.validate(t.vLoc, js)
      result.isError must beFalse
    }
    "reject mismatched input" in TestTypes() { t: TestTypes ⇒
      val js = Map( "trait1" -> t.js2, "trait2" -> t.js1)
      val result = t.AnEntity.validate(t.vLoc, js)
      result.isError must beTrue
    }
    "accept reversed input" in TestTypes() { t: TestTypes ⇒
      val js = Map( "trait2" -> t.js2, "trait1" -> t.js1)
      val result = t.AnEntity.validate(t.vLoc, js)
      result.isError must beFalse
    }
  }
  */

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
    import RangeType._
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

  "Types" should {
    "not spoof registration in a module via Type.moduleof" in TestTypes() { t: TestTypes ⇒
      val mod = t.AnEntity.moduleOf // Make sure AnEntity is referenced her lest it be garbage collected!
      mod.isDefined must beFalse
    }
  }
}
