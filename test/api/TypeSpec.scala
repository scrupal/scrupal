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

package scrupal.api

import org.specs2.mutable.Specification
import play.api.libs.json._
import play.api.libs.json.JsSuccess
import play.api.Logger
import scala.collection.immutable.HashMap
import scrupal.models.{Patterns, EmailAddress_t}

/** Test specifications for the abstract Type system portion of the API.  */
class TypeSpec extends Specification {

  /** The Scrupal Type for Uniform Resource Identifiers per http://tools.ietf.org/html/rfc3986 */
  object MiddlePeriod extends Type('URI, "A type for validating URI strings.") {
    override def validate(value: JsValue) : JsResult[Boolean]= {
      value match {
        case v: JsString => {
          val a = v.value.split('.')
          if (a.size > 2)
            JsError("Too many periods")
          else if (a.size < 2)
            JsError("Must have at least one period")
          else if (a(0).length != a(1).length)
            JsError("Strings on each side of . must have same length")
          else
            JsSuccess(true)
        }
        case x => JsError("Expecting to validate against a string, not " + x.getClass().getSimpleName())
      }
    }
  }

  "MiddlePeriod" should {
    "accept 'foo.bar'" in {
      val result = MiddlePeriod.validate(JsString("foo.bar"))
      result.asOpt.isDefined must beTrue
    }
    "reject 'foo'" in {
      MiddlePeriod.validate(JsString("foo")).asOpt.isDefined must beFalse
    }
    "reject 'foo.barbaz'" in {
      MiddlePeriod.validate(JsString("foo.barbaz")).asOpt.isDefined must beFalse
    }
  }

  val rangeTy = RangeType('tenerval, "Ten from 10", 10, 20)

  "RangeType(10,20)" should {
    "accept 17" in {
      rangeTy.validate(JsNumber(17)).asOpt.isDefined must beTrue
    }
    "accept 10" in {
      rangeTy.validate(JsNumber(10)).asOpt.isDefined must beTrue
    }
    "accept 20" in {
      rangeTy.validate(JsNumber(20)).asOpt.isDefined must beTrue
    }
    "reject 9" in {
      rangeTy.validate(JsNumber(9)).asOpt.isDefined must beFalse
    }
    "reject 21" in {
      rangeTy.validate(JsNumber(21)).asOpt.isDefined must beFalse
    }
  }

  val realTy = RealType('tenerval, "Ten from 10", 10.1, 20.9)

  "RangeType(10.1,20.9)" should {
    "accept 17.0" in {
      realTy.validate(JsNumber(17.0)).asOpt.isDefined must beTrue
    }
    "accept 10.2" in {
      realTy.validate(JsNumber(10.2)).asOpt.isDefined must beTrue
    }
    "accept 20.8" in {
      realTy.validate(JsNumber(20.8)).asOpt.isDefined must beTrue
    }
    "reject 10.01" in {
      realTy.validate(JsNumber(10.01)).asOpt.isDefined must beFalse
    }
    "reject 20.99" in {
      realTy.validate(JsNumber(20.99)).asOpt.isDefined must beFalse
    }
  }

  val enumTy = EnumType('enum, "Enum example", HashMap(
    "one" -> 1, "two" -> 2, "three" -> 3, "four" -> 5, "five" -> 8, "six" -> 13
  ))

  "EnumType(fibonacci)" should {
    "accept 'five'" in {
      enumTy.validate(JsString("five")).asOpt.isDefined must beTrue
    }
    "reject 'seven'" in {
      enumTy.validate(JsString("seven")).asOpt.isDefined must beFalse
    }
    "provide 13 for 'six' " in {
      enumTy.valueOf("six").get must beEqualTo(13)
    }
  }

  val blobTy = BLOBType('blob, "Blob example", "application/binary", 2, 4)

  "BLOBType(2,4)" should {
    "accept valid URI" in {
      val url = JsString("http://foo.com/bar/baz.bin")
      blobTy.validate(url).asOpt.isDefined must beTrue
    }
    "reject invalid URI" in {
      val url = JsString("http:")
      blobTy.validate(url).asOpt.isDefined must beFalse
    }
  }

  val listTy = ListType('list, "List example", enumTy)

  "ListType(enumTy)" should {
    "reject JsArray(JsNumber)" in {
      val js = Json.arr( 3, 2 )
      listTy.validate(js).asOpt.isDefined must beFalse
    }
    "accept JsArray('six')" in {
      val js = Json.arr("six")
      listTy.validate(js).asOpt.isDefined must beTrue
    }
    "accept JsArray()" in {
      val js = Json.arr()
      listTy.validate(js).asOpt.isDefined must beTrue
    }
    "accept JsArray(\"one\", \"three\", \"five\")" in {
      val js = Json.arr("one", "three", "five")
      listTy.validate(js).asOpt.isDefined must beTrue
    }
    "reject JsArray('nine')" in {
      val js = Json.arr("nine")
      listTy.validate(js).asOpt.isDefined must beFalse
    }
  }

  val setTy = SetType('set, "Set example", rangeTy)

  "SetType(rangeTy)" should {
    "reject JsArray(\"foo\")" in {
      val js = Json.arr(JsString("foo"))
      setTy.validate(js).asOpt.isDefined must beFalse
    }
    "accept JsArray(17)" in {
      val js = Json.arr(JsNumber(17))
      setTy.validate(js).asOpt.isDefined must beTrue
    }
    "accept JsArray(17,18)" in {
      val js = Json.arr(17, 18)
      setTy.validate(js).asOpt.isDefined must beTrue
    }
    "reject JsArray(17,17)" in {
      val js = Json.arr(17, 17)
      setTy.validate(js).asOpt.isDefined must beFalse
    }
    "reject JsArray(21)" in {
      val js = Json.arr(21)
      setTy.validate(js).asOpt.isDefined must beFalse
    }
  }

  val mapTy = MapType('map, "Map example", realTy)

  "MapType(realTy)" should {
    "reject JsObject('foo' -> 17)" in {
      val js = Json.obj("foo" -> 17L)
      mapTy.validate(js).asOpt.isDefined must beTrue
    }
    "accept JsObject('foo' -> 17.0)" in {
      val js = Json.obj("foo" -> 17.0)
      mapTy.validate(js).asOpt.isDefined must beTrue
    }
    "reject JsArray('foo', 17.0)" in {
      val js = Json.arr("foo", 17.0)
      mapTy.validate(js).asOpt.isDefined must beFalse
    }
  }

  object trait1 extends TraitType('trait1, "Trait example 1",
    fields = HashMap (
      'even -> MiddlePeriod,
      'email -> EmailAddress_t,
      'range -> rangeTy,
      'real -> realTy,
      'enum -> enumTy
    ),
    actions = HashMap()
  )

  object trait2 extends TraitType('trait2, "Trait example 2",
    fields = HashMap(
      'list -> listTy,
      'set -> setTy,
      'map -> mapTy
    ),
    actions = HashMap()
  )

  object AnEntity extends BundleType('entity, "Entity example",
    traits = HashMap('trait1 -> trait1, 'trait2 -> trait2),
    actions = HashMap()
  )

  val js1 = Json.obj(
    "even" -> "foo.bar",
    "email" -> "somebody@example.com",
    "range" -> 17,
    "real" -> 17.0,
    "enum" -> "three"
  )

  val js2 = Json.obj(
    "list" -> Json.arr("one", "three", "five"),
    "set" -> Json.arr(17, 18),
    "map" -> Json.obj("foo" -> 17.0)
  )

  "Complex Entity With Traits" should {
    "accept matching input" in {
      val js = Json.obj( "trait1" -> js1, "trait2" -> js2)
      AnEntity.validate(js).asOpt.isDefined must beTrue
    }
    "reject mismatched input" in {
      val js = Json.obj( "trait1" -> js2, "trait2" -> js1)
      AnEntity.validate(js).asOpt.isDefined must beFalse
    }
    "accept reversed input" in {
      val js = Json.obj( "trait2" -> js2, "trait1" -> js1)
      AnEntity.validate(js).asOpt.isDefined must beTrue
    }
  }
}
