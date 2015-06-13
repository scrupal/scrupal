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
import scrupal.utils.Validation.Location
import shapeless.{Poly1, CNil, :+:}

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
  max : Double = Double.MaxValue) extends Type[RealType.ILDFS] {
  override type ValueType = Double
  require(min <= max)
  def validate(ref : Location, value : RealType.ILDFS) : VResult = {
    simplify(ref, value, "Double, Long or Integer") { value ⇒
      object validator extends Poly1 {
        implicit def caseInt = at[Int] { i : Int ⇒
          if (i < min)
            Some(s"Value $i is out of range, below minimum of $max")
          else if (i > max)
            Some(s"Value $i is out of range, above maximum of $max")
          else
            None
        }
        implicit def caseLong = at[Long] { l : Long ⇒
          if (l < min)
            Some(s"Value $l is out of range, below minimum of $max")
          else if (l > max)
            Some(s"Value $l is out of range, above maximum of $max")
          else
            None
        }
        implicit def caseFloat = at[Float] { f : Float ⇒
          if (f < min)
            Some(s"Value $f is out of range, below minimum of $max")
          else if (f > max)
            Some(s"Value $f is out of range, above maximum of $max")
          else
            None
        }
        implicit def caseDouble = at[Double] { d : Double ⇒
          if (d < min)
            Some(s"Value $d is out of range, below minimum of $max")
          else if (d > max)
            Some(s"Value $d is out of range, above maximum of $max")
          else
            None
        }
        implicit def caseString = at[String] { s: String ⇒
          try {
            val num = s.toDouble
            if (num > max)
              Some(s"Value $s is out of range, above maximum of $max")
            else if (num < min)
              Some(s"Value $s is out of range, below minimum of $min")
            else
              None
          } catch{
            case x : Throwable ⇒
              Some(s"Value '$s' is not convertible to a number: ${x.getClass.getSimpleName}: ${x.getMessage}")
          }
        }
        implicit def caseOther = at[Any] { x ⇒ Some("") }
      }
      value.map(validator).select[Option[String]].getOrElse(None)
    }
  }
  override def kind = 'Real
}

object RealType {
  type ILDFS = Int :+: Long :+: Double :+: Float :+: String :+: CNil
}

object AnyReal_t
  extends RealType('AnyReal, "A type that accepts any double floating point value", Double.MinValue, Double.MaxValue)

