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

package scrupal.store.reactivemongo

import java.util.Date

import org.joda.time.DateTime
import org.specs2.mutable.Specification

import reactivemongo.bson._
import scrupal.utils.ScrupalComponent

import scala.util.matching.Regex

/** Test case for
 * Created by reidspencer on 10/28/14.
 */
class BSONValueBuilderSpec extends Specification with ScrupalComponent {

  import BSONValueBuilder._

  "BSONValueBuilderSpec" should {
    "construct simple BSONValues correctly" in {
      $empty.isEmpty must beTrue
      $string("foo").value must beEqualTo("foo")
      $string("bar") must beAnInstanceOf[BSONString]
      $double(42.0D).value must beEqualTo(42.0D)
      $double(42.0) must beAnInstanceOf[BSONDouble]
      $int(42).value must beEqualTo(42)
      $int(42) must beAnInstanceOf[BSONInteger]
      $long(42L).value must beEqualTo(42L)
      $long(42L) must beAnInstanceOf[BSONLong]
      $boolean(true).value must beTrue
      $undefined.isInstanceOf[BSONUndefined.type] must beTrue
      $null.isInstanceOf[BSONNull.type] must beTrue
    }

    "construct $document correctly" in {
      val doc = $document("foo" -> BSONInteger(42))
      doc.get("foo").isDefined must beTrue
      doc.get("foo").get must beAnInstanceOf[BSONInteger]
      doc.get("foo").get.asInstanceOf[BSONInteger].value must beEqualTo(42)
    }

    "construct $array forms correctly" in {
      val array = $array($int(42), $double(42.0), $string("42.0"))
      array.get(2).isDefined must beTrue
      array.get(2).get must beAnInstanceOf[BSONString]
      array.get(2).get.asInstanceOf[BSONString].value must beEqualTo("42.0")

      /** Explicitly create an [[reactivemongo.bson.BSONArray]] from a [[Traversable]] of its elements
        */
      //def $array(elements: Traversable[BSONValue]) = BSONArray(elements)
    }

    "construct $binary, $md5, $uuid, $function, $genericBinary correctly" in {
      // TODO: write this test case
      /** Explicitly create a [[reactivemongo.bson.Subtype.UserDefinedSubtype]] [[reactivemongo.bson.BSONBinary]] from a blob
        */
      // def $binary(bytes: Array[Byte]): BSONBinary = BSONBinary(bytes, Subtype.UserDefinedSubtype)

      /** Explicitly create an [[reactivemongo.bson.Subtype.Md5Subtype]] [[reactivemongo.bson.BSONBinary]]  from a blob
        */
      //def $md5(bytes: Array[Byte]): BSONBinary = BSONBinary(bytes, Subtype.Md5Subtype)

      /** Explicitly create a [[reactivemongo.bson.Subtype.UuidSubtype]] [[reactivemongo.bson.BSONBinary]] from a blob
        */
      //def $uuid(bytes: Array[Byte]): BSONBinary = BSONBinary(bytes, Subtype.UuidSubtype)

      /** Explicitly create a [[reactivemongo.bson.Subtype.FunctionSubtype]] [[reactivemongo.bson.BSONBinary]] from a blob
        */
      //def $function(bytes: Array[Byte]): BSONBinary = BSONBinary(bytes, Subtype.FunctionSubtype)

      /** Explicitly create a [[reactivemongo.bson.Subtype.GenericBinarySubtype]] [[reactivemongo.bson.BSONBinary]]
        * from a blob.
        */
      //def $genericBinary(bytes: Array[Byte]): BSONBinary = BSONBinary(bytes, Subtype.GenericBinarySubtype)
      success
    }

    "construct $objId correctly" in {

      // TODO: write this test case
      /** Explicitly create an [[reactivemongo.bson.BSONObjectID]] from a blob
        */
      // def $objId(bytes: Array[Byte]) = BSONObjectID(bytes)

      /** Explicitly create an [[reactivemongo.bson.BSONObjectID]] from a string
        */
      // def $objId(str: String) = BSONObjectID(str)
      success
    }

    "construct $datetime, $timestamp correctly" in {
      // TODO: write this test case
      /** Implicitly convert a [[java.util.Date]] to a [[reactivemongo.bson.BSONDateTime]]
        */
      // implicit def $datetime(date: Date) = BSONDateTime(date.getTime)

      /** Implicitly convert a [[org.joda.time.DateTime]] to a [[reactivemongo.bson.BSONDateTime]]
        */
      // implicit def $datetime(date: DateTime) = BSONDateTime(date.getMillis)
      /** Implicitly convert a [[java.lang.Long]] to a [[reactivemongo.bson.BSONTimestamp]]
        */
      //implicit def $timestamp(value: Long) = BSONTimestamp(value)
      success
    }

    "construct miscellaneous BSONValues correctly" in {

      // TODO: write this test case
      /** Explicitly create a [[reactivemongo.bson.BSONRegex]]
        */
      // def $regex(value: Regex, flags: String) = BSONRegex(value.pattern.pattern(), flags)

      /** Explicitly create a [[reactivemongo.bson.BSONDBPointer]] from a blob
        */
      // def $pointer(name: String, pointer: Array[Byte]) = BSONDBPointer(name, pointer)

      /** Explicitly create a [[reactivemongo.bson.BSONDBPointer]] from a [[reactivemongo.bson.BSONObjectID]]
        */
      // def $pointer(name: String, pointer: BSONObjectID) = BSONDBPointer(name, pointer.valueAsArray)

      /** Explicitly create a [[reactivemongo.bson.BSONJavaScript]] from a String
        */
      // def $javascript(value: String) = BSONJavaScript(value)

      /** Explicitly create a [[reactivemongo.bson.BSONSymbol]] from a String
        */
      // def $symbol(value: String) = BSONSymbol(value)

      /** Implicitly  create a [[reactivemongo.bson.BSONSymbol]] from a Symbol
        */
      // implicit def $symbol(value: Symbol) = BSONSymbol(value.name)

      /** Explicitly createa [[reactivemongo.bson.BSONJavaScriptWS]] from a String
        */
      // def $javascriptws(value: String) = BSONJavaScriptWS(value)
      success
    }

    "construct $id correctly" in {
      val id = $id(42L)
      id must beAnInstanceOf[BSONDocument]
      id.get("_id").isDefined must beTrue
      val value = id.get("_id").get
      value must beAnInstanceOf[BSONLong]
      val int = value.asInstanceOf[BSONLong]
      int.value must beEqualTo(42L)
    }

    val expected_doc = """{
                         |  one: BSONInteger(1),
                         |  two: BSONLong(2),
                         |  three: {
                         |    a: BSONInteger(10)
                         |  }
                         |}""".stripMargin
    "prettify a document" in {
      val doc = $document("one" -> 1, "two" -> 2L, "three" -> $document("a" -> 0x0A))
      val str: String = doc
      str must beEqualTo(expected_doc)
    }
  }

}
