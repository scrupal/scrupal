/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.api.types

import reactivemongo.bson._
import scrupal.api._

case class EnumValidator(enumerators: Map[Identifier, Int], name: String) extends BSONValidator {

  def validate(ref: ValidationLocation, value: BSONValue): VR = {
    simplify(ref, value, "Integer, Long or String") {
      case BSONInteger(x) if !enumerators.exists { y => y._2 == x} => Some(s"Value $x not valid for '$name'")
      case BSONInteger(x) => None
      case BSONLong(x) if !enumerators.exists { y => y._2 == x} => Some(s"Value $x not valid for '$name'")
      case BSONLong(x) => None
      case BSONString(x) if !enumerators.contains(Symbol(x)) => Some(s"Value '$x' not valid for '$name'")
      case BSONString(x) => None
      case _ => Some("")
    }
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
  val validator = EnumValidator(enumerators,label)
  def validate(ref: ValidationLocation, value: BSONValue) = validator.validate(ref, value)
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
  val validator = EnumValidator(enumerators, label)
  def validate(ref: ValidationLocation, value: BSONValue) : VR = {
    value match {
      case a: BSONArray => validateArray(ref, a, validator )
      case x: BSONValue => wrongClass(ref, x, "Array")
    }
  }
}
