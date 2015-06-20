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

case class EnumValidator(enumerators : Map[Identifier, Int], name : String) extends Validator[EnumType.ISILA] {

  def validate(ref : Location, value : EnumType.ISILA) : VResult = {
    simplify(ref, value, "Integer, Long or String") { value ⇒
      object validator extends Poly1 {
        implicit def caseInt = at[Int] { i : Int ⇒
          if (!enumerators.exists { y ⇒ y._2 == i }) Some(s"Value $i not valid for '$name'") else None
        }
        implicit def caseLong = at[Long] { l : Long ⇒
          if (!enumerators.exists { y ⇒ y._2 == l }) Some(s"Value $l not valid for '$name'") else None
        }
        implicit def caseString = at[String] { s : String ⇒
          if (!enumerators.contains(Symbol(s))) Some(s"Value $s not valid for '$name'") else None
        }
        implicit def caseIdentifier = at[Identifier] { id : Identifier ⇒
          if (!enumerators.contains(id)) Some(s"Value '$id' not valid for '$name'") else None
        }
        implicit def caseAny = at[Any] { x ⇒ Some("") }
      }
      val mapped = value.map(validator)
      val selected = mapped.select[Option[String]]
      selected.getOrElse(None)
    }
  }
}

/** An Enum type allows a selection of one enumerator from a list of enumerators.
  * Each enumerator is assigned an integer value.
  */
case class EnumType(
  id : Identifier,
  description : String,
  enumerators : Map[Identifier, Int]) extends Type[EnumType.ISILA] {
  require(enumerators.nonEmpty)
  val validator = EnumValidator(enumerators, label)
  def validate(ref : Location, value : EnumType.ISILA) = {
    validator.validate(ref, value)
  }
  override def kind = 'Enum
  def valueOf(enum : Symbol) = enumerators.get(enum)
  def valueOf(enum : String) = enumerators.get(Symbol(enum))
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
