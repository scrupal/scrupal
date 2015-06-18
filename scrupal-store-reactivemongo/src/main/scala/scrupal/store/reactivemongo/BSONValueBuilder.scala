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

import java.util.Date

import scala.reflect.runtime.universe.{ TypeTag, typeOf }
import scala.util.matching.Regex

import org.joda.time.DateTime

import reactivemongo.bson._

trait BSONDocumentHandler[T] extends BSONDocumentReader[T] with BSONDocumentWriter[T] with BSONHandler[BSONDocument,T]

// TODO: https://groups.google.com/forum/#!topic/reactivemongo/DMP_fAA6kBU

/** A BSONDocument Builder
  * Provides a DSL for composing BSNDocuments from primitives and providing conversions from plain old Scala types.
  */
trait BSONValueBuilder {

  /** Type Conversion Writer
    * This type, when used as an implicit argument type, allows conversion from T to the corresponding BSONValue,
    * assuming there is a writer for that type.
    * @tparam T The type to be converted to a BSONValue
    */
  type AnyBSONWriter[T,B <: BSONValue] = BSONWriter[T, B]

  type BSONValueConverter[T] = (T) ⇒ BSONValue

  /** Generate an empty document. Sometimes useful */
  def $empty: BSONDocument = BSONDocument.empty

  /** Implicitly convert a String to a [[reactivemongo.bson.BSONString]]
    * This is implicit for syntactic convenience
    * @param str The string to wrap in a BSONString
    * @return
    */
  implicit def $string(str: String): BSONString = BSONString(str)

  /** Implicitly convert a Double to a [[reactivemongo.bson.BSONDouble]]
    * This is implicit for syntactic convenience
    * @param dbl The value to wrap in a BSONDouble
    * @return
    */
  implicit def $double(dbl: Double): BSONDouble = BSONDouble(dbl)

  /** Explicitly create a [[reactivemongo.bson.BSONDocument]]  from its constituent elements
    * This is likely the simplest constructor for a document. You just list the elements that you want as tuples.
    * Use it like this:
    * {{{\$elements("foo" -> 3, "bar" -> 42.0) }}}
    * @param elements The elements you want included in the document
    * @return Corresponding BSONDocument
    */
  def $document(elements: Producer[BSONElement]*): BSONDocument = {
    BSONDocument(elements: _*)
  }

  /** Explicitly create an [[reactivemongo.bson.BSONArray]]  from its elements
    *
    * @param elements
    * @return
    */
  def $array(elements: Producer[BSONValue]*): BSONArray = BSONArray(elements: _*)

  /** Explicitly create an [[reactivemongo.bson.BSONArray]] from a [[Traversable]] of its elements
    *
    * @param elements
    * @return
    */
  def $array(elements: Traversable[BSONValue]) = BSONArray(elements)

  /** Explicitly create a [[reactivemongo.bson.Subtype.UserDefinedSubtype]] [[reactivemongo.bson.BSONBinary]] from a blob
    *
    * @param bytes
    * @return
    */
  def $binary(bytes: Array[Byte]): BSONBinary = BSONBinary(bytes, Subtype.UserDefinedSubtype)

  /** Explicitly create an [[reactivemongo.bson.Subtype.Md5Subtype]] [[reactivemongo.bson.BSONBinary]]  from a blob
    *
    * @param bytes
    * @return
    */
  def $md5(bytes: Array[Byte]): BSONBinary = BSONBinary(bytes, Subtype.Md5Subtype)

  /** Explicitly create a [[reactivemongo.bson.Subtype.UuidSubtype]] [[reactivemongo.bson.BSONBinary]] from a blob
    *
    * @param bytes
    * @return
    */
  def $uuid(bytes: Array[Byte]): BSONBinary = BSONBinary(bytes, Subtype.UuidSubtype)

  /** Explicitly create a [[reactivemongo.bson.Subtype.FunctionSubtype]] [[reactivemongo.bson.BSONBinary]] from a blob
    *
    * @param bytes
    * @return
    */
  def $function(bytes: Array[Byte]): BSONBinary = BSONBinary(bytes, Subtype.FunctionSubtype)

  /** Explicitly create a [[reactivemongo.bson.Subtype.GenericBinarySubtype]] [[reactivemongo.bson.BSONBinary]]
    * from a blob.
    *
    * @param bytes
    * @return
    */
  def $genericBinary(bytes: Array[Byte]): BSONBinary = BSONBinary(bytes, Subtype.GenericBinarySubtype)

  /** Explicitly create an [[reactivemongo.bson.BSONUndefined]]
    *
    * @return
    */
  def $undefined = BSONUndefined

  /** Explicitly create an [[reactivemongo.bson.BSONObjectID]] from a blob
    *
     * @param bytes
    * @return
    */
  def $objId(bytes: Array[Byte]) = BSONObjectID(bytes)

  /** Explicitly create an [[reactivemongo.bson.BSONObjectID]] from a string
    *
    * @param str
    * @return
    */
  def $objId(str: String) = BSONObjectID(str)

  /** Implicitly convert a Boolean to a [[reactivemongo.bson.BSONBoolean]]
    *
    * @param bool
    * @return
    */
  implicit def $boolean(bool: Boolean) = BSONBoolean(bool)

  /** Implicitly convert a [[java.util.Date]] to a [[reactivemongo.bson.BSONDateTime]]
    *
    * @param date
    * @return
    */
  implicit def $datetime(date: Date) = BSONDateTime(date.getTime)

  /** Implicitly convert a [[org.joda.time.DateTime]] to a [[reactivemongo.bson.BSONDateTime]]
    *
    * @param date
    * @return
    */
  implicit def $datetime(date: DateTime) = BSONDateTime(date.getMillis)

  /** Explicitly create a [[reactivemongo.bson.BSONNull]]
    *
    * @return
    */
  def $null = BSONNull

  /** Explicitly create a [[reactivemongo.bson.BSONRegex]]
    *
    * @param value
    * @param flags
    * @return
    */
  def $regex(value: Regex, flags: String) = BSONRegex(value.pattern.pattern(), flags)

  /** Explicitly create a [[reactivemongo.bson.BSONDBPointer]] from a blob
    *
    * @param name
    * @param pointer
    * @return
    */
  def $pointer(name: String, pointer: Array[Byte]) = BSONDBPointer(name, pointer)

  /** Explicitly create a [[reactivemongo.bson.BSONDBPointer]] from a [[reactivemongo.bson.BSONObjectID]]
    *
    * @param name
    * @param pointer
    * @return
    */
  def $pointer(name: String, pointer: BSONObjectID) = BSONDBPointer(name, pointer.valueAsArray)

  /** Explicitly create a [[reactivemongo.bson.BSONJavaScript]] from a String
    *
    * @param value
    * @return
    */
  def $javascript(value: String) = BSONJavaScript(value)

  /** Explicitly create a [[reactivemongo.bson.BSONSymbol]] from a String
    *
    * @param value
    * @return
    */
  def $symbol(value: String) = BSONSymbol(value)

  /** Implicitly  create a [[reactivemongo.bson.BSONSymbol]] from a Symbol
    *
    * @param value
    * @return
    */
  implicit def $symbol(value: Symbol) = BSONSymbol(value.name)

  /** Explicitly createa [[reactivemongo.bson.BSONJavaScriptWS]] from a String
    *
    * @param value
    * @return
    */
  def $javascriptws(value: String) = BSONJavaScriptWS(value)

  /** Implicitly convert a [[reactivemongo.bson.BSONInteger]] from an integer
    *
    * A simple implicit conversion form int to BSONInteger which can be used explicitly too with
    * @param int
    * @return
    */
  implicit def $int(int: Integer): BSONInteger = BSONInteger(int)

  /** Implicitly convert a [[java.lang.Long]] to a [[reactivemongo.bson.BSONTimestamp]]
    *
    * @param value
    * @return
    */
  implicit def $timestamp(value: Long) = BSONTimestamp(value)

  /** Implicitly convert a [[java.lang.Long]] to a [[reactivemongo.bson.BSONLong]]
    *
    * @param long
    * @return
    */
  implicit def $long(long: Long): BSONLong = BSONLong(long)


  /** Construct a [[reactivemongo.bson.BSONDocument]] that contains the `_id` field only
    *
    * @param id
    * @param convert
    * @tparam T
    * @return
    */
  def $id[T](id: T)(implicit convert: BSONValueConverter[T]): BSONDocument = {
    BSONDocument("_id" -> convert(id))
  }

  /** Implicitly convert a [[reactivemongo.bson.BSONDocument]] to a String
    *
    * @param document
    * @return
    */
  implicit def prettifyBSONDoc(document: BSONDocument): String = BSONDocument.pretty(document)
}

trait ComparisonQueryOperators extends BSONValueBuilder {
  def $eq[T](value: T)(implicit convert: BSONValueConverter[T]) = BSONDocument( "$eq" -> convert(value) )

  /** Matches all values that are not equal to the value specified in the query. */
  def $ne[T](value: T)(implicit convert: BSONValueConverter[T]) = BSONDocument( "$ne" -> convert(value) )

  /** Matches values that are greater than the value specified in the query. */
  def $gt[T](value: T)(implicit convert: BSONValueConverter[T]) = BSONDocument( "$gt" -> convert(value) )

  /** Matches values that are greater than or equal to the value specified in the query. */
  def $gte[T](value: T)(implicit convert: BSONValueConverter[T]) = BSONDocument( "$gte" -> convert(value) )

  /** Matches values that are less than the value specified in the query. */
  def $lt[T](value: T)(implicit convert: BSONValueConverter[T]) = BSONDocument( "$lt" -> convert(value) )

  /** Matches values that are less than or equal to the value specified in the query. */
  def $lte[T](value: T)(implicit convert: BSONValueConverter[T]) = BSONDocument( "$lte" -> convert(value) )

  /** Matches any of the values that exist in an array specified in the query.*/
  def $in[T](values: T*)(implicit convert: BSONValueConverter[T]) = {
    BSONDocument( "$in" -> $array(values.map { v ⇒ convert(v) }) )
  }

  /** Matches values that do not exist in an array specified to the query. */
  def $nin[T](values: T*)(implicit convert: BSONValueConverter[T]) = {
    BSONDocument("$nin" -> $array(values.map { v ⇒ convert(v) }))
  }
}

trait LogicalQueryOperators extends BSONValueBuilder {
  def $not(expression: BSONDocument) = BSONDocument("$not" -> expression)

  def $or(disjunctives: BSONDocument*) = BSONDocument("$or" -> BSONArray(disjunctives))

  def $nor(disjunctives: BSONDocument*) = BSONDocument("$nor" -> BSONArray(disjunctives))

  def $and(conjunctives: BSONDocument*) = BSONDocument("$and" -> BSONArray(conjunctives))

  def $nand(conjunctives: BSONDocument*) = BSONDocument("$not" -> $and(conjunctives: _*))

}

trait ElementQueryOperators extends BSONValueBuilder {

  def $exists(exists: Boolean) = BSONDocument("$exists" -> exists)

  def $type[T <: BSONValue: TypeTag] = BSONDocument("$type" -> bsonTypeNumberOf[T])

  def bsonTypeNumberOf[T <: BSONValue: TypeTag]: Int = typeOf[T] match {
    case t if t =:= typeOf[BSONDouble] ⇒ 1
    case t if t =:= typeOf[BSONString] ⇒ 2
    case t if t =:= typeOf[BSONDocument] ⇒ 3
    case t if t =:= typeOf[BSONArray] ⇒ 4
    case t if t =:= typeOf[BSONBinary] ⇒ 5
    case t if t =:= typeOf[BSONUndefined.type] ⇒ 6
    case t if t =:= typeOf[BSONObjectID] ⇒ 7
    case t if t =:= typeOf[BSONBoolean] ⇒ 8
    case t if t =:= typeOf[BSONDateTime] ⇒ 9
    case t if t =:= typeOf[BSONNull.type] ⇒ 10
    case t if t =:= typeOf[BSONRegex] ⇒ 11
    case t if t =:= typeOf[BSONDBPointer] ⇒ 12
    case t if t =:= typeOf[BSONJavaScript] ⇒ 13
    case t if t =:= typeOf[BSONSymbol] ⇒ 14
    case t if t =:= typeOf[BSONJavaScriptWS] ⇒ 15
    case t if t =:= typeOf[BSONInteger] ⇒ 16
    case t if t =:= typeOf[BSONTimestamp] ⇒ 17
    case t if t =:= typeOf[BSONLong] ⇒ 18
    case t if t =:= typeOf[BSONMinKey.type] ⇒ 255
    case t if t =:= typeOf[BSONMaxKey.type] ⇒ 127
  }
}

trait EvaluationQueryOperators extends BSONValueBuilder {

  def $mod(divisor: Long, expected: Long) = BSONDocument("$mod" -> BSONArray(divisor, expected))

  def $regex(regex: String, options: String) = BSONDocument("$regex" -> regex, "$options" -> options)

  def $where(js_expression: String) = BSONDocument("$where" -> js_expression)

  def $text(search: String, language: String = "en-US"): BSONDocument = {
    BSONDocument("$text" -> BSONDocument("$search" -> search), "$language" -> language)
  }

}

trait GeospatialQueryOperators extends BSONValueBuilder {

  object GeoJSONTypes extends Enumeration {
    type Kind = Value
    val Point = Value
    val LineString = Value
    val Polygon = Value
    val MultiPoint = Value
    val MultiLineString = Value
    val MultiPolygon = Value
    val GeometryCollection = Value
  }

  case class Coordinate(longitude: Double, lattitude: Double)

  implicit def toBSONArray(coords: Coordinate) :BSONArray = BSONArray(coords.longitude, coords.lattitude)

  /** Specifies a rectangular box using legacy coordinate pairs for [[\$geoWithin]] queries.
    *
    * @param bottom_left
    * @param top_right
    * @return
    */
  def $box(bottom_left: Coordinate, top_right: Coordinate) = {
    BSONDocument(
      "$box" -> BSONArray(
        toBSONArray(bottom_left),
        toBSONArray(top_right)
      )
    )
  }

  /** Specifies a circle using legacy coordinate pairs to \$geoWithin queries when using planar geometry.
    *
    */
  def $center(center: Coordinate, radius: Double) = {
    BSONDocument("$center" -> BSONArray(toBSONArray(center), BSONDouble(radius)))
  }

  /** Specifies a circle using either legacy coordinate pairs or GeoJSON format for \$geoWithin queries
    * when using spherical geometry.
    *
    */
  def $centerSphere(center: Coordinate, radius: Double) = {
    BSONDocument("$centerSphere" -> BSONArray(toBSONArray(center), BSONDouble(radius)))
  }

  /** Specifies a geometry in GeoJSON format to geospatial query operators.
    *
    * @param typ
    * @param coordinates
    * @return
    */
  def $geometry(typ: GeoJSONTypes.Kind, coordinates: Traversable[Traversable[Coordinate]]) = {
    BSONDocument("$geometry" ->
      BSONDocument(
        "type" -> BSONString( typ.toString ),
        "coordinates" -> {
          BSONArray {
            for (coord_list <- coordinates; coord <- coord_list) yield { toBSONArray(coord) }
          }
        }
      )
    )
  }

  /** Specifies a polygon to using legacy coordinate pairs for \$geoWithin queries.
    *
    * @param coordinates
    * @return
    */
  def $polygon(coordinates: Traversable[Coordinate]) = {
    BSONDocument("$polygon" -> {
      val as_bson_arrays = for (coords <- coordinates) yield {
        BSONArray(coords.longitude, coords.lattitude)
      }
      BSONArray(as_bson_arrays)
    }
    )
  }

  def $point(coords: Coordinate) = {
    BSONDocument("$geometry" ->
      BSONDocument(
        "type" -> GeoJSONTypes.Point.toString,
        "coordinates" -> BSONArray(coords.longitude, coords.lattitude)
      )
    )
  }


  /** Selects geometries that intersect with a GeoJSON geometry
   * @param typ The type of geometric object
   * @param coordinates The coordinates of the geometric object
   * @return BSONDocument for \$geoIntersects clause
   */
  def $geoIntersects(typ: GeoJSONTypes.Kind, coordinates: Traversable[Traversable[Coordinate]]) = {
    BSONDocument("$geoIntersects" -> $geometry(typ, coordinates))
  }

  /** Selects geometries within a bounding GeoJSON geometry.
    */
  def $geoWithin(typ: GeoJSONTypes.Kind, coordinates: Traversable[Traversable[Coordinate]]) = {
    BSONDocument("$geoWithin" -> $geometry(typ, coordinates))
  }

  /** Returns geospatial objects in proximity to a point on a sphere.
    */
  def $nearSphere(point: Coordinate, maxDistance: Option[Double] = None, minDistance: Option[Double] = None) = {
    BSONDocument( "$nearSphere" -> $point(point).++(maxDistance match {
      case Some(max) ⇒ BSONDocument("$maxDistance" -> BSONDouble(max))
      case None ⇒ $empty
    }).++(minDistance match {
      case Some(min) ⇒ BSONDocument("$minDistance" -> BSONDouble(min))
      case None ⇒ $empty
    }))
  }

  /** Returns geospatial objects in proximity to a point.
    */
  def $near(point: Coordinate, maxDistance: Option[Double] = None, minDistance: Option[Double] = None) = {
    BSONDocument( "$near" -> $point(point).++(maxDistance match {
      case Some(max) ⇒ BSONDocument("$maxDistance" -> BSONDouble(max))
      case None ⇒ $empty
    }).++(minDistance match {
      case Some(min) ⇒ BSONDocument("$minDistance" -> BSONDouble(min))
      case None ⇒ $empty
    }))
  }
}

trait ArrayQueryOperators extends BSONValueBuilder {

  def $all[T](values: T*)(implicit writer: BSONDocumentWriter[T]) = BSONDocument("$all" -> values)

  def $elemMatch(query: Producer[BSONElement]*) = BSONDocument("$elemMatch" -> BSONDocument(query: _*))

  def $size(size: Int) = BSONDocument("$size" -> size)
}

trait ProjectionOperators {

  def $(collName: String, position: Int) = BSONDocument( collName + ".$" -> BSONInteger(position))

  def $meta(which: String) = BSONDocument("$meta" -> which)

}

trait QueryModifiers extends BSONValueBuilder{
  /** 	Adds a comment to the query to identify queries in the database profiler output.
    */
  def $comment = ???

  /** Forces MongoDB to report on query execution plans.
    *
    * @return
    */
  def $explain = ???

  /** Forces MongoDB to use a specific index.
    *
    * @return
    */
  def $hint = ???
  /* TODO: Remaining Query Modifiers in BSONValueBuilder
  def $maxScan	Limits the number of documents scanned.
def $maxTimeMS	Specifies a cumulative time limit in milliseconds for processing operations on a cursor. See maxTimeMS().
def $max	Specifies an exclusive upper limit for the index to use in a query. See max().
def $min	Specifies an inclusive lower limit for the index to use in a query. See min().
def $orderby	Returns a cursor with documents sorted according to a sort specification. See sort().
def $query	Wraps a query document.
def $returnKey	Forces the cursor to only return fields included in the index.
def $showDiskLoc	Modifies the documents returned to include references to the on-disk location of each document.
def $snapshot	Forces the query to use the index on the _id field. See snapshot().
    */
}

trait FieldUpdateOperators extends BSONValueBuilder {

  def $inc(item: Producer[BSONElement], items: Producer[BSONElement]*) = {
    BSONDocument("$inc" -> BSONDocument((Seq(item) ++ items): _*))
  }

  def $mul(item: Producer[BSONElement]): BSONDocument = {
    BSONDocument("$mul" -> BSONDocument(item))
  }

  def $rename(item: (String, String), items: (String, String)*): BSONDocument = {
    BSONDocument("$rename" -> BSONDocument((Seq(item) ++ items).map(Producer.nameValue2Producer[String]): _*))
  }

  def $setOnInsert(item: Producer[BSONElement], items: Producer[BSONElement]*): BSONDocument = {
    BSONDocument("$setOnInsert" -> BSONDocument((Seq(item) ++ items): _*))
  }

  def $set(item: Producer[BSONElement], items: Producer[BSONElement]*): BSONDocument = {
    BSONDocument("$set" -> BSONDocument((Seq(item) ++ items): _*))
  }

  def $unset(field: String, fields: String*): BSONDocument = {
    BSONDocument("$unset" -> BSONDocument((Seq(field) ++ fields).map(_ -> BSONString(""))))
  }

  def $min(item: Producer[BSONElement]): BSONDocument = {
    BSONDocument("$min" -> BSONDocument(item))
  }

  def $max(item: Producer[BSONElement]): BSONDocument = {
    BSONDocument("$max" -> BSONDocument(item))
  }

  trait CurrentDateValueProducer[T] {
    def produce: BSONValue
  }

  implicit class BooleanCurrentDateValueProducer(value: Boolean) extends CurrentDateValueProducer[Boolean] {
    def produce: BSONValue = BSONBoolean(value)
  }

  implicit class StringCurrentDateValueProducer(value: String) extends CurrentDateValueProducer[String] {
    def isValid: Boolean = Seq("date", "timestamp") contains value

    def produce: BSONValue = {
      if (!isValid)
        throw new IllegalArgumentException(value)

      BSONDocument("$type" -> value)
    }
  }

  def $currentDate(items: (String, CurrentDateValueProducer[_])*): BSONDocument = {
    BSONDocument("$currentDate" -> BSONDocument(items.map(item ⇒ item._1 -> item._2.produce)))
  }
}

trait ArrayUpdateOperators extends BSONValueBuilder {

  //**********************************************************************************************//
  // Top Level Array Update Operators
  def $addToSet(item: Producer[BSONElement], items: Producer[BSONElement]*): BSONDocument = {
    BSONDocument("$addToSet" -> BSONDocument((Seq(item) ++ items): _*))
  }

  def $pop(item: (String, Int)): BSONDocument = {
    if (item._2 != -1 && item._2 != 1)
      throw new IllegalArgumentException(s"${item._2} is not equal to: -1 | 1")

    BSONDocument("$pop" -> BSONDocument(item))
  }

  def $push(item: Producer[BSONElement]): BSONDocument = {
    BSONDocument("$push" -> BSONDocument(item))
  }

  def $pushEach[T](field: String, values: T*)(implicit convert: BSONValueConverter[T]): BSONDocument = {
    BSONDocument(
      "$push" -> BSONDocument(
        field -> BSONDocument(
          "$each" -> BSONArray(values.map { v ⇒ convert(v) })
        )
      )
    )
  }

  def $pull(item: Producer[BSONElement]): BSONDocument = {
    BSONDocument("$pull" -> BSONDocument(item))
  }
}

trait BitwiseUpdateOperators extends BSONValueBuilder{

}

trait IsolationUpdateOperators extends BSONValueBuilder{

}


object BSONValueBuilder extends BSONValueBuilder
  with ComparisonQueryOperators
  with LogicalQueryOperators
  with ElementQueryOperators
  with EvaluationQueryOperators
  with ArrayQueryOperators
  with GeospatialQueryOperators
  with ProjectionOperators
  with QueryModifiers
  with FieldUpdateOperators
  with ArrayUpdateOperators
  with BitwiseUpdateOperators
  with IsolationUpdateOperators {

  implicit def long2BSONLong(long: Long) = BSONLong(long)
  implicit def int2BSONInt(int: Integer) = BSONInteger(int)
  implicit def string2BSONString(str: String) = BSONString(str)

  /** Represents a BSONElement expression */
  trait Expression[V <: BSONValue] {
    def name: String
    def value: V
    def append(value: BSONDocument): BSONDocument = value
  }

  implicit def toBSONElement[T <: BSONValue](exp: Expression[T])
                                            (implicit writer: BSONWriter[T, _ <: BSONValue]): Producer[BSONElement] = {
    exp.name -> exp.value
  }

  implicit def toBSONDocument[T <: BSONValue](exp: Expression[T])
                                             (implicit writer: BSONWriter[T, _ <: BSONValue]): BSONDocument = {
    BSONDocument(exp.name -> exp.value)
  }

}



