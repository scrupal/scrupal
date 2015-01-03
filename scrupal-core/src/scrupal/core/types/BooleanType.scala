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

package scrupal.core.types

import reactivemongo.bson._
import scrupal.core.api._

case class BooleanType(
  id : Identifier,
  description: String
  ) extends Type {
  override type ScalaValueType = Boolean
  override def kind = 'Boolean
  def verity = List("true", "on", "yes", "confirmed")
  def falseness = List("false", "off", "no", "denied")

  def validate(value: BSONValue) : BVR = {
    simplify(value, "Boolean, Integer, Long, or String") {
      case b: BSONBoolean => None
      case BSONInteger(bi) if bi == 0 || bi == 1 => None
      case BSONInteger(bi) => Some(s"Value '$bi' could not be converted to boolean (0 or 1 required)")
      case BSONLong(bl) if bl == 0 || bl == 1 => None
      case BSONLong(bi) => Some(s"Value '$bi' could not be converted to boolean (0 or 1 required)")
      case BSONString(bs) if verity.contains(bs.toLowerCase) => None
      case BSONString(bs) if falseness.contains(bs.toLowerCase) => None
      case BSONString(bs) => Some(s"Value '$bs' could not be interpreted as a boolean")
      case _ ⇒ Some("")
    }
  }
}

object Boolean_t extends BooleanType('TheBoolean, "A type that accepts true/false values")

