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

package scrupal.core

import reactivemongo.bson._
import scrupal.core.api._
import scrupal.core.Patterns._

import scala.util.matching.Regex
import scala.language.existentials

/** A type that can take any value and always validates successfully */
case class AnyType(
  id: Identifier,
  description: String,
  module: Module
) extends Type {
  def asT = this
  def apply(value: BSONValue) : ValidationResult = None
  override def kind = 'Any
  override def trivial = true
}

case class BooleanType(
  id : Identifier,
  description: String,
  module: Module
) extends Type {
  def asT = this
  override def kind = 'Boolean
  def verity = List("true", "on", "yes", "confirmed")
  def falseness = List("false", "off", "no", "denied")

  def apply(value: BSONValue) : ValidationResult = single(value) {
    case BSONBoolean(b) => None
    case BSONInteger(bi)if (bi == 0 || bi == 1) => None
    case BSONInteger(bi) => Some(s"Value '$bi' could not be converted to boolean (0 or 1 required)")
    case BSONLong(bi)   if (bi == 0 || bi == 1) => None
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
  module: Module,
  regex : Regex,
  maxLen : Int = Int.MaxValue
) extends Type {
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
  module: Module,
  min : Long = Long.MinValue,
  max : Long = Long.MaxValue
) extends Type {
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
  module: Module,
  min : Double = Double.MinValue,
  max : Double = Double.MaxValue
) extends Type {
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
  module: Module,
  mime : String,
  maxLen : Long = Long.MaxValue
) extends Type {
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

case class EnumValidator(enumerators: Map[Identifier, Int], name: String) extends Validator {

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
  module: Module,
  enumerators : Map[Identifier, Int]
) extends Type {
  require(enumerators.nonEmpty)
  def asT = this
  def apply(value: BSONValue) = EnumValidator(enumerators,label)(value)
  override def kind = 'Enum

  def valueOf(enum: Symbol) = enumerators.get(enum)
  def valueOf(enum: String) = enumerators.get(Symbol(enum))
}

case class MultiEnumType(
  id: Identifier,
  description: String,
  module: Module,
  enumerators: Map[Identifier, Int]
) extends Type {
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
  module: Module,
  elemType : Type
) extends CompoundType {
  def asT = this
  override def kind = 'List
  def apply(value: BSONValue) : ValidationResult = {
    value match {
      case a: BSONArray => validate(a, elemType)
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
  override val id : Identifier,
  description : String,
  module: Module,
  elemType : Type
) extends CompoundType {
  def asT = this
  override def kind = 'Set
  def apply(value: BSONValue) : ValidationResult = {
    value match {
      case a: BSONArray => validate(a, elemType) // FIXME: validate there are no duplicates
      case x: BSONValue => wrongClass("BSONArray", x).map { s => Seq(s)}
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
  module: Module,
  elemType : Type
) extends CompoundType {
  def asT = this
  override def kind = 'Map
  def apply(value: BSONValue) : ValidationResult = {
    value match {
      case d: BSONDocument => validate(d.elements.map { pair => pair._2 }.toSeq, elemType)
      case x: BSONValue => wrongClass("BSONDocument", x).map { s => Seq(s) }
    }
  }
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
  module: Module,
  fields : Map[Identifier, Type]
) extends Type {
  require(!fields.isEmpty)
  def asT = this
  override def kind = 'Bundle
  def apply(value: BSONValue) : ValidationResult = {
    value match {
      case d: BSONDocument =>
        // Note: Stream this to a map up front because we're going to access everything so we might as well stream it once
        val elems = d.elements.toMap
        def dovalidate(field: (Identifier, Type)) = {
          if (!elems.contains(field._1.name))
            Some(Seq(s"Field '${field._1.name}' is missing."))
          else
            field._2.validate(elems.get(field._1.name).get)
        }
        val errors = {
          for (field <- fields; result = dovalidate(field) if result.isDefined) yield {
            result.get
          }
        }.flatten.toSeq
        if (errors.isEmpty)
          None
        else
          Some(errors)
      case x: BSONValue =>
        wrongClass("BSONDocument", x).map { s => Seq(s) }
    }
  }
}


object AnyType_t extends
  AnyType('Any, "A type that accepts any value", CoreModule)

object AnyString_t extends
  StringType('AnyString, "A type that accepts any string input", CoreModule, ".*".r, 1024*1024)

object NonEmptyString_t extends
  StringType('NonEmptyString, "A type that accepts any string input except empty", CoreModule, ".+".r, 1024*1024)

object Password_t extends
  StringType('Password, "A type for human written passwords", CoreModule, anchored(Password), 64)

object AnyInteger_t extends
  RangeType('Integer, "A type that accepts any integer value", CoreModule, Int.MinValue, Int.MaxValue)

object AnyReal_t extends
  RealType('Real, "A type that accepts any double floating point value", CoreModule, Double.MinValue, Double.MaxValue)

object Boolean_t extends
  BooleanType('Boolean, "A Boolean truth value accepting 0/1 values or true/false, on/off, etc.", CoreModule)

/** The Scrupal Type for the identifier of things */
object Identifier_t
  extends StringType('Identifier, "Scrupal Identifier", CoreModule, anchored(Identifier), 64)

object Description_t
  extends StringType('Description, "Scrupal Description", CoreModule, anchored(Markdown), 1024)

object Markdown_t
  extends StringType('Markdown, "Markdown document type", CoreModule, anchored(Markdown))

/** The Scrupal Type for domain names per  RFC 1035, RFC 1123, and RFC 2181 */
object DomainName_t
  extends StringType('DomainName, "RFC compliant Domain Name", CoreModule, anchored(DomainName), 253)

/** The Scrupal Type for TCP port numbers */
object TcpPort_t
  extends RangeType('TcpPort, "A type for TCP port numbers", CoreModule, 1, 65535) {
}

/** The Scrupal Type for Uniform Resource Locators.
  * We should probably have one for URIs too,  per http://tools.ietf.org/html/rfc3986
  */
object URL_t
  extends StringType ('URL, "Uniform Resource Locator", CoreModule, anchored(UniformResourceLocator))

/** The Scrupal Type for IP version 4 addresses */
object IPv4Address_t
  extends StringType('IPv4Address, "A type for IP v4 Addresses", CoreModule, anchored(IPv4Address), 15)

/** The Scrupal Type for Email addresses */
object EmailAddress_t
  extends StringType('EmailAddress, "An email address", CoreModule, anchored(EmailAddress), 253)

object LegalName_t
  extends StringType('LegalName, "The name of a person or corporate entity", CoreModule, anchored(LegalName), 128)

/** The Scrupal Type for information about Sites */
object SiteInfo_t
  extends  BundleType('SiteInfo, "Basic information about a site that Scrupal will serve.", CoreModule,
    fields = Map(
      'name -> Identifier_t,
      'title -> Identifier_t,
      'domain -> DomainName_t,
      'port -> TcpPort_t,
      'admin_email -> EmailAddress_t,
      'copyright -> Identifier_t
    )
)

object PageBundle_t
  extends BundleType('PageBundle, "Information bundle for a page entity.", CoreModule,
    fields = Map (
      'body -> Markdown_t
    )
)
