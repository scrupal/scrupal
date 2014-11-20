/**********************************************************************************************************************
  * This file is part of Scrupal a Web Application Framework.                                                          *
  *                                                                                                                    *
  * Copyright (c) 2014, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

import java.util.Date

import reactivemongo.bson._
import scrupal.utils.Patterns._
import spray.http.{MediaTypes, MediaType}

import scala.concurrent.duration.Duration
import scala.language.existentials
import scala.util.matching.Regex
import scala.util.{Success, Failure, Try}

import scrupal.utils.{Registry, Pluralizer, Registrable}

/** A generic Type used as a placeholder for subclasses that compose types.
  * Note that the name of the Type is a Symbol. Symbols are interned so there is only ever one copy of the name of
  * the type. This is important because type linkage is done by indirectly referencing the name, not the actual type
  * object and name reference equality is the same as type equality.  Scrupal also interns the Type objects that
  * modules provide so they can be looked up by name quickly. This allows inter-module integration without sharing
  * code and provides rapid determination of type equality.
  *
  * Types are interned by the Registry[Type] utility. This means that types share a single global name space.
  * Modules must cooperate on defining types in such a way that their names do not conflict.
  */
trait Type extends Registrable[Type] with Describable with BSONValidator[BSONValue] with Bootstrappable {
  def registry = Type

  type ScalaValueType

  /** The plural of the name of the type.
    * The name given to the type should be the singular form (Color not Colors) but things associated with this type
    * may wish to use the plural form. To ensure that everyone uses the same rules for pluralization,
    * we use our utility [[scrupal.utils.Pluralizer]] to make it consistent. This is lazy constructed so there's no
    * cost for it unless it gets used.
    */
  lazy val plural = Pluralizer.pluralize(label)

  def moduleOf = { Module.values.find(mod ⇒ mod.types.contains(this)) }

  /** The kind of this class is simply its simple class name. Each "kind" has a different information structure */
  def kind : Symbol = Symbol(super.getClass.getSimpleName.replace("$","_"))

  def trivial = false
  def nonTrivial = !trivial

  /** Validate value of BSON type B against Scrupal Type T
    *
    * @param value the BSONValue to be validated
    * @return None if there were no validation errors, otherwise Some(Seq[String]) containing the errors
    */
  def validate(value: BSONValue): Option[Seq[String]] = apply(value)

  /** Convert the BSON value B into the Scala value of type S
    *
    * This validates the BSONValue first and then attempts to apply a BSONReader in order to extract a value of type S
    * @param value
    * @return
    */
  def convert[S <: ScalaValueType](value: BSONValue)(implicit reader: BSONReader[BSONValue,S]) : Try[S] = {
    apply(value) match {
      case Some(error) => Failure[S](new ValidationError(asT, error, value))
      case None => reader.readTry(value)
    }
  }


  /** Convert the Scala value of type S into the BSON value of type B
    * After applying the BSONWriter this validates that what was written can be converted back to an S and
    * reports validation errors if anything is wrong.
    * @param value
    * @param writer
    * @return
    */
  def convert[S <: ScalaValueType](value: S)(implicit writer: BSONWriter[S,BSONValue]) : Try[BSONValue] = {
    writer.writeTry(value).flatMap { bson =>
      apply(bson) match {
        case Some(error) => Failure[BSONValue](new ValidationError(asT, error, bson))
        case None => Success[BSONValue](bson)
      }
    }
  }
}

case class Not_A_Type() extends Type {
  lazy val id = 'NotAType
  override val kind = 'NotAKind
  override val trivial = true
  val description = "Not A Type"
  val module = 'NotAModule
  def asT = this
  def apply(value: BSONValue) = Some(Seq("NotAType is not valid"))
}

case class UnfoundType(id: Symbol) extends Type {
  override val kind = 'Unfound
  override val trivial = true
  val description = "A type that was not loaded in memory"
  val module = 'NotAModule
  def asT = this
  def apply(value: BSONValue) =
    Some(Seq(
      s"Unfound type '${id.name}' cannot be used for validation. The module defining the type is not loaded."))
}


/** Abstract base class of Types that refer to another Type that is the element type of the compound type
  */
trait CompoundType extends Type {
  def elemType: Type
}

trait IndexableType extends CompoundType

trait DocumentType extends Type {
  def validatorFor(id: String) : Option[Type]
  def fieldNames: Iterable[String]
  def allowMissingFields : Boolean = false
  def allowExtraFields : Boolean = false

  def apply(value: BSONValue) : ValidationResult = {
    value match {
      case d: BSONDocument =>
        def doValidate(name: String, value: BSONValue) : Option[Seq[String]]= {
          validatorFor(name) match {
            case Some(validator) ⇒ validator.apply(value)
            case None ⇒ {
              if (!allowExtraFields)
                Some(Seq(s"Field '$name' is spurious."))
              else
              // Don't validate or anything, spurious field
                None
            }
          }
        }
        // Note: Stream this to a map up front because we're going to access everything so we might as well stream it once
        val elements = d.elements.toMap
        val errors = {
          val field_results = for (
            field ← elements ;
            result = doValidate(field._1, field._2) if result.isDefined ;
            x ← result
          ) yield x

          val missing_results = if (!allowMissingFields) {
            for (
              fieldName ← fieldNames if !elements.contains(fieldName)
            ) yield {
              Seq(s"Field '$fieldName' is missing")
            }
          } else {
            Seq.empty[Seq[String]]
          }

          (field_results ++ missing_results).flatten.toSeq
        }
        if (errors.isEmpty)
          None
        else
          Some(errors)
      case x: BSONValue =>
        wrongClass("BSONDocument", x).map { s => Seq(s) }
    }
  }

}


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

object AnyInteger_t
  extends RangeType('AnyInteger, "A type that accepts any integer value", Int.MinValue, Int.MaxValue)

object AnyReal_t
  extends RealType('AnyReal, "A type that accepts any double floating point value", Double.MinValue, Double.MaxValue)

object AnyTimestamp_t
  extends TimestampType('AnyTimestamp, "A type that accepts any timestamp value")

object Boolean_t extends BooleanType('TheBoolean, "A type that accepts true/false values")

object NonEmptyString_t
  extends StringType('NonEmptyString, "A type that accepts any string input except empty", ".+".r, 1024*1024)

/** The Scrupal Type for the identifier of things */
object Identifier_t
  extends StringType('Identifier, "Scrupal Identifier", anchored(Identifier), 64)

object Password_t extends
StringType('Password, "A type for human written passwords", anchored(Password), 64)

object Description_t
  extends StringType('Description, "Scrupal Description", anchored(Markdown), 1024)

object Markdown_t
  extends StringType('Markdown, "Markdown document type", anchored(Markdown))

/** Type Registry and companion */
object Type extends Registry[Type] {

  val registryName = "Types"
  val registrantsName = "type"

  /** Determine if a type is a certain kind
    * While Scrupal defines a useful set of types that will suffice for many needs,
    * Modules are free to create new kinds of types (i.e. subclass from Type itself,
    * not create a new instance of Type). Types have a "kind" field that allows them to be categorized roughly by the
    * nature of the subclass from Type. This method checks to see if a given type is a member of that category.
    * @param name The symbol for the type to check
    * @param kind The symbol for the kind that ```name``` should be
    * @return true iff ```name``` is of kind ```kind```
    */
  def isKindOf(name: Symbol, kind: Symbol) : Boolean = lookupOrElse(name,NotAType).kind == kind

  lazy val NotAType = Not_A_Type()

  def of(id: Identifier) : Type = {
    Type(id) match {
      case Some(typ) => typ
      case None => new UnfoundType(id)
    }
  }

  /** Handle reading/writing Type instances to and from BSON.
    * Note that types are a little special. We write them as strings and restore them via lookup. Types are intended
    * to only ever live in memory but they can be references in the database. So when a Type is a field of some
    * class that is stored in the database, what actually gets stored is just the name of the type.
    */
  class BSONHandlerForType[T <: Registrable[_]]  extends BSONHandler[BSONString,T] {
    override def write(t: T): BSONString = BSONString(t.id.name)
    override def read(bson: BSONString): T = Type.as(Symbol(bson.value))
  }
}

