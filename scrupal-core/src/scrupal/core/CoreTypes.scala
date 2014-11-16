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

package scrupal.core

import java.util.Date

import reactivemongo.bson._
import scrupal.core.api._
import scrupal.utils.Patterns
import Patterns._
import spray.http.{MediaTypes, MediaType}

import scala.concurrent.duration.Duration
import scala.util.matching.Regex
import scala.language.existentials

/** A type that can take any value and always validates successfully */
case class AnyType(
  id : Identifier,
  description: String
) extends Type {
  def asT = this
  def apply(value: BSONValue) : ValidationResult = None
  override def kind = 'Any
  override def trivial = true
}

case class BooleanType(
  id : Identifier,
  description: String
) extends Type {
  override type ScalaValueType = Boolean

  def asT = this
  override def kind = 'Boolean
  def verity = List("true", "on", "yes", "confirmed")
  def falseness = List("false", "off", "no", "denied")

  def apply(value: BSONValue) : ValidationResult = single(value) {
    case BSONBoolean(b) => None
    case BSONInteger(bi)if bi == 0 || bi == 1 => None
    case BSONInteger(bi) => Some(s"Value '$bi' could not be converted to boolean (0 or 1 required)")
    case BSONLong(bi)   if bi == 0 || bi == 1 => None
    case BSONLong(bi) => Some(s"Value '$bi' could not be converted to boolean (0 or 1 required)")
    case BSONString(bs) if verity.contains(bs.toLowerCase)  => None
    case BSONString(bs) if falseness.contains(bs.toLowerCase) => None
    case BSONString(bs) => Some(s"Value '$bs' could not be interpreted as a boolean")
    case x: BSONValue => wrongClass("BSONBoolean, BSONInteger, BSONLong, BSONString", x)
  }
}

/** A String type constrains a string by defining its content with a regular expression and a maximum length.
  *
  * @param id THe name of the string type
  * @param description A brief expression of the string type
  * @param regex The regular expression that specifies legal values for the string type
  * @param maxLen The maximum length of this string type
  */
case class StringType (
  id : Identifier,
  description : String,
  regex : Regex,
  maxLen : Int = Int.MaxValue
) extends Type {
  override type ScalaValueType = String
  require(maxLen >= 0)
  def asT = this
  def apply(value: BSONValue) = single(value) {
    case BSONString(bs) if bs.length > maxLen => Some(s"String of length ${bs.length} exceeds maximum of $maxLen")
    case BSONString(bs) if !regex.pattern.matcher(bs).matches() => Some(s"String '${bs}' does not match pattern '${regex.pattern.pattern()}")
    case BSONString(bs) => None
    case x: BSONValue => wrongClass("BSONString", x)
  }
  override def kind = 'String
}

/** A Range type constrains Long Integers between a minimum and maximum value
  *
  * @param id
  * @param description
  * @param min
  * @param max
  */
case class RangeType (
  id : Identifier,
  description : String,
  min : Long = Long.MinValue,
  max : Long = Long.MaxValue
) extends Type {
  override type ScalaValueType = Long
  require(min <= max)
  def asT = this
  def apply(value: BSONValue) = single(value) {
    case BSONLong(l) if l < min => Some(s"Value $l is out of range, below minimum of $min")
    case BSONLong(l) if l > max => Some(s"Value $l is out of range, above maximum of $max")
    case BSONLong(l) => None
    case BSONInteger(i) if i < min => Some(s"Value $i is out of range, below minimum of $min")
    case BSONInteger(i) if i > max => Some(s"Value $i is out of range, above maximum of $max")
    case BSONInteger(i) => None
    case x: BSONValue => wrongClass("BSONInteger or BSONLong",x)
  }
  override def kind = 'Range
}

/** A Real type constrains Double values between a minimum and maximum value
  *
  * @param id
  * @param description
  * @param min
  * @param max
  */
case class RealType (
  id : Identifier,
  description : String,
  min : Double = Double.MinValue,
  max : Double = Double.MaxValue
) extends Type {
  override type ScalaValueType = Double
  require(min <= max)
  def asT = this
  def apply(value: BSONValue) =  single(value) {
    case BSONDouble(d) if d < min => Some(s"Value $d is out of range, below minimum of $min")
    case BSONDouble(d) if d > max => Some(s"Value $d is out of range, above maximum of $max")
    case BSONDouble(d) => None
    case BSONLong(l) if l < min => Some(s"Value $l is out of range, below minimum of $min")
    case BSONLong(l) if l > max => Some(s"Value $l is out of range, above maximum of $max")
    case BSONLong(l) => None
    case BSONInteger(i) if i < min => Some(s"Value $i is out of range, below minimum of $min")
    case BSONInteger(i) if i > max => Some(s"Value $i is out of range, above maximum of $max")
    case BSONInteger(i) => None
    case x: BSONValue => wrongClass("BSONDouble",x)
  }
  override def kind = 'Real
}

/** A point-in-time value between a minimum and maximum time point
  *
  * @param id
  * @param description
  * @param min
  * @param max
  */
case class TimestampType (
  id : Identifier,
  description: String,
  min: Date = new Date(0L),
  max: Date = new Date(Long.MaxValue)
) extends Type {
  override type ScalaValueType = Duration
  assert(min.getTime <= max.getTime)
  def asT = this
  def apply(value: BSONValue) = single(value) {
    case BSONLong(l) if l < min.getTime => Some(s"Timestamp $l is out of range, below minimum of $min")
    case BSONLong(l) if l > max.getTime => Some(s"Timestamp $l is out of range, above maximum of $max")
    case BSONLong(l) => None
    case x: BSONValue => wrongClass("BSONLong",x)
  }
}


/** A BLOB type has a specified MIME content type and a minimum and maximum length
  *
  * @param id
  * @param description
  * @param mime
  * @param maxLen
  */
case class BLOBType  (
  id : Identifier,
  description : String,
  mime : String,
  maxLen : Long = Long.MaxValue
) extends Type {
  override type ScalaValueType = Array[Byte]
  assert(maxLen >= 0)
  assert(mime.contains("/"))
  def asT = this
  def apply(value: BSONValue) = single(value) {
    case b: BSONBinary if b.value.size > maxLen => Some(s"BLOB of length ${b.value.size} exceeds maximum length of ${maxLen}")
    case b: BSONBinary => None
    case b: BSONString if b.value.length > maxLen => Some(s"BLOB of length ${b.value.size} exceeds maximum length of ${maxLen}")
    case b: BSONString => None
    case x: BSONValue => wrongClass("BSONBinary",x)
  }
  override def kind = 'BLOB

}

case class EnumValidator(enumerators: Map[Identifier, Int], name: String) extends BSONValidator[BSONValue] {

  def apply(value: BSONValue): ValidationResult = single(value) {
    case BSONInteger(x) if !enumerators.exists { y => y._2 == x} =>
      Some(s"Value $x not valid for '$name'")
    case BSONInteger(x) => None
    case BSONLong(x) if !enumerators.exists { y => y._2 == x} =>
      Some(s"Value $x not valid for '$name'")
    case BSONLong(x) => None
    case BSONString(x) if !enumerators.contains(Symbol(x)) =>
      Some(s"Value '$x' not valid for '$name'")
    case BSONString(x) => None
    case x: BSONValue => wrongClass("BSONInteger, BSONLong or BSONString", x)
  }
}

/** An Enum type allows a selection of one enumerator from a list of enumerators.
  * Each enumerator is assigned an integer value.
  */
case class EnumType  (
  id : Identifier,
  description : String,
  enumerators : Map[Identifier, Int]
) extends Type {
  override type ScalaValueType = Int
  require(enumerators.nonEmpty)
  def asT = this
  def apply(value: BSONValue) = EnumValidator(enumerators,label)(value)
  override def kind = 'Enum

  def valueOf(enum: Symbol) = enumerators.get(enum)
  def valueOf(enum: String) = enumerators.get(Symbol(enum))
}

case class MultiEnumType(
  id : Identifier,
  description: String,
  enumerators: Map[Identifier, Int]
) extends Type {
  override type ScalaValueType = Seq[Int]
  require(enumerators.nonEmpty)
  def asT = this
  def apply(value: BSONValue) = {
    value match {
      case a: BSONArray => validate(a.values,  EnumValidator(enumerators, label))
      case x: BSONValue => single(value) { _ => wrongClass("BSONArray", x) }
    }
  }
}

/** A List type allows a non-exclusive list of elements of other types to be constructed
  *
  * @param id
  * @param description
  * @param elemType
  */
case class ListType  (
  id : Identifier,
  description : String,
  elemType : Type
) extends IndexableType {
  override type ScalaValueType = Seq[elemType.ScalaValueType]
  def asT = this
  override def kind = 'List
  def apply(value: BSONValue) : ValidationResult = {
    value match {
      case a: BSONArray => validate(a.values, elemType)
      case x: BSONValue => wrongClass("BSONArray", x).map { s => Seq(s)}
    }
  }
}

/** A Set type allows an exclusive Set of elements of other types to be constructed
  *
  * @param id
  * @param description
  * @param elemType
  */
case class SetType  (
  id : Identifier,
  description : String,
  elemType : Type
) extends IndexableType {
  override type ScalaValueType = Seq[elemType.ScalaValueType]
  def asT = this
  override def kind = 'Set
  def apply(value: BSONValue) : ValidationResult = {
    value match {
      case a: BSONArray =>
        val vals = a.values // stop dealing with a stream
        val msgs = validate(vals, elemType)
        val distinct = vals.distinct
        if (distinct.size != vals.size) {
          val delta = vals.filterNot { x ⇒ distinct.contains(x) }
          msgs.map { x ⇒ x ++ Seq(s"Set contains non-distinct values: $delta") }
        }
        else
          msgs
      case x: BSONValue =>
        wrongClass("BSONArray", x).map { s => Seq(s)}
    }
  }
}

/** A Map is a set whose elements are named with an arbitrary string
  *
  * @param id
  * @param description
  * @param elemType
  */
case class MapType  (
  override val id : Identifier,
  description : String,
  elemType : Type
) extends DocumentType {
  override type ScalaValueType = Map[Identifier, elemType.ScalaValueType]
  def asT = this
  override def kind = 'Map
  def validatorFor(id: String) : Option[Type] = Some(elemType)
  def fieldNames: Seq[String] = Seq.empty[String]
}

trait StructuredType extends DocumentType {
  val fields : Map[String, Type]
  def validatorFor(id:String) : Option[Type] = fields.get(id)
  def fieldNames : Iterable[String] = fields.keys
  def size = fields.size
}

/** A group of named and typed fields that are related in some way.
  * An bundle type defines the structure of the fundamental unit of storage in Scrupal: An instance.  BundleTypes
  * are analogous to table definitions in relational databases, but with one important difference. An BundleType can
  * define itself recursively. That is, one of the fields of an Bundle can be another Bundle Type. Cycles in the
  * definitions of BundleTypes are not permitted. In this way it is possible to assemble entities from a large
  * collection of smaller bundle concepts (traits if you will). Like relational tables, bundle types define the names
  * and types of a group of fields (columns) that are in some way related. For example, a street name, city name,
  * country and postal code are fields of an address bundle because they are related by the common purpose of
  * specifying a location.
  *
  * Note that BundleTypes, along with ListType, SetType and MapType make the type system fully composable. You
  * can create an arbitrarily complex data structure (not that we recommend that you do so).
  *
  * @param id The name of the trait
  * @param description A description of the trait in terms of its purpose or utility
  * @param fields A map of the field name symbols to their Type
  */
case class BundleType (
  id : Identifier,
  description : String,
  fields : Map[String, Type]
) extends StructuredType {
  override type ScalaValueType = Map[String,Any]
  def asT = this
  override def kind = 'Bundle
}

object BundleType {
  val Empty = BundleType('EmptyBundleType, "A Bundle with no fields", Map.empty[String,Type])
}

/** Abstract Node Type
  *
  * A NodeType defines a way to generate
  * NodeType inherits this trait so it defines an apply method with
  * a matching signature. The intent is that the BSONDocument supplied to the NodeType is validated against the
  * node types fields. Because a Type is also a validator
  * @param id
  * @param description
  * @param module
  * @param fields
  * @param mediaType
  */
case class NodeType (
  id : Identifier,
  description : String,
  fields : Map[String, Type],
  mediaType : MediaType = MediaTypes.`text/html`
) extends StructuredType
{
  override type ScalaValueType = Map[String,Any]
  def asT : NodeType = this
  override def kind = 'Node
}

object AnyType_t extends AnyType('Any, "A type that accepts any value")

object AnyString_t extends StringType('AnyString, "A type that accepts any string input", ".*".r, 1024*1024)

object TheBoolean_t extends BooleanType('TheBoolean, "A type that accepts true/false values")

object NonEmptyString_t extends
  StringType('NonEmptyString, "A type that accepts any string input except empty", ".+".r, 1024*1024)

object Password_t extends
  StringType('Password, "A type for human written passwords", anchored(Password), 64)

object AnyInteger_t extends
  RangeType('AnyInteger, "A type that accepts any integer value", Int.MinValue, Int.MaxValue)

object AnyReal_t extends
  RealType('AnyReal, "A type that accepts any double floating point value", Double.MinValue, Double.MaxValue)

object AnyTimestamp_t extends
  TimestampType('AnyTimestamp, "A type that accepts any timestamp value")

object Boolean_t extends
  BooleanType('Boolean, "A Boolean truth value accepting 0/1 values or true/false, on/off, etc.")

/** The Scrupal Type for the identifier of things */
object Identifier_t
  extends StringType('Identifier, "Scrupal Identifier", anchored(Identifier), 64)

object Description_t
  extends StringType('Description, "Scrupal Description", anchored(Markdown), 1024)

object Markdown_t
  extends StringType('Markdown, "Markdown document type", anchored(Markdown))

/** The Scrupal Type for domain names per  RFC 1035, RFC 1123, and RFC 2181 */
object DomainName_t
  extends StringType('DomainName, "RFC compliant Domain Name", anchored(DomainName), 253)

/** The Scrupal Type for TCP port numbers */
object TcpPort_t
  extends RangeType('TcpPort, "A type for TCP port numbers", 1, 65535) {
}

/** The Scrupal Type for Uniform Resource Locators.
  * We should probably have one for URIs too,  per http://tools.ietf.org/html/rfc3986
  */
object URL_t
  extends StringType ('URL, "Uniform Resource Locator", anchored(UniformResourceLocator))

/** The Scrupal Type for IP version 4 addresses */
object IPv4Address_t
  extends StringType('IPv4Address, "A type for IP v4 Addresses", anchored(IPv4Address), 15)

/** The Scrupal Type for Email addresses */
object EmailAddress_t
  extends StringType('EmailAddress, "An email address", anchored(EmailAddress), 253)

object LegalName_t
  extends StringType('LegalName, "The name of a person or corporate entity", anchored(LegalName), 128)

object Title_t
  extends StringType('Title, "A string that is valid for a page title", anchored(Title), 70)

/** The Scrupal Type for information about Sites */
object SiteInfo_t
  extends  BundleType('SiteInfo, "Basic information about a site that Scrupal will serve.",
    fields = Map(
      "name" -> Identifier_t,
      "title" -> Identifier_t,
      "domain" -> DomainName_t,
      "port" -> TcpPort_t,
      "admin_email" -> EmailAddress_t,
      "copyright" -> Identifier_t
    )
)

object PageBundle_t
  extends BundleType('PageBundle, "Information bundle for a page entity.",
    fields = Map (
      "title" -> Title_t,
      "body" -> Markdown_t
     // TODO: Figure out how to structure a bundle to factor in constructing a network of nodes
     // 'master -> Node_t,
     // 'defaultLayout -> Node_t,
     // 'body -> Node_t,
     // 'blocks ->
    )
)
