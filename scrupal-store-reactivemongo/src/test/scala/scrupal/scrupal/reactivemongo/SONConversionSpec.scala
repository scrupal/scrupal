/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
 *                                                                                                                    *
 * Copyright © 2015 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
 * except in compliance with the License. You may obtain a copy of the License at                                     *
 *                                                                                                                    *
 *        http://www.apache.org/licenses/LICENSE-2.0                                                                  *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the                          *
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
 * either express or implied. See the License for the specific language governing permissions                         *
 * and limitations under the License.                                                                                 *
 **********************************************************************************************************************/

package scrupal.store.reactivemongo

import java.nio.charset.Charset
import java.util

import org.specs2.mutable.Specification
import play.api.libs.json._
import reactivemongo.bson._


/** Title Of Thing.
  *
  * Description of thing
  */
class SONConversionSpec extends Specification {

  import SONConversion._
  import reactivemongo.bson.DefaultBSONHandlers._

  "toJSON" should {
    "handle Documents" in {
      val b = BSONDocument("foo" → BSONLong(42L), "bar" → BSONString("fourty-two"))
      val j : JsValue = toJSON(b)
      j.isInstanceOf[JsObject] must beTrue
      val jso = j.asInstanceOf[JsObject]
      val foo = (jso \ "foo" \ "$long").as[Long]
      foo must beEqualTo(42L)
      val bar = (jso \ "bar").as[String]
      bar must beEqualTo("fourty-two")
    }
    "handle String" in {
      val b = BSONString("fourty-two")
      val j : JsValue = toJSON(b)
      j.isInstanceOf[JsString] must beTrue
      j.asInstanceOf[JsString].value must beEqualTo("fourty-two")
    }
    "handle Array" in {
      val b = BSONArray(BSONInteger(21), BSONLong(42), BSONDouble(84.0), BSONString("42"))
      val j : JsValue = toJSON(b)
      j.isInstanceOf[JsArray] must beTrue
      val values = j.asInstanceOf[JsArray].value
      values must beEqualTo(Seq(
        JsObject(Seq("$int"→JsNumber(21))),
        JsObject(Seq("$long"→JsNumber(42))),
        JsNumber(84.0),
        JsString("42")
      ))
    }
    "handle ObjectID" in {
      val b = BSONObjectID(Array[Byte](1,2,3,4,5,6,7,8,9,10,11,12))
      val j = toJSON(b)
      j.isInstanceOf[JsObject] must beTrue
      val value = (j.asInstanceOf[JsObject] \ "$oid").as[String]
      value must beEqualTo("0102030405060708090a0b0c")
    }
    "handle DateTime" in {
      val now = System.currentTimeMillis()
      val b = BSONDateTime(now)
      val j : JsValue = toJSON(b)
      j.isInstanceOf[JsObject] must beTrue
      val jso = j.asInstanceOf[JsObject]
      (jso \ "$datetime").as[Long] must beEqualTo(now)
    }
    "handle DBPointer" in {
      val b = BSONDBPointer("foo", Array[Byte](1,2,3,4,5,6,7,8,9,10,11,12))
      val j = toJSON(b)
      j.isInstanceOf[JsObject] must beTrue
      val jso = j.asInstanceOf[JsObject]
      val value = (jso \ "$value").as[String]
      val id = (jso \ "$id").as[String]
      value must beEqualTo("foo")
      id must beEqualTo("0102030405060708090a0b0c")
    }
    "handle Boolean" in {
      val b = BSONBoolean(true)
      val j : JsValue = toJSON(b)
      j.isInstanceOf[JsBoolean] must beTrue
      j.asInstanceOf[JsBoolean].value must beEqualTo(true)
    }
    "handle Long" in {
      val b = BSONLong(42L)
      val j : JsValue = toJSON(b)
      j.isInstanceOf[JsObject] must beTrue
      (j.asInstanceOf[JsObject] \ "$long").as[Long] must beEqualTo(42)
    }
    "handle Integer" in {
      val b = BSONInteger(42)
      val j : JsValue = toJSON(b)
      j.isInstanceOf[JsObject] must beTrue
      (j.asInstanceOf[JsObject] \ "$int").as[Int]  must beEqualTo(42)
    }
    "handle Double" in {
      val b = BSONDouble(42.0)
      val j : JsValue = toJSON(b)
      j.isInstanceOf[JsNumber] must beTrue
      j.asInstanceOf[JsNumber].value.toDouble must beEqualTo(42.0)
    }
    "handle Binary" in {
      val b = BSONBinary(Array[Byte](1,2,3,4,5,6,7,8,9,10,11,12), Subtype.GenericBinarySubtype)
      val j = toJSON(b)
      j.isInstanceOf[JsObject] must beTrue
      val jso = j.asInstanceOf[JsObject]
      val binary = (jso \ "$binary").as[String]
      binary must beEqualTo("0102030405060708090a0b0c")
      val subtype = (jso \ "$type").as[String]
      subtype must beEqualTo("00")
    }
  }


  "toBSON" should {
    "handle Object" in {
      val j = Json.obj("foo" → 42.0, "bar" → "fourty-two")
      val jsResult = toBSON(j)
      val b = jsResult match {
        case JsSuccess(x,_) ⇒ x
        case JsError(err) ⇒ failure(err.toString)
      }
      b.isInstanceOf[BSONDocument] must beTrue
      val bd = b.asInstanceOf[BSONDocument]
      val foo = bd.get("foo").get.asInstanceOf[BSONDouble].value
      foo must beEqualTo(42L)
      val bar = bd.get("bar").get.asInstanceOf[BSONString].value
      bar must beEqualTo("fourty-two")
    }
    "handle String" in {
      val j = JsString("fourty-two")
      val b = toBSON(j).get
      b.isInstanceOf[BSONString] must beTrue
      b.asInstanceOf[BSONString].value must beEqualTo("fourty-two")
    }
    "handle Array" in {
      val j = JsArray(Seq(JsString("foo"), JsBoolean(true), JsNumber(42.0)))
      val b = toBSON(j).get
      b.isInstanceOf[BSONArray] must beTrue
      val values = b.asInstanceOf[BSONArray].values.toList
      values must beEqualTo(Seq(BSONString("foo"), BSONBoolean(true), BSONDouble(42.0)))
    }
    "handle ObjectID" in {
      val j = JsObject(Seq("$oid" → JsString("0102030405060708090A0B0C")))
      val b = toBSON(j).get
      b.isInstanceOf[BSONObjectID] must beTrue
      val value = b.asInstanceOf[BSONObjectID].valueAsArray
      value must beEqualTo(Array[Byte](1,2,3,4,5,6,7,8,9,10,11,12))
    }
    "handle DateTime" in {
      val now = System.currentTimeMillis()
      val j = JsObject(Seq("$datetime" → JsNumber(now)))
      val b = toBSON(j).get
      b.isInstanceOf[BSONDateTime] must beTrue
      val value = b.asInstanceOf[BSONDateTime].value
      value must beEqualTo(now)
    }
    "handle DBPointer" in {
      val j = JsObject(Seq("$value" → JsString("foo"), "$id" → JsString("0102030405060708090A0B0C")))
      val b = toBSON(j).get
      b.isInstanceOf[BSONDBPointer] must beTrue
      val value = b.asInstanceOf[BSONDBPointer].value
      val id = b.asInstanceOf[BSONDBPointer].id
      value must beEqualTo("foo")
      id must beEqualTo(Array[Byte](1,2,3,4,5,6,7,8,9,10,11,12))
    }
    "handle Boolean" in {
      val b = BSONBoolean(true)
      val j = toJSON(b)
      j.isInstanceOf[JsBoolean] must beTrue
      j.asInstanceOf[JsBoolean].value must beEqualTo(true)
    }
    "handle Long" in {
      val j = JsObject(Seq("$long" → JsNumber(42L)))
      val b = toBSON(j).get
      b.isInstanceOf[BSONLong] must beTrue
      b.asInstanceOf[BSONLong].value must beEqualTo(42L)
    }
    "handle Integer" in {
      val j = JsObject(Seq("$int" → JsNumber(42)))
      val b = toBSON(j).get
      b.isInstanceOf[BSONInteger] must beTrue
      b.asInstanceOf[BSONInteger].value must beEqualTo(42)
    }
    "handle Double value" in {
      val j = JsNumber(42.0D)
      val b = toBSON(j).get
      b.isInstanceOf[BSONDouble] must beTrue
      b.asInstanceOf[BSONDouble].value must beEqualTo(42.0)
    }
    "handle Double" in {
      val j = JsObject(Seq("$double" → JsNumber(42.0D)))
      val b = toBSON(j).get
      b.isInstanceOf[BSONDouble] must beTrue
      b.asInstanceOf[BSONDouble].value must beEqualTo(42.0D)

    }
    "handle Binary" in {
      val j = JsObject(Seq("$binary" → JsString("0102030405060708090a0b0c"), "$type" → JsString("00")))
      val b = toBSON(j).get
      b.isInstanceOf[BSONBinary] must beTrue
      val bin = b.asInstanceOf[BSONBinary]
      val binary = bin.value.readArray(12)
      binary must beEqualTo(Array[Byte](1,2,3,4,5,6,7,8,9,10,11,12))
      bin.subtype must beEqualTo(Subtype.GenericBinarySubtype)
    }
  }

  "SONConversion" should {
    "BSONDBPointer.equals works" in {
      val dbp1 = BSONDBPointer("coll", Array[Byte](1,2,3,4,5,6,7,8,9,10,11,12))
      val dbp2 = dbp1.copy()
      dbp1 must beEqualTo(dbp2)
    }
    "BSONObjectID.equals works" in {
      val boid1 = BSONObjectID("0102030405060708090a0b0c")
      val boid2 = BSONObjectID("0102030405060708090a0b0c")
      boid1 must beEqualTo(boid2)
      val j1 = toJSON(boid1)
      val j2 = toJSON(boid2)
      j1 must beEqualTo(j2)
    }
    "be BSON->JSON->BSON->JSON->BSON reflective" in {
      val b1 = BSONDocument(Seq(
        "boolean" → BSONBoolean(value=true),
        "int" → BSONInteger(42),
        "long" → BSONLong(42L),
        "double" → BSONDouble(42.0),
        "string" → BSONString("forty-two"),
        "datetime" → BSONDateTime(System.currentTimeMillis()),
        "timestamp" → BSONTimestamp(System.currentTimeMillis()),
        "binary" → BSONBinary(Array[Byte](1,2,3), Subtype.GenericBinarySubtype),
        "objectid" → BSONObjectID(Array[Byte](1,2,3,4,5,6,7,8,9,10,11,12)),
        "dbpointer" → BSONDBPointer("coll", Array[Byte](1,2,3,4,5,6,7,8,9,10,11,12)),
        "array" → BSONArray(Seq(BSONInteger(42), BSONString("42"), BSONDouble(42.0), BSONDateTime(0)))
      ))
      val j1 = toJSON(b1)
      val b2 = toBSON(j1).get.asInstanceOf[BSONDocument]
      b1.get("boolean") must beEqualTo(b2.get("boolean"))
      b1.get("int") must beEqualTo(b2.get("int"))
      b1.get("long") must beEqualTo(b2.get("long"))
      b1.get("double") must beEqualTo(b2.get("double"))
      b1.get("string") must beEqualTo(b2.get("string"))
      b1.get("datetime") must beEqualTo(b2.get("datetime"))
      b1.get("timestamp") must beEqualTo(b2.get("timestamp"))
      b1.get("binary") must beEqualTo(b2.get("binary"))
      b1.get("objectid") must beEqualTo(b2.get("objectid"))
      b1.get("dbpointer") must beEqualTo(b2.get("dbpointer"))
      b1.get("array") must beEqualTo(b2.get("array"))
      val j2 = toJSON(b2)
      j2 must beEqualTo(j1)
      b2 must beEqualTo(b1)
      val b3 = toBSON(j2).get
      b3 must beEqualTo(b1)
    }
  }
}
