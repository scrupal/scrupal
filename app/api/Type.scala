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

import play.api.libs.json._

import scala.util.matching.Regex
import scala.collection.immutable.HashMap
import play.api.libs.json.JsArray
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsString
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.data.validation.ValidationError

/**
  * An enumeration for the fundamental kinds of data types that Scrupal can model.
  * Every Type that a Module can define must be based in one of these fundamental
  * kinds.
  */
object TypeKind extends Enumeration {
  type Type = Value
  val CustomKind = Value
  val StringKind = Value
  val RangeKind = Value
  val RealKind = Value
  val BLOBKind = Value
  val EnumKind = Value
  val ListKind = Value
  val SetKind = Value
  val MapKind = Value
}

/**
  * A generic type is a DescribedThing that also had a TypeKind field to identify its essential kind
  */
abstract class Type(
  name : Symbol,
  description: String,
  val kind: TypeKind.Type = TypeKind.CustomKind
) extends Thing(name, description) {
  def validate(value : JsValue) : JsResult[Boolean] = JsError("Unimplemented validator.")
}

/** A String type constrains a string by defining its content with a regular expression and a maximum length. */
case class StringType (
  override val name : Symbol,
  override val description : String,
  regex : Regex,
  maxLen : Int
) extends Type(name, description, TypeKind.StringKind) {
  override def validate(value : JsValue) = {
    value match {
      case v: JsString => {
        if (v.value.length <= maxLen && regex.findAllIn(v.value).nonEmpty) JsSuccess(true)
        else JsError("Value does not match pattern " + regex.pattern.pattern())
      }
      case x => JsError("JsString was expected, not " + (x.getClass.getSimpleName) )
    }
  }
}


/** A Range type constrains Long Integers between a minimum and maximum value */
case class RangeType (
  override val name : Symbol,
  override val description : String,
  min : Long = Int.MinValue,
  max : Long = Int.MaxValue
) extends Type(name, description, TypeKind.RangeKind) {
  override def validate(value : JsValue) = {
    value match {
      case v: JsNumber => {
        if (v.value < min) JsError("Value smaller than minimum: " + min)
        else if (v.value > max) JsError("Value greater than maximum: " + max)
        else JsSuccess(true)
      }
      case x => JsError("JsSNumber was expected, not " + x.getClass.getSimpleName )
    }
  }
}

/** A Real type constrains Double values between a minimum and maximum value */
case class RealType (
  override val name : Symbol,
  override val description : String,
  min : Double = Double.MinValue,
  max : Double = Double.MaxValue
) extends Type (name, description, TypeKind.RealKind) {
  override def validate(value : JsValue) = {
    value match {
      case v: JsNumber => {
        if (v.value < min) JsError("Value smaller than minimum: " + min)
        else if (v.value > max) JsError("Value greater than maximum: " + max)
        else JsSuccess(true)
      }
      case x => JsError("JsNumber was expected, not " + x.getClass.getSimpleName )
    }
  }
}

/** A BLOB type has a specified MIME content type and a minimum and maximum length */
case class BLOBType  (
  override val name : Symbol,
  override val description : String,
  mime : String,
  minLen : Integer = 0,
  maxLen : Long = Long.MaxValue
) extends Type (name, description, TypeKind.BLOBKind) {
  override def validate(value: JsValue) = {
    JsError("Cannot validate BLOBs with JSON data")
  }
  def validate(value: Array[Byte]) : Boolean = {
    value.size >= minLen && value.size <= maxLen
  }
}

/** An Enum type allows a selection of one enumerator from a list of enumerators.
  * Each enumerator is assigned an integer value.
  */
case class EnumType  (
  override val name : Symbol,
  override val description : String,
  val enumerators : HashMap[String, Int]
) extends Type (name, description, TypeKind.EnumKind) {
  override def validate(value : JsValue) = {
    value match {
      case v: JsString => {
        if (enumerators.contains( v.value )) JsSuccess(true)
        else JsError("Invalid enumeration value")
      }
      case x => JsError("JsString was expected, not " + x.getClass.getSimpleName )
    }
  }
  def valueOf(enum: String) = enumerators.get(enum)
}

/** Abstract base class of Types that refer to another Type that is the element type of the compound type */
abstract class CompoundType (
  name : Symbol,
  description : String,
  val elemType : Type,
  kind : TypeKind.Type
) extends Type(name, description, kind)

/** A List type allows a non-exclusive list of elements of other types to be constructed */
case class ListType  (
  override val name : Symbol,
  override val description : String,
  override val elemType : Type
) extends CompoundType (name, description, elemType, TypeKind.ListKind) {
  override def validate(value : JsValue) = {
    value match {
      case v: JsArray => {
        if (v.value exists { elem => !elemType.validate(elem).asOpt.isDefined } )
          JsError("Not all elements match element type")
        else JsSuccess(true)
      }
      case x => JsError("JsArray was expected, not " + x.getClass.getSimpleName )
    }
  }
}


/** A Set type allows an exclusive Set of elements of other types to be constructed */
case class SetType  (
  override val name : Symbol,
  override val description : String,
  override val elemType : Type
) extends CompoundType (name, description, elemType, TypeKind.SetKind) {
  override def validate(value : JsValue) = {
    value match {
      case v: JsArray => {
        val s = v.value
        if (s.distinct.size != s.size)
          JsError("Elements of set are not distinct")
        else if ( v.value exists { elem => !elemType.validate(elem).asOpt.isDefined } )
          JsError("Not all elements match element type")
        else
          JsSuccess(true)
      }
      case x => JsError("JsArray was expected, not " + x.getClass.getSimpleName )
    }
  }
}

/** A Map is a set whose elements are named with an arbitrary string */
case class MapType  (
  override val name : Symbol,
  override val description : String,
  override val elemType : Type
) extends CompoundType(name, description, elemType, TypeKind.MapKind) {
  override def validate(value : JsValue) = {
    value match {
      case v: JsObject => {
        if (v.value exists { case ( key: String, elem: JsValue ) => key.isEmpty || !elemType.validate(elem).asOpt.isDefined } )
          JsError("Not all elements match element type")
        else
          JsSuccess(true)
      }
      case x => JsError("JsObject was expected, not " + x.getClass.getSimpleName )
    }
  }
}

/** Provides a common paradigm: validation of JsObject content. */
trait ObjectValidator {
  /**
   * This traverses a JsObject and for each field, looks up the associated type in the "structure" and validates that
   * field with the type.
   * @return JsSuccess(true) if all the fields of the JsObject value validate correctly
   */
  protected def validateObj( value: JsObject)(implicit structure: Map[Symbol,Type]) : JsResult[Boolean] = {
    if (value.value exists {
      case (key,data) => !(structure.contains(Symbol(key)) &&
        structure.get(Symbol(key)).get.validate(data).asOpt.isDefined)
    })
      JsError(JsPath(), ValidationError("Not all elements validate"))
    else
      JsSuccess(true)
  }
}

/** A group of named and typed fields that are related in some way. E.g. the fields of an address */
case class Trait (
  override val name : Symbol,
  override val description : String,
  fields : HashMap[Symbol, Type]
) extends Type(name, description) with ObjectValidator {
  override def validate(value: JsValue) : JsResult[Boolean] = {
    value match {
      case v: JsObject => implicit val structure = fields ; validateObj(v)
      case x => JsError("JsObject was expected, not " + x.getClass.getSimpleName )
    }
  }
}

/** An action against an Entity that is parameterized by a JSON object and returns a JSON object as its result. */
abstract class Action(name: Symbol, description: String) extends Thing(name, description) {
  def apply(entity: Entity, in: JsObject ) : JsObject
}

/** The fundamental element of interaction for Modules.
  *
  * Entity subclasses define the fields and actions that can be accessed from the public API of Scrupal. Modules define
  * the Entity types that they support and implement the actions that can be taken against them.
  * Further description here.
  */
abstract class Entity(name: Symbol, description: String) extends Thing(name, description) with ObjectValidator {
  def traits : HashMap[Symbol,Trait] = HashMap()
  def actions : HashMap[Symbol,Action] = HashMap()
  def validate(value: JsObject) : JsResult[Boolean] = {
    implicit val structure = traits
    validateObj(value)
  }
}


