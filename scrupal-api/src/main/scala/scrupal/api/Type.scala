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

import java.time.Instant
import java.util.regex.Pattern

import akka.http.scaladsl.model.{MediaType, MediaTypes}
import play.api.libs.json.{JsArray, JsObject, JsValue}
import scrupal.utils.Patterns._
import scrupal.utils.Validation._
import scrupal.utils.{Pluralizer, Registrable, Registry, Validation}
import shapeless.Poly1

import scala.collection.Map
import scala.concurrent.duration.Duration
import scala.language.postfixOps
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

case class TypeFailure[VT, T <: Type[VT]](ref : Location, value : VT, t : T, errors : String*)
  extends Validation.Failure[VT] {
  override def msgBldr : StringBuilder = {
    val s = super.msgBldr
    if (errors.isEmpty)
      s.append("Value does not conform to ").append(t.label).append("")
    else {
      for (err ← errors) { s.append(err).append(",\n") }
      s.deleteCharAt(s.length - 1)
      s.deleteCharAt(s.length - 1)
    }
  }
}

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
trait Type[VT] extends Registrable[Type[_]] with Describable with Validator[VT] with Bootstrappable {

  def registry : Registry[Type[_]] = Types

  type ValueType = VT

  /** The plural of the name of the type.
    * The name given to the type should be the singular form (Color not Colors) but things associated with this type
    * may wish to use the plural form. To ensure that everyone uses the same rules for pluralization,
    * we use our utility [[scrupal.utils.Pluralizer]] to make it consistent. This is lazy constructed so there's no
    * cost for it unless it gets used.
    */
  lazy val plural = Pluralizer.pluralize(label)

  /** The kind of this class is simply its simple class name. Each "kind" has a different information structure */
  def kind : Symbol = Symbol(super.getClass.getSimpleName.replace("$", "_"))

  def trivial = false
  def nonTrivial = !trivial

  override protected def simplify(
    ref : Location,
    value : VT,
    classes : String
  )(validator : (VT) ⇒ Option[String]) : VResult = {
      validator(value) match {
        case Some("") ⇒
          wrongClass(ref, value, classes)
        case Some(msg : String) ⇒
          TypeFailure(ref, value, this, msg)
        case None ⇒
          Validation.Success(ref, value)
      }
    }
}

/** The registry of Types for this Scrupal
  *
  * This object is the registry of Type objects. When a [[scrupal.api.Type]] is instantiated, it will register
  * itself with this object. The object is located in [[scrupal.api.Scrupal]]
  */
object Types extends Registry[Type[_]] {

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
  def isKindOf(name : Symbol, kind : Symbol) : Boolean = lookup(name).exists { k ⇒ k.kind == kind }

  def of(id : Identifier) : Type[_] = {
    this(id) match {
      case Some(typ) ⇒ typ
      case None ⇒ new UnfoundType(id)
    }
  }
}


case class UnfoundType(id : Symbol) extends Type[Boolean] {
  override val kind = 'Unfound
  override val trivial = true
  val description = "A type that was not loaded in memory"
  val module = 'NotAModule
  def validate(ref : Location, value : Boolean) =
    TypeFailure(ref, value, this,
      s"Unfound type '${id.name}' cannot be used for validation. The module defining the type is not loaded.")
}

/** Abstract base class of Types that refer to another Type that is an indexable element of the type */

trait IndexableType[ET, ST] extends Type[ST] with SeqValidator[ET, ST]{
  def elemType : Type[ET]
}

trait JsArrayType extends IndexableType[JsValue, JsArray] {
  def toSeq(st : JsArray) : Seq[JsValue] = st.value
  def validateElement(ref : IndexedLocation, v : JsValue) : Results[JsValue] = {
    elemType.validate(ref, v)
  }
}

trait MapableType[KT, ET, ST] extends Type[ST] with MapValidator[KT, ET, ST] {
  def elemType: Type[ET]
}

trait JsObjectType extends MapableType[String,JsValue,JsObject] {
  override def toMap(mt: JsObject): Map[String, JsValue] = mt.value
  override def validateElement(ref: SelectedLocation[String], k: String, v: JsValue): Results[JsValue] = {
    elemType.validate(ref, v)
  }
}

/** A generic Document Type
  * Subclasses of this type represent types have named fields of a specific type
  *
  * @tparam ET The Element Type
  * @tparam MT The Document Type
  */
trait DocumentType[KT, ET, MT] extends Type[MT] with MapValidator[KT, ET, MT] {
  def validatorFor(id : KT) : Option[Type[_]]
  def fieldNames : Iterable[KT]
  def allowMissingFields : Boolean = false
  def allowExtraFields : Boolean = false
  def toMap(mt : MT) : scala.collection.Map[KT, ET]

  def validateElement(ref : SelectedLocation[KT], k: KT, v : ET) : Results[ET] = {
    validatorFor(k) match {
      case Some(validator) ⇒
        validator.asInstanceOf[Validator[ET]].validate(ref, v)
      case None ⇒ {
        if (!allowExtraFields)
          StringFailure[ET](ref, v, s"Field '$k' is spurious.")
        else
        // Don't validate or anything, spurious field
          Validation.Success(ref, v)
      }
    }
  }

  override def validate(ref : Location, value : MT) : Results[MT] = {
    super.validate(ref, value) match {
      case x: Validation.Failure[VResult] @unchecked ⇒ x
      case r: Validation.Success[VResult] @unchecked ⇒
        val elements = toMap(value) // Collect the values only once in case there is a cost for traversing it
      val missing_results: Iterable[Validation.Failure[ET]] = if (!allowMissingFields) {
          for (
            fieldName ← fieldNames if !elements.contains(fieldName)
          ) yield {
            StringFailure(ref.select(fieldName), null.asInstanceOf[ET], s"Field '$fieldName' is missing")
          }
        } else {
          Iterable.empty[Validation.Failure[ET]]
        }
        if (missing_results.isEmpty)
          Validation.Success(ref, value)
        else
          Failures(ref, value, missing_results.toSeq: _*)
    }
  }
}

trait StructuredType[ET] extends DocumentType[String, ET, Map[String, ET]] {
  override type ValueType = Map[String, ET]
  def fields : Map[String, Type[_]]
  def validatorFor(id : String) : Option[Type[_]] = fields.get(id)
  def fieldNames : Iterable[String] = fields.keys
  def size = fields.size
  def toMap(mt : Map[String, ET]) : Map[String, ET] = mt
}


/** A type that can take any value and always validates successfully */
case class AnyType(
  id : Identifier,
  description : String) extends Type[Any] {
  def validate(ref : Location, value : Any) : VResult = Validation.Success(ref, value)
  override def kind = 'Any
  override def trivial = true
}

object AnyType_t extends AnyType('Any, "A type that accepts any value")

/** A BLOB type has a specified MIME content type and a minimum and maximum length
  *
  * @param id
  * @param description
  * @param mediaType
  * @param maxLen
  */
case class BLOBType(
  id : Identifier,
  description : String,
  mediaType : MediaType,
  maxLen : Long = Long.MaxValue) extends Type[Array[Byte]] {
  assert(maxLen >= 0)
  def validate(ref : Location, value : Array[Byte]) : VResult = {
    simplify(ref, value, "Array[Byte]") {
      case b: Array[Byte] if b.length > maxLen ⇒ Some(s"BLOB of length ${b.length} exceeds maximum length of $maxLen")
      case b: Array[Byte] ⇒ None
      case _ ⇒ Some("")
    }
  }
  override def kind = 'BLOB

}

object BooleanType {
  def verity = List("true", "on", "yes", "confirmed")
  def falsity = List("false", "off", "no", "denied")
}

case class BooleanType(
  id : Identifier,
  description : String
) extends Type[Atom] {
  override def kind = 'Boolean

  private object validation extends Poly1 {
    def check[T](value: T): Option[String] = {
      if (value == 0 || value == 1)
        None
      else
        Some(s"Value '$value' could not be converted to boolean (0 or 1 required")
    }
    def checkString(s: String) : Option[String] = {
      import BooleanType._
      if (verity.contains(s)) None
      else if (falsity.contains(s)) None
      else Some(s"Value '$s' could not be interpreted as a boolean")
    }


    implicit def caseBoolean = at[Boolean] { b: Boolean ⇒ None}

    implicit def caseByte = at[Byte] { b: Byte ⇒ check(b) }

    implicit def caseShort = at[Short] { s: Short ⇒ check(s) }

    implicit def caseInt = at[Int] { i: Int ⇒ check(i) }

    implicit def caseLong = at[Long] { l: Long ⇒ check(l) }

    implicit def caseFloat = at[Float] { f: Float ⇒ check(f) }

    implicit def caseDouble = at[Double] { d: Double ⇒ check(d) }

    implicit def caseSymbol = at[Symbol] { s : Symbol ⇒ checkString(s.name) }

    implicit def caseString = at[String] { s ⇒ checkString(s) }

    implicit def caseInstant = at[Instant] { i : Instant ⇒ check(i.toEpochMilli, i) }

    implicit def caseDuration = at[Duration] { i : Duration ⇒ check(i.toMillis, i)  }

  }
  def validate(ref : Location, value : Atom) : VResult = {
    simplify(ref, value, "Boolean, Integer, Long, or String") { v  ⇒
      val mapped = v.map(validation)
      mapped.select[Option[String]].flatten
    }
  }
}

object Boolean_t extends BooleanType('TheBoolean, "A type that accepts true/false values")

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
case class BundleType(
  id : Identifier,
  description : String,
  fields : Map[String, Type[_]]
) extends StructuredType[Type[_]] {
  override def kind = 'Bundle
}

object BundleType {
  val empty : BundleType = BundleType('Empty, "An empty bundle type", Map.empty[String,Type[_]])
}

/** The Scrupal Type for information about Sites */
object SiteInfo_t
  extends BundleType('SiteInfo, "Basic information about a site that Scrupal will serve.",
    fields = Map(
      "name" -> Identifier_t,
      "title" -> Identifier_t,
      "domain" -> DomainName_t,
      "port" -> TcpPort_t,
      "admin_email" -> EmailAddress_t,
      "copyright" -> Identifier_t
    )
  )


/** An Enum type allows a selection of one enumerator from a list of enumerators.
  * Each enumerator is assigned an integer value.
  */
case class EnumType(
  id : Identifier,
  description : String,
  enumerators : Map[Identifier, Int]
) extends Type[Atom] {
  require(enumerators.nonEmpty)
  override def kind = 'Enum

  def valueOf(enum : Symbol) = enumerators.get(enum)
  def valueOf(enum : String) = enumerators.get(Symbol(enum))

  private object validator extends Poly1 {
    def check[T](value: Int, orig: T): Option[String] = {
      if (!enumerators.exists { case (key,v) ⇒ v == value } )
        Some(s"Value $orig ($value) is not valid for enumeration '${id.name}'")
      else
        None
    }

    def check(s: Symbol) : Option[String] = {
      if (!enumerators.contains(s))
        Some(s"Value '${id.name}' is not valid for enumeration '${id.name}")
      else
        None
    }

    implicit def caseBoolean = at[Boolean] { b: Boolean ⇒ check(if (b) 1 else 0, b) }

    implicit def caseByte = at[Byte] { b: Byte ⇒ check(b.toInt, b) }

    implicit def caseShort = at[Short] { s: Short ⇒ check(s.toInt, s) }

    implicit def caseInt = at[Int] { i: Int ⇒ check(i, i) }

    implicit def caseLong = at[Long] { l: Long ⇒ check(l.toInt, l) }

    implicit def caseFloat = at[Float] { f: Float ⇒ check(f.toInt, f) }

    implicit def caseDouble = at[Double] { d: Double ⇒ check(d.toInt, d) }

    implicit def caseSymbol = at[Symbol] { s : Symbol ⇒ check(s) }

    implicit def caseString = at[String] { s : String ⇒ check(Symbol(s)) }

    implicit def caseInstant = at[Instant] { i : Instant ⇒ Some(s"Instant values are not compatible with enums") }

    implicit def caseDuration = at[Duration] { i : Duration ⇒ Some(s"Duration values are not compatible with enums") }
  }

  def validate(ref : Location, value : Atom) = {
    simplify(ref, value, "Atom") { value ⇒
      value.map(validator).unify
    }
  }
}

/** A Homogenous List type allows a non-exclusive list of elements of other types to be constructed
  *
  * @param id
  * @param description
  * @param elemType
  */
case class ListType[EType](
  id : Identifier,
  description : String,
  elemType : Type[EType]
) extends IndexableType[EType, Seq[EType]] {
  override def kind = 'List
  def toSeq(st : Seq[EType]) : Seq[EType] = st
  def validateElement(ref : IndexedLocation, v : EType) : Results[EType]= {
    elemType.validate(ref, v)
  }
}


/** A Map is a set whose elements are named with an arbitrary string
  *
  * @param id The unique identifier for this type
  * @param description A description for this type
  * @param fieldNames The names of the fields of this type
  * @param elemType The types of the elements of this type
  * @tparam ET The element type
  */
case class MapType[ET](
  override val id : Identifier,
  description : String,
  override val fieldNames: Seq[String],
  elemType : Type[ET]
) extends StructuredType[ET] {
  override def kind = 'Map

  def fields : Map[String, Type[_]] = fieldNames.map { name ⇒ name → elemType } toMap

  override def validatorFor(id: String): Option[Type[ET]] = Some(elemType)
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
case class NodeType[R](
  id : Identifier,
  description : String,
  fields : Map[String, Type[R]],
  mediaType : MediaType = MediaTypes.`text/html`
) extends StructuredType[R] {
  override def kind = 'Node
}


/** A Range type constrains Long Integers between a minimum and maximum value
  *
  * @param id
  * @param description
  * @param min
  * @param max
  */
case class RangeType(
  id : Identifier,
  description : String,
  min : Long = Long.MinValue,
  max : Long = Long.MaxValue
) extends Type[Atom] {
  require(min <= max)

  override def kind = 'Range

  private object validation extends Poly1 {
    def check[T](value: Long, orig: T): Option[String] = {
      if (value < min)
        Some(s"Value $orig ($value) is out of range, below minimum of $min")
      else if (value > max)
        Some(s"Value $orig ($value) is out of range, above maximum of $max")
      else
        None
    }

    implicit def caseBoolean = at[Boolean] { b: Boolean ⇒ check(if (b) 1 else 0, b) }
    implicit def caseByte = at[Byte] { b: Byte ⇒ check(b.toLong, b) }
    implicit def caseShort = at[Short] { s: Short ⇒ check(s.toLong, s) }
    implicit def caseInt = at[Int] { i: Int ⇒ check(i.toLong, i) }
    implicit def caseLong = at[Long] { l: Long ⇒ check(l, l) }
    implicit def caseFloat = at[Float] { f: Float ⇒ check(f.toLong, f) }
    implicit def caseDouble = at[Double] { d: Double ⇒ check(d.toLong, d) }
    implicit def caseSymbol = at[Symbol] { s : Symbol ⇒ checkString(s.name) }
    implicit def caseString = at[String] { s : String ⇒ checkString(s) }
    implicit def caseInstant = at[Instant] { i : Instant ⇒ check(i.toEpochMilli, i) }
    implicit def caseDuration = at[Duration] { i : Duration ⇒ check(i.toMillis, i)  }

    def checkString(s: String) : Option[String] = {
      try {
        check(s.toLong, s)
      } catch {
        case x: Throwable ⇒
          Some(s"Value '$s' is not convertible to a number: ${x.getClass.getSimpleName}: ${x.getMessage}")
      }
    }
  }

  def validate(ref : Location, value : Atom) : VResult = {
    simplify(ref, value, "Atom") { value ⇒
      value.map(validation).unify
    }
  }

}



object AnyInteger_t extends RangeType('AnyInteger,
  "A type that accepts any integer value", Int.MinValue, Int.MaxValue)

/** The Scrupal Type for TCP port numbers */
object TcpPort_t extends RangeType('TcpPort,
  "A type for TCP port numbers", 1, 65535) {
}

/** A Real type constrains Double values between a minimum and maximum value
  *
  * @param id
  * @param description
  * @param min
  * @param max
  */
case class RealType(
  id : Identifier,
  description : String,
  min : Double = Double.MinValue,
  max : Double = Double.MaxValue
) extends Type[Atom] {
  require(min <= max)

  override def kind = 'Real

  private object validation extends Poly1 {
    def check[T](value: Double, orig: T): Option[String] = {
      if (value < min)
        Some(s"Value $orig ($value) is out of range, below minimum of $min")
      else if (value > max)
        Some(s"Value $orig ($value) is out of range, above maximum of $max")
      else
        None
    }

    implicit def caseBoolean = at[Boolean] { b: Boolean ⇒ check(if (b) 1.0D else 0.0D, b) }
    implicit def caseByte = at[Byte] { b: Byte ⇒ check(b.toDouble, b) }
    implicit def caseShort = at[Short] { s: Short ⇒ check(s.toDouble, s) }
    implicit def caseInt = at[Int] { i: Int ⇒ check(i.toDouble, i) }
    implicit def caseLong = at[Long] { l: Long ⇒ check(l.toDouble, l) }
    implicit def caseFloat = at[Float] { f: Float ⇒ check(f.toDouble, f) }
    implicit def caseDouble = at[Double] { d: Double ⇒ check(d, d) }
    implicit def caseSymbol = at[Symbol] { s : Symbol ⇒ checkString(s.name) }
    implicit def caseString = at[String] { s : String ⇒ checkString(s) }
    implicit def caseInstant = at[Instant] { i : Instant ⇒ check(i.toEpochMilli.toDouble, i) }
    implicit def caseDuration = at[Duration] { i : Duration ⇒ check(i.toMillis.toDouble, i)  }

    def checkString(s: String) : Option[String] = {
      try {
        check(s.toLong, s)
      } catch {
        case x: Throwable ⇒
          Some(s"Value '$s' is not convertible to a number: ${x.getClass.getSimpleName}: ${x.getMessage}")
      }
    }
  }

  def validate(ref : Location, value : Atom) : VResult = {
    simplify(ref, value, "Double, Long or Integer") { value ⇒
      value.map(validation).unify
    }
  }
}


object AnyReal_t extends RealType('AnyReal,
  "A type that accepts any double floating point value", Double.MinValue, Double.MaxValue)


/** A type for a regular expression
  *
  * @param id The name of the Regex type
  * @param description A brief description of the regex type
  */
case class RegexType(
  id : Identifier,
  description : String
  ) extends Type[Atom] {
  override def kind = 'Regex

  private object validation extends Poly1 {
    def check(value: String): Option[String] = {
      value match {
        case s: String ⇒ Try {
          Pattern.compile(s)
        } match {
          case Success(x) ⇒ None
          case Failure(x) ⇒ Some(s"Error in pattern '$value': ${x.getClass.getName}: ${x.getMessage}")
        }
      }
    }

    implicit def caseBoolean = at[Boolean] { b: Boolean ⇒ check(b.toString) }

    implicit def caseByte = at[Byte] { b: Byte ⇒ check(b.toString) }

    implicit def caseShort = at[Short] { s: Short ⇒ check(s.toString) }

    implicit def caseInt = at[Int] { i: Int ⇒ check(i.toString) }

    implicit def caseLong = at[Long] { l: Long ⇒ check(l.toString) }

    implicit def caseFloat = at[Float] { f: Float ⇒ check(f.toString) }

    implicit def caseDouble = at[Double] { d: Double ⇒ check(d.toString) }

    implicit def caseSymbol = at[Symbol] { s: Symbol ⇒ check(s.name) }

    implicit def caseString = at[String] { s: String ⇒ check(s) }

    implicit def caseInstant = at[Instant] { i: Instant ⇒ check(i.toEpochMilli.toString) }

    implicit def caseDuration = at[Duration] { i: Duration ⇒ check(i.toMillis.toString) }
  }

  def validate(ref: Location, value: Atom): VResult = {
    simplify(ref, value, "Double, Long or Integer") { value ⇒
      value.map(validation).unify
    }
  }
}

object Regex_t extends RegexType('Regex,
  "Regular expression type")


case class SelectionType(
  id : Identifier,
  description : String,
  choices : Seq[String]
  ) extends Type[Atom] {
  override def kind = 'Selection
  require(choices.nonEmpty)

  private object validation extends Poly1 {
    def check(s: String): Option[String] = {
      if (!choices.contains(s))
        Some(s"Invalid selection. Options are: ${choices.mkString(", ")}")
      else
        None
    }

    implicit def caseBoolean = at[Boolean] { b: Boolean ⇒ check(b.toString) }

    implicit def caseByte = at[Byte] { b: Byte ⇒ check(b.toString) }

    implicit def caseShort = at[Short] { s: Short ⇒ check(s.toString) }

    implicit def caseInt = at[Int] { i: Int ⇒ check(i.toString) }

    implicit def caseLong = at[Long] { l: Long ⇒ check(l.toString) }

    implicit def caseFloat = at[Float] { f: Float ⇒ check(f.toString) }

    implicit def caseDouble = at[Double] { d: Double ⇒ check(d.toString) }

    implicit def caseSymbol = at[Symbol] { s: Symbol ⇒ check(s.name) }

    implicit def caseString = at[String] { s: String ⇒ check(s) }

    implicit def caseInstant = at[Instant] { i: Instant ⇒ check(i.toEpochMilli.toString) }

    implicit def caseDuration = at[Duration] { i: Duration ⇒ check(i.toMillis.toString) }
  }

  def validate(ref: Location, value: Atom): VResult = {
    simplify(ref, value, "Atom") { value ⇒
      value.map(validation).unify
    }
  }
}


object UnspecificQuantity_t extends SelectionType('UnspecificQuantity,
  "A simple choice of quantities that do not specifically designate a number",
  Seq("None", "Some", "Any", "Both", "Few", "Several", "Most", "Many", "All")
)

/** A Set type allows an exclusive Set of elements of other types to be constructed
  *
  * @param id
  * @param description
  * @param elemType
  */
case class SetType[ET](
  id : Identifier,
  description : String,
  elemType : Type[ET]
) extends IndexableType[ET,Set[ET]] {
  override def kind = 'Set
  def toSeq(st: Set[ET]): Seq[ET] = st.toSeq
  def validateElement(ref: IndexedLocation, v: ET): Results[ET] = {
    elemType.validate(ref, v)
  }
}


/** A String type constrains a string by defining its content with a regular expression and a maximum length.
  *
  * @param id THe name of the string type
  * @param description A brief description of the string type
  * @param regex The regular expression that specifies legal values for the string type
  * @param maxLen The maximum length of this string type
  */
case class StringType(
  id : Identifier,
  description : String,
  regex : Regex,
  maxLen : Int = Int.MaxValue,
  minLen : Int = 0,
  patternName : String = "pattern"
  ) extends Type[Atom] {
  override def kind = 'String
  require(maxLen >= 0)

  protected def check(s: String): Option[String] = {
    if (s.length > maxLen) {
      Some(s"String of length ${s.length} exceeds maximum of $maxLen.")
    } else if (s.length < minLen) {
      Some(s"String of length ${s.length} is shorter than minimum of $minLen")
    } else if (!regex.pattern.matcher(s).matches()) {
      Some(s"'$s' does not match $patternName.")
    } else
      None
  }

  protected object validation extends Poly1 {
    implicit def caseBoolean = at[Boolean] { b: Boolean ⇒ check(b.toString) }

    implicit def caseByte = at[Byte] { b: Byte ⇒ check(b.toString) }

    implicit def caseShort = at[Short] { s: Short ⇒ check(s.toString) }

    implicit def caseInt = at[Int] { i: Int ⇒ check(i.toString) }

    implicit def caseLong = at[Long] { l: Long ⇒ check(l.toString) }

    implicit def caseFloat = at[Float] { f: Float ⇒ check(f.toString) }

    implicit def caseDouble = at[Double] { d: Double ⇒ check(d.toString) }

    implicit def caseSymbol = at[Symbol] { s: Symbol ⇒ check(s.name) }

    implicit def caseString = at[String] { s: String ⇒ check(s) }

    implicit def caseInstant = at[Instant] { i: Instant ⇒ check(i.toEpochMilli.toString) }

    implicit def caseDuration = at[Duration] { i: Duration ⇒ check(i.toMillis.toString) }
  }

  def validate(ref: Location, value: Atom): VResult = {
    simplify(ref, value, "Atom") { value ⇒
      value.map(validation).unify
    }
  }

}



object AnyString_t extends StringType('AnyString,
  "A type that accepts any string input", ".*".r, 1024 * 1024)

object NonEmptyString_t extends StringType('NonEmptyString,
  "A type that accepts any string input except empty", ".+".r, 1024 * 1024)

/** The Scrupal Type for the identifier of things */
object Identifier_t extends StringType('Identifier,
  "Scrupal Identifier", anchored(Identifier), 64, 1, "an identifier")

object Password_t extends StringType('Password,
  "A type for human written passwords", anchored(Password), 64, 6, "a password") {
  override def check(bs: String) = {
    if (bs.length > maxLen)
      Some(s"Value is too short for a password.")
    else if (!regex.pattern.matcher(bs).matches())
      Some(s"Value is not legal for a password.")
    else
        None
  }
}

object Description_t extends StringType('Description,
  "Scrupal Description", anchored(".+".r), 1024, 10, "a description")

object Markdown_t extends StringType('Markdown,
  "Markdown document type", anchored(oneOrMore(Markdown)), patternName = "markdown formatting")

/** The Scrupal Type for domain names per  RFC 1035, RFC 1123, and RFC 2181 */
object DomainName_t extends StringType('DomainName,
  "RFC compliant Domain Name", anchored(DomainName), 253, 3, "a domain name")

/** The Scrupal Type for Uniform Resource Locators.
  * We should probably have one for URIs too,  per http://tools.ietf.org/html/rfc3986
  */
object URL_t extends StringType('URL,
  "Uniform Resource Locator", anchored(UniformResourceLocator))

/** The Scrupal Type for IP version 4 addresses */
object IPv4Address_t extends StringType('IPv4Address,
  "A type for IP v4 Addresses", anchored(IPv4Address), 15, 7, "an IPv4 address")

/** The Scrupal Type for Email addresses */
object EmailAddress_t extends StringType('EmailAddress,
  "An email address", anchored(EmailAddress), 253, 7, "an e-mail address")

object LegalName_t extends StringType('LegalName,
  "The name of a person or corporate entity", anchored(LegalName), 128, 2, "a legal name.")

object Title_t extends StringType('Title,
  "A string that is valid for a page title", anchored(Title), 70, 3, "a page title")

/** A point-in-time value between a minimum and maximum time point
  *
  * @param id
  * @param description
  * @param min
  * @param max
  */
case class TimestampType(
  id : Identifier,
  description : String,
  min : Instant = Instant.ofEpochMilli(0L),
  max : Instant = Instant.ofEpochMilli(Long.MaxValue / 2)
  ) extends Type[Atom] {
  assert(min.toEpochMilli <= max.toEpochMilli)

  private object validation extends Poly1 {
    def check(l: Long): Option[String] = {
      if (l < min.toEpochMilli)
        Some(s"Timestamp $l is out of range, below minimum of $min")
      else if (l > max.toEpochMilli)
        Some(s"Timestamp $l is out of range, above maximum of $max")
      else
        None
    }

    implicit def caseBoolean = at[Boolean] { b: Boolean ⇒ Some("") }

    implicit def caseByte = at[Byte] { b: Byte ⇒ check(b.toLong) }

    implicit def caseShort = at[Short] { s: Short ⇒ check(s.toLong) }

    implicit def caseInt = at[Int] { i: Int ⇒ check(i.toLong) }

    implicit def caseLong = at[Long] { l: Long ⇒ check(l.toLong) }

    implicit def caseFloat = at[Float] { f: Float ⇒ check(f.toLong) }

    implicit def caseDouble = at[Double] { d: Double ⇒ check(d.toLong) }

    implicit def caseSymbol = at[Symbol] { s: Symbol ⇒ check(s.name.toLong) }

    implicit def caseString = at[String] { s: String ⇒ check(s.toLong) }

    implicit def caseInstant = at[Instant] { i: Instant ⇒ check(i.toEpochMilli) }

    implicit def caseDuration = at[Duration] { i: Duration ⇒ check(i.toMillis) }
  }

  def validate(ref: Location, value: Atom): VResult = {
    simplify(ref, value, "Atom") { value ⇒
      value.map(validation).unify
    }
  }
}

object AnyTimestamp_t extends TimestampType('AnyTimestamp,
  "A type that accepts any timestamp value")

