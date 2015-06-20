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

package scrupal.api.types

import scrupal.api._
import scrupal.utils.Validation._

import shapeless._

import scala.language.implicitConversions


/** An Enum type allows a selection of one enumerator from a list of enumerators.
  * Each enumerator is assigned an integer value.
  */
case class EnumType(
  id : Identifier,
  description : String,
  enumerators : Map[Identifier, Int]) extends Type[Atom] {
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
  }

  def validate(ref : Location, value : Atom) = {
    simplify(ref, value, "Atom") { value ⇒
      value.map(validator).unify
    }
  }

}

object EnumType {
  type ISILA = Int :+: Long :+: String :+: Identifier :+: Any :+: CNil
  implicit def idWrapper(id: Identifier) : ISILA = Coproduct[ISILA](id)
  implicit def strWrapper(str: String) : ISILA = Coproduct[ISILA](str)
  implicit def intWrapper(int: Int) : ISILA = Coproduct[ISILA](int)
  implicit def idSetWrapper(ids: Set[Identifier]) : Set[ISILA] = ids.map { x ⇒ Coproduct[ISILA](x) }
  implicit def strSetWrapper(ids: Set[String]) : Set[ISILA] = ids.map { x ⇒ Coproduct[ISILA](x) }
  implicit def intSetWrapper(ids: Set[Int]) : Set[ISILA] = ids.map { x ⇒ Coproduct[ISILA](x) }
  implicit def idSeqWrapper(ids: Seq[Identifier]) : Seq[ISILA] = ids.map { x ⇒ Coproduct[ISILA](x) }
  implicit def strSeqWrapper(ids: Seq[String]) : Seq[ISILA] = ids.map { x ⇒ Coproduct[ISILA](x) }
  implicit def intSeqWrapper(ids: Seq[Int]) : Seq[ISILA] = ids.map { x ⇒ Coproduct[ISILA](x) }
}
