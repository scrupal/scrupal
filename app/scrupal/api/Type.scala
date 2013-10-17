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

import scala.util.matching.Regex
import scala.collection.immutable.HashMap
import play.api.libs.json._
import play.api.data.validation.ValidationError
import java.net.{URI, URISyntaxException}
import scrupal.utils._
import scala.collection.mutable
import play.api.libs.json.JsArray
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsString
import scala.Some
import play.api.data.validation.ValidationError
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject


/** A trait to define the validate method for objects that can validate a JsValue */
trait ValueValidator {
  def validate(value : JsValue) : JsResult[Boolean] = {
    value match {
      case JsNull => JsSuccess(true) // JsNull is always valid
      case x: JsUndefined => JsError("JsUndefined can never validate successfully.")
      case x: JsValue => JsError("Unexpected JsValue type: " + x.getClass.getSimpleName )
    }
  }
}

/** Validation trait for JsArray
  * Several of the types defined below expect a JsArray with a particular element type. This trait just provides the
  * method to validate the JsArray against the expected element type.
  */
trait ArrayValidator {

  /** The validation method for validating a JsArray
    * This traverses the array and validates that each element conforms to the `elemType`
    * @param value The JsArray to be validated
    * @param elemType The Type each element of the array should have. By default
    * @return JsSuccess(true) when valid, JsError otherwise
    */
  def validate(value : JsArray, elemType: Type) : JsResult[Boolean] = {
    /// TODO: Improve this to collect and return all the validation issues
    if (value.value exists { elem => !elemType.validate(elem).asOpt.isDefined } )
      JsError("Not all elements match element type")
    else JsSuccess(true)
  }
}

/** Validation trait for JsObject
  * Several of the types defined below expect a JsObject with a particular structure to them. This trait just
  * provides the method to validate the JsObject against a Map of what is expected in that JsObject.
  */
trait ObjectValidator {
  /** The validation method for validating a JsObject
    * This traverses a JsObject and for each field, looks up the associated type in the "structure" and validates that
    * field with the type.
    * @return JsSuccess(true) if all the fields of the JsObject value validate correctly
    */
   def validate( value: JsObject, structure: Map[Symbol,Type]) : JsResult[Boolean] = {
    if (value.value exists {
      case (key,data) => !(structure.contains(Symbol(key)) &&
        structure.get(Symbol(key)).get.validate(data).asOpt.isDefined)
    })
      JsError(JsPath(), ValidationError("Not all elements validate"))
    else
      JsSuccess(true)
  }
}

case class EssentialType (
  override val id : TypeIdentifier,
  description: String,
  moduleId: ModuleIdentifier
) extends Registrable

/** A generic Type used as a placeholder for subclasses that compose types.
  * Note that the name of the Type is a Symbol. Symbols are interned so there is only ever one copy of the name of
  * the type. This is important because type linkage is done by indirectly referencing the name, not the actual type
  * object and name reference equality is the same as type equality.  Scrupal also interns the Type objects that
  * modules provide so they can be looked up by name quickly. This allows inter-module integration without sharing
  * code and provides rapid determination of type equality.
  * @param id
  * @param description
  * @param moduleId
  */
abstract class Type (
  id : TypeIdentifier,
  description: String,
  moduleId: ModuleIdentifier
) extends EssentialType(id, description, moduleId) with ValueValidator  with Jsonic {
  require(!label.isEmpty)
  require(!description.isEmpty)
  require(!moduleId.name.isEmpty)

  /** By default the validate method returns an error
    * @param value The JSON value to be validated
    * @return JsError, always -- subclasses should override
    */
  override def validate(value : JsValue) : JsResult[Boolean] = JsError("Unimplemented validator.")

  /** The plural of the name of the type.
    * The name given to the type should be the singular form (Color not Colors) but things associated with this type
    * may wish to use the plural form. To ensure that everyone uses the same rules for pluralization,
    * we use our utility [[scrupal.util.Pluralizer]] to make it consistent. This is lazy constructed so there's no
    * cost for it unless it gets used.
    */
  lazy val plural = Pluralizer.pluralize(label)

  /** The kind of this class is simply its simple class name. Each "kind" has a different information structure */
  def kind :Symbol = Symbol(super.getClass.getSimpleName.replace("$",""))

  override def toJson : JsObject = ???
  override def fromJson( js: JsObject ) = ???

  /** Register this type with the registry of types (below) */
  Type.register(this)

}

/** The type for indirectly referencing another value of a specific type.
  * By default, all nested types are stored in JSON by value. In this way, complex data structures can be created and
  * validated by composing the various subclasses of Type --- with one exception, this `ReferenceType` class. This
  * Type refers to some other value stored outside of the object in which the `ReferenceType` occurs.
  * @param id The name of the reference type
  * @param description A description of the reference type
  * @param typ The type of the value to which the this object refers
  */
class ReferenceType (
  id : TypeIdentifier,
  description: String,
  moduleId: ModuleIdentifier,
  val typ : Type
) extends Type(id, description, moduleId) {
  override def validate(value : JsValue) = {
    value match {
      case v : JsObject => {
        val map = v.value
        if (map.size != 1)
          JsError("Reference object should only have one entry.")
        else {
          val (typ,id) = map.head
          // TODO: validate that typ is a symbol/identifier legal for types and id is correct for an identifier
          JsSuccess(true)
        }
      }
      case x => super.validate(value)
    }
  }

  override def kind = 'Reference

  override def toJson = Json.obj(
      "kind" -> kind.name,
      "name" -> label,
      "plural" -> plural,
      "description" -> description,
      "type" -> typ.label
    )
}

/** A String type constrains a string by defining its content with a regular expression and a maximum length.
  *
  * @param id THe name of the string type
  * @param description A brief expression of the string type
  * @param regex The regular expression that specifies legal values for the string type
  * @param maxLen The maximum length of this string type
  */
class StringType (
  id : TypeIdentifier,
  description : String,
  moduleId: ModuleIdentifier,
  val regex : Regex,
  val maxLen : Int = Int.MaxValue
) extends Type(id, description, moduleId) {
  require(maxLen >= 0)

  override def validate(value : JsValue) = {
    value match {
      case v: JsString => {
        if (v.value.length <= maxLen && regex.findFirstIn(v.value).isDefined) JsSuccess(true)
        else JsError("Value does not match pattern " + regex.pattern.pattern())
      }
      case x => super.validate(value)
    }
  }

  override def kind = 'String

  override def toJson = Json.obj(
    "kind" -> kind.name,
    "name" -> label,
    "plural" -> plural,
    "description" -> description,
    "regex" -> regex.pattern.pattern,
    "maxLen" -> maxLen
  )
}

/** A Range type constrains Long Integers between a minimum and maximum value
  *
  * @param id
  * @param description
  * @param min
  * @param max
  */
class RangeType (
  id : TypeIdentifier,
  description : String,
  moduleId: ModuleIdentifier,
  val min : Long = Int.MinValue,
  val max : Long = Int.MaxValue
) extends Type(id, description, moduleId) {

  override def validate(value : JsValue) = {
    value match {
      case v: JsNumber => {
        if (v.value < min) JsError("Value smaller than minimum: " + min)
        else if (v.value > max) JsError("Value greater than maximum: " + max)
        else JsSuccess(true)
      }
      case x => super.validate(value)
    }
  }
  override def kind = 'Range

  override def toJson = Json.obj(
    "kind" -> kind.name,
    "name" -> label,
    "plural" -> plural,
    "description" -> description,
    "min" -> min,
    "max" -> max
  )
}

/** A Real type constrains Double values between a minimum and maximum value
  *
  * @param id
  * @param description
  * @param min
  * @param max
  */
class RealType (
  id : TypeIdentifier,
  description : String,
  moduleId: ModuleIdentifier,
  val min : Double = Double.MinValue,
  val max : Double = Double.MaxValue
) extends Type(id, description, moduleId) {

  override def validate(value : JsValue) = {
    value match {
      case v: JsNumber => {
        if (v.value < min) JsError("Value smaller than minimum: " + min)
        else if (v.value > max) JsError("Value greater than maximum: " + max)
        else JsSuccess(true)
      }
      case x => super.validate(value)
    }
  }

  override def kind = 'Real

  override def toJson = Json.obj(
    "kind" -> kind.name,
    "name" -> label,
    "plural" -> plural,
    "description" -> description,
    "min" -> min,
    "max" -> max
  )
}

/** A BLOB type has a specified MIME content type and a minimum and maximum length
  *
  * @param id
  * @param description
  * @param mime
  * @param minLen
  * @param maxLen
  */
class BLOBType  (
  id : TypeIdentifier,
  description : String,
  moduleId: ModuleIdentifier,
  val mime : String,
  val minLen : Int = 0,
  val maxLen : Long = Long.MaxValue
) extends Type(id, description, moduleId) {
  assert(minLen >= 0)
  assert(maxLen >= 0)
  assert(mime.contains("/"))

  /** Validation of BLOBs is done via indirection.
    * JSON doesn't permit (efficient) representation of BLOBs so instead of attempting to place the BLOB in the JSON
    * we simply put a URI to the blob in the JSON. This has the benefit of allowing the BLOB loading to occur
    * asynchronously and in a binary manner.
    * @param value The JsString containing the URI of the BLOB
    * @return JsSuccess(true) if the BLOB validates correctly, JsError otherwise
    */
  override def validate(value: JsValue) : JsResult[Boolean] = {
    value match {
      case s: JsString => try { validate(new URI(s.value)) }
        catch { case x: URISyntaxException => JsError(ValidationError("URI Syntax Error: " + x.getMessage())) }
      case x => super.validate(value)
    }
  }

  /** Subclass validator for BLOBs
    * Subclasses are expected to implement this method to validate the content of the BLOB at the provided `uri`. This
    * base implementation just return success which is a suitable assumption for many blobs. For others,
    * its not. The default implementation makes it possible to instantiate this case class instead of it being
    * abstract.
    * @param uri The Uniform Resource Indicator where the BLOB can be loaded from.
    * @return JsSuccess(true)
    */
  def validate(uri: URI) : JsResult[Boolean] = JsSuccess(true)

  override def kind = 'BLOB

  override def toJson = Json.obj(
    "kind" -> kind.name,
    "name" -> label,
    "plural" -> plural,
    "description" -> description,
    "mine" -> mime,
    "minLen" -> minLen,
    "axnLen" -> maxLen
  )
}

/** An Enum type allows a selection of one enumerator from a list of enumerators.
  * Each enumerator is assigned an integer value.
  */
class EnumType  (
  id : TypeIdentifier,
  description : String,
  moduleId: ModuleIdentifier,
  val enumerators : HashMap[Symbol, Int]
) extends Type(id, description, moduleId) {
  require(!enumerators.isEmpty)

  override def validate(value : JsValue) = {
    value match {
      case v: JsString => {
        if (enumerators.contains( Symbol(v.value) )) JsSuccess(true)
        else JsError("Invalid enumeration value")
      }
      case x => super.validate(value)
    }
  }
  def valueOf(enum: Symbol) = enumerators.get(enum)
  def valueOf(enum: String) = enumerators.get(Symbol(enum))

  override def kind = 'Enum

  override def toJson = {
    Json.obj(
      "kind" -> kind.name,
      "name" -> label,
      "plural" -> plural,
      "description" -> description,
      "enumerators" -> (enumerators map { case (name:Symbol, value: Int) => (name.name, value) })
    )
  }
}

/** Abstract base class of Types that refer to another Type that is the element type of the compound type
  *
  * @param id
  * @param description
  * @param elemType
  */
abstract class CompoundType (
  id : TypeIdentifier,
  description : String,
  moduleId: ModuleIdentifier,
  val elemType : Type
) extends Type(id, description, moduleId)

/** A List type allows a non-exclusive list of elements of other types to be constructed
  *
  * @param id
  * @param description
  * @param elemType
  */
class ListType  (
  override val id : TypeIdentifier,
  override val description : String,
  override val moduleId: ModuleIdentifier,
  override val elemType : Type
) extends CompoundType( id, description, moduleId, elemType ) with ArrayValidator {

  override def validate(value : JsValue) = {
    value match {
      case v: JsArray => validate(v, elemType)
      case x => super.validate(value)
    }
  }

  override def kind = 'List

  override def toJson = Json.obj(
    "kind" -> kind.name,
    "name" -> label,
    "plural" -> plural,
    "description" -> description,
    "elementType" -> elemType.label
  )
}


/** A Set type allows an exclusive Set of elements of other types to be constructed
  *
  * @param id
  * @param description
  * @param elemType
  */
class SetType  (
  override val id : TypeIdentifier,
  override val description : String,
  override val moduleId: ModuleIdentifier,
  override val elemType : Type
) extends CompoundType( id, description, moduleId, elemType) with ArrayValidator {

  override def validate(value : JsValue) = {
    value match {
      case v: JsArray => {
        val s = v.value
        if (s.distinct.size != s.size)
          JsError("Elements of set are not distinct")
        else
          validate(v, elemType)
      }
      case x => super.validate(value)
    }
  }

  override def kind = 'Set

  override def toJson = Json.obj(
    "kind" -> kind.name,
    "name" -> label,
    "plural" -> plural,
    "description" -> description,
    "elementType" -> elemType.label
  )
}

/** A Map is a set whose elements are named with an arbitrary string
  *
  * @param id
  * @param description
  * @param elemType
  */
class MapType  (
  override val id : TypeIdentifier,
  override val description : String,
  override val moduleId: ModuleIdentifier,
  override val elemType : Type
) extends CompoundType( id, description, moduleId, elemType) {

  override def validate(value : JsValue) = {
    value match {
      case v: JsObject => {
        if (v.value exists { case ( key: String, elem: JsValue ) =>
                             key.isEmpty || !elemType.validate(elem).asOpt.isDefined } )
          JsError("Not all elements match element type")
        else
          JsSuccess(true)
      }
      case x => super.validate(value)
    }
  }

  override def kind = 'Map

  override def toJson = Json.obj(
    "kind" -> kind.name,
    "name" -> label,
    "plural" -> plural,
    "description" -> description,
    "elementType" -> elemType.label
  )
}

/** A group of named and typed fields that are related in some way.
  * An entity type defines the structure of the fundamental unit of storage in Scrupal: An entity.  EntityTypes
  * are analogous to table definitions in relational databases, but with one important difference. An EntityType can
  * define itself recursively. That is, one of the fields of an Entity can be of Entity Type. In this way it is
  * possible to assemble entities from a large collection of smaller entity concepts (traits if you will).
  * Like relational tables, entity types define the names and types of a group of fields (columns) that are in some
  * way related. For example, a street name, city name, country and postal code are fields of an address trait
  * because they are related by the common purpose of specifying a location.
  *
  * Note that EntityTypes, along with ListType, SetType and MapType make the type system fully composable. You
  * can create an arbitrarily complex data structure (not that we recommend that you do so).
  *
  * EntityTypes also have actions associated with them. Actions can
  * - transform the fields of the Trait into arbitrary JSON (map)
  * - access the fields of the Trait to reduce its content (reduce)
  * - mutate the fields of the Trait to produce a new instance of the Trait (mutate)
  * @param id The name of the trait
  * @param description A description of the trait in terms of its purpose or utility
  * @param fields A map of the field name symbols to their Type
  */
class BundleType (
  override val id : TypeIdentifier,
  override val description : String,
  override val moduleId: ModuleIdentifier,
  fields : HashMap[Symbol, Type]
) extends Type( id, description, moduleId) with ObjectValidator {
  require(!fields.isEmpty)

  override def validate(value: JsValue) : JsResult[Boolean] = {
    value match {
      case v: JsObject => validate(v, fields)
      case x => super.validate(value)
    }
  }

  override def kind = 'Bundle

  override def toJson = {
    Json.obj(
      "kind" -> kind.name,
      "name" -> label,
      "plural" -> plural,
      "description" -> description,
      "fields" -> (fields map { case (s:Symbol, t:Type) => (s.name, t.label ) })
    )
  }
}

/** A utility object for accessing the types registered by modules */
object Type extends Registry[Type] {

  override val registryName = "Types"
  override val registrantsName = "type"

  /** Determine if a type exists
    * Types are interned by the Registry[Type] utility. This means that types share a single global name space.
    * Modules must cooperate on defining types in such a way that their names do not conflict.
    * @param name The Symbol for the type to check
    * @return true iff ```name``` is a registered type
    */
  def exists(name: Symbol) : Boolean = registrants.contains(name)

  /** Determine if a type is a certain kind
    * While Scrupal defines a useful set of types that will suffice for many needs,
    * Modules are free to create new kinds of types (i.e. subclass from Type itself,
    * not create a new instance of Type). Types have a "kind" field that allows them to be categorized roughly by the
    * nature of the subclass from Type. This method checks to see if a given type is a member of that category.
    * @param name The symbol for the type to check
    * @param kind The symbol for the kind that ```name``` should be
    * @return true iff ```name``` is of kind ```kind```
    */
  def isKind(name: Symbol, kind: Symbol) : Boolean = registrants.getOrElse(name,NotAType).kind == kind

  lazy val NotAType = new Type('NotAType, "Not A Type", 'CoreModule) { override val kind = 'Cruel }

  /** Retrieve the module in which a Type was defined
    * Every Type is associated with a module. This utility helps you find the associated module for a given type id
    * @param id The Symbol for the type to look up
    * @return Some[Module] if the ```id``` was found, None otherwise
    */
  def moduleOf(id: TypeIdentifier) : Option[Module] = {
    registrants.get(id) match {
      case Some(ty) => Module(ty.moduleId)
      case _ => None
    }
  }

  implicit lazy val typeWriter : Writes[Type] = new Writes[Type] {
    def writes(typ: Type) = typ.toJson
  }
}

