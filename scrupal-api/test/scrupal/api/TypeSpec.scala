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

package scrupal.api

import org.specs2.mutable.Specification
import reactivemongo.bson.Subtype.GenericBinarySubtype
import reactivemongo.bson._
import scrupal.test.FakeModule

/** Test specifications for the abstract Type system portion of the API.  */
class TypeSpec extends Specification {

  val db = "test-types"

  object TestModule extends FakeModule('TestModule, db)

  /** The Scrupal Type for Uniform Resource Identifiers per http://tools.ietf.org/html/rfc3986 */
  object MiddlePeriod extends AnyType('MiddlePeriod, "A type for validating URI strings.") {
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

  "MiddlePeriod" should {
    "accept 'foo.bar'" in {
      val result = MiddlePeriod.validate(BSONString("foo.bar"))
      result.isDefined must beFalse
    }
    "reject 'foo'" in {
      MiddlePeriod.validate(BSONString("foo")).isDefined must beTrue
    }
    "reject 'foo.barbaz'" in {
      MiddlePeriod.validate(BSONString("foo.barbaz")).isDefined must beTrue
    }
  }

  object rangeTy extends RangeType('aRange, "Ten from 10", 10, 20)

  "RangeType(10,20)" should {
    "accept 17" in {
      rangeTy.validate(BSONInteger(17)).isDefined must beFalse
    }
    "accept 10" in {
      rangeTy.validate(BSONInteger(10)).isDefined must beFalse
    }
    "accept 20" in {
      rangeTy.validate(BSONInteger(20)).isDefined must beFalse
    }
    "reject 9" in {
      rangeTy.validate(BSONInteger(9)).isDefined must beTrue
    }
    "reject 21" in {
      rangeTy.validate(BSONInteger(21)).isDefined must beTrue
    }
  }

  object realTy extends RealType('aReal, "Ten from 10", 10.1, 20.9)

  "RangeType(10.1,20.9)" should {
    "accept 17.0" in {
      realTy.validate(BSONDouble(17.0)).isDefined must beFalse
    }
    "accept 10.2" in {
      realTy.validate(BSONDouble(10.2)).isDefined must beFalse
    }
    "accept 20.8" in {
      realTy.validate(BSONDouble(20.8)).isDefined must beFalse
    }
    "reject 10.01" in {
      realTy.validate(BSONDouble(10.01)).isDefined must beTrue
    }
    "reject 20.99" in {
      realTy.validate(BSONDouble(20.99)).isDefined must beTrue
    }
  }

  object enumTy extends EnumType('enumTy, "Enum example", Map(
    'one -> 1, 'two -> 2, 'three -> 3, 'four -> 5, 'five -> 8, 'six -> 13
  ))

  "EnumType(fibonacci)" should {
    "accept 'five'" in {
      enumTy.validate(BSONString("five")).isDefined must beFalse
    }
    "reject 'seven'" in {
      enumTy.validate(BSONString("seven")).isDefined must beTrue
    }
    "provide 13 for 'six' " in {
      enumTy.valueOf("six").get must beEqualTo(13)
    }
  }

  object blobTy extends BLOBType('blobTy, "Blob example", "application/binary", 4)

  "BLOBType(4)" should {
    "reject a string that is too long" in {
      val url = BSONString("http://foo.com/bar/baz.bin")
      blobTy.validate(url).isDefined must beTrue
    }
    "accept a string that is short enough" in {
      val url = BSONString("http")
      blobTy.validate(url).isDefined must beFalse
    }
    "accept BSONBinary" in {
      val url = BSONBinary(Array[Byte](0,3,2,1), GenericBinarySubtype)
      blobTy.validate(url).isDefined must beFalse
    }
  }

  object listTy extends ListType('listTy, "List example", enumTy)

  "ListType(enumTy)" should {
    "reject BSONArray(6,7)" in {
      val js :BSONValue = BSONArray( 6, 7 )
      listTy.validate(js).isDefined must beTrue
    }
    "accept BSONArray('six')" in {
      val js = BSONArray("six")
      listTy.validate(js).isDefined must beFalse
    }
    "accept BSONArray()" in {
      val js = BSONArray()
      listTy.validate(js).isDefined must beFalse
    }
    "accept BSONArray(\"one\", \"three\", \"five\")" in {
      val js = BSONArray("one", "three", "five")
      listTy.validate(js).isDefined must beFalse
    }
    "reject BSONArray('nine')" in {
      val js = BSONArray("nine")
      listTy.validate(js).isDefined must beTrue
    }
  }

  object setTy extends SetType('setTy, "Set example", rangeTy)

  "SetType(rangeTy)" should {
    "reject BSONArray(\"foo\")" in {
      val js = BSONArray(BSONString("foo"))
      setTy.validate(js).isDefined must beTrue
    }
    "accept BSONArray(17)" in {
      val js = BSONArray(BSONInteger(17))
      setTy.validate(js).isDefined must beFalse
    }
    "accept BSONArray(17,18)" in {
      val js = BSONArray(17, 18)
      setTy.validate(js).isDefined must beFalse
    }
    "accept BSONArray(17,17)" in {
      val js = BSONArray(17, 17)
      setTy.validate(js).isDefined must beFalse
    }
    "reject BSONArray(21)" in {
      val js = BSONArray(21)
      setTy.validate(js).isDefined must beTrue
    }
  }

  object mapTy extends MapType('mapTy, "Map example", realTy)

  "MapType(realTy)" should {
    "reject JsObject('foo' -> 17)" in {
      val js = BSONDocument("foo" -> 17L)
      mapTy.validate(js).isDefined must beFalse
    }
    "accept JsObject('foo' -> 17.0)" in {
      val js = BSONDocument("foo" -> 17.0)
      mapTy.validate(js).isDefined must beFalse
    }
    "reject BSONArray('foo', 17.0)" in {
      val js = BSONArray("foo", 17.0)
      mapTy.validate(js).isDefined must beTrue
    }
  }

  object trait1 extends BundleType('trait1, "Trait example 1",
    fields = Map (
      "even" -> MiddlePeriod,
      "email" -> mapTy,
      "range" -> rangeTy,
      "real" -> realTy,
      "enum" -> enumTy
    )
  )

  object trait2 extends BundleType('trait2, "Trait example 2",
    fields = Map(
      "list" -> listTy,
      "set" -> setTy,
      "map" -> mapTy
    )
  )

  object AnEntity extends BundleType('AnEntity, "Entity example",
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

  "Complex Entity With Traits" should {
    "accept matching input" in {
      val js = BSONDocument( "trait1" -> js1, "trait2" -> js2)
      val result = AnEntity.validate(js)
      result.isDefined must beFalse
    }
    "reject mismatched input" in {
      val js = BSONDocument( "trait1" -> js2, "trait2" -> js1)
      val result = AnEntity.validate(js)
      result.isDefined must beTrue
    }
    "accept reversed input" in {
      val js = BSONDocument( "trait2" -> js2, "trait1" -> js1)
      val result = AnEntity.validate(js)
      result.isDefined must beFalse
    }
  }

  "Types" should {
    "not spoof registration in a module via Type.moduleof" in {
      val mod = AnEntity.moduleOf // Make sure AnEntity is referenced her lest it be garbage collected!
      mod.isDefined must beFalse
    }
  }
}
