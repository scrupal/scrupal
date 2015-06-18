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

import play.api.libs.json.{JsObject, JsArray, JsValue}
import scrupal.utils.Validation._
import scrupal.utils.{ Registrable, Registry, Pluralizer }

import scala.collection.Map

case class TypeFailure[VT, T <: Type[VT]](ref : Location, value : VT, t : T, errors : String*)
  extends Failure[VT] {
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
  def registry : Registry[Type[_]] = Type

  type ValueType = VT

  /** The plural of the name of the type.
    * The name given to the type should be the singular form (Color not Colors) but things associated with this type
    * may wish to use the plural form. To ensure that everyone uses the same rules for pluralization,
    * we use our utility [[scrupal.utils.Pluralizer]] to make it consistent. This is lazy constructed so there's no
    * cost for it unless it gets used.
    */
  lazy val plural = Pluralizer.pluralize(label)

  def moduleOf = { Module.values.find(mod ⇒ mod.types.contains(this)) }

  /** The kind of this class is simply its simple class name. Each "kind" has a different information structure */
  def kind : Symbol = Symbol(super.getClass.getSimpleName.replace("$", "_"))

  def trivial = false
  def nonTrivial = !trivial

  override protected def simplify(ref : Location, value : VT, classes : String)(
    validator : (VT) ⇒ Option[String]) : VResult =
    {
      validator(value) match {
        case Some("") ⇒ wrongClass(ref, value, classes)
        case Some(msg : String) ⇒ TypeFailure(ref, value, this, msg)
        case None ⇒ Success(ref, value)
      }
    }
}

case class Not_A_Type() extends Type[Boolean] {
  lazy val id = 'NotAType
  override val kind = 'NotAKind
  override val trivial = true
  val description = "Not A Type"
  val module = 'NotAModule
  def validate(ref : Location, value : Boolean) =
    TypeFailure(ref, value, this, "NotAType is not valid")
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

/** Type Registry and companion */
object Type extends Registry[Type[_]] {

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
  def isKindOf(name : Symbol, kind : Symbol) : Boolean = lookupOrElse(name, NotAType).kind == kind

  lazy val NotAType = Not_A_Type()

  def of(id : Identifier) : Type[_] = {
    Type(id) match {
      case Some(typ) ⇒ typ
      case None ⇒ new UnfoundType(id)
    }
  }

  /** FIXME: Handle reading/writing Type instances to and from BSON.
    * Note that types are a little special. We write them as strings and restore them via lookup. Types are intended
    * to only ever live in memory but they can be references in the database. So when a Type is a field of some
    * class that is stored in the database, what actually gets stored is just the name of the type.
    * class BSONHandlerForType[T <: Type] extends BSONHandler[JsString, T] {
    * override def write(t : T) : BSONString = BSONString(t.id.name)
    * override def read(bson : BSONString) : T = Type.as(Symbol(bson.value))
    * }
    */
}

