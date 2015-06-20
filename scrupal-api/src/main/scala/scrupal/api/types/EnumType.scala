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

import shapeless.{Coproduct, Poly1, CNil, :+:}

import scala.language.implicitConversions

case class EnumValidator(enumerators : Map[Identifier, Int], name : String) extends Validator[EnumType.ID_IS] {

  object validator extends Poly1 {
    implicit def caseIdentifier = at[Identifier] { id : Identifier ⇒
      if (!enumerators.contains(id)) Some(s"Value '$id' not valid for '$name'") else None
    }
    implicit def caseString = at[String] { s : String ⇒
      if (!enumerators.contains(Symbol(s))) Some(s"Value $s not valid for '$name'") else None
    }
    implicit def caseInt = at[Int] { i : Int ⇒
      if (!enumerators.exists { y ⇒ y._2 == i }) Some(s"Value $i not valid for '$name'") else None
    }
    implicit def caseLong = at[Long] { l : Long ⇒
      if (!enumerators.exists { y ⇒ y._2 == l }) Some(s"Value $l not valid for '$name'") else None
    }
    implicit def caseAny = at[Any] { x ⇒ Some("") }
  }

  def validate(ref : Location, value : EnumType.ID_IS) : VResult = {
    simplify(ref, value, "Integer, Long or String") { value ⇒
      value.map(validator).select[Option[String]].getOrElse(None)
    }
  }
}

/** An Enum type allows a selection of one enumerator from a list of enumerators.
  * Each enumerator is assigned an integer value.
  */
case class EnumType(
  id : Identifier,
  description : String,
  enumerators : Map[Identifier, Int]) extends Type[EnumType.ID_IS] {
  require(enumerators.nonEmpty)
  val validator = EnumValidator(enumerators, label)
  def validate(ref : Location, value : EnumType.ID_IS) = {
    validator.validate(ref, value)
  }
  override def kind = 'Enum
  def valueOf(enum : Symbol) = enumerators.get(enum)
  def valueOf(enum : String) = enumerators.get(Symbol(enum))
}

object EnumType {
  type ID_IS = Identifier :+: Int :+: String :+: CNil
  implicit def idWrapper(id: Identifier) : ID_IS = Coproduct[ID_IS](id)
  implicit def strWrapper(str: String) : ID_IS = Coproduct[ID_IS](str)
  implicit def intWrapper(int: Int) : ID_IS = Coproduct[ID_IS](int)
  implicit def idSetWrapper(ids: Set[Identifier]) : Set[ID_IS] = ids.map { x ⇒ Coproduct[ID_IS](x) }
  implicit def strSetWrapper(ids: Set[String]) : Set[ID_IS] = ids.map { x ⇒ Coproduct[ID_IS](x) }
  implicit def intSetWrapper(ids: Set[Int]) : Set[ID_IS] = ids.map { x ⇒ Coproduct[ID_IS](x) }
  implicit def idSeqWrapper(ids: Seq[Identifier]) : Seq[ID_IS] = ids.map { x ⇒ Coproduct[ID_IS](x) }
  implicit def strSeqWrapper(ids: Seq[String]) : Seq[ID_IS] = ids.map { x ⇒ Coproduct[ID_IS](x) }
  implicit def intSeqWrapper(ids: Seq[Int]) : Seq[ID_IS] = ids.map { x ⇒ Coproduct[ID_IS](x) }
}
