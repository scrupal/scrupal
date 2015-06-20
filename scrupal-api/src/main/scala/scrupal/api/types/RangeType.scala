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
import shapeless.{Coproduct, Poly1, CNil, :+:}

import scala.language.implicitConversions

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
  max : Long = Long.MaxValue) extends Type[RangeType.SIL] {
  require(min <= max)
  def validate(ref : Location, value : RangeType.SIL) : VResult = {
    simplify(ref, value, "String, Integer or Long") { value ⇒
      object validation extends Poly1 {
        implicit def caseString = at[String] { s: String ⇒
          try {
            val num = s.toLong
            if (num > max)
              Some(s"Value $s is out of range, above maximum of $max")
            else if (num < min)
              Some(s"Value $s is out of range, below minimum of $min")
            else
              None
          } catch {
            case x: Throwable ⇒
              Some(s"Value '$s' is not convertible to a number: ${x.getClass.getSimpleName}: ${x.getMessage}")
          }
        }

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
        implicit def caseDouble = at[Double] { d : Double ⇒
          if (d < min)
            Some(s"Value #d is out of range, below minimum of $max")
          else if (d > max)
            Some(s"Value $d is out of range, above maximum of $max")
          else
            None
        }
      }
      value.map(validation).select[Option[String]].getOrElse(None)
    }
  }
  override def kind = 'Range
}

object RangeType {
  type SIL = String :+: Int :+: Long :+: Double :+: CNil
  implicit def stringWrapper(str : String) : SIL = Coproduct[SIL](str)
  implicit def intWrapper(int: Int) : SIL = Coproduct[SIL](int)
  implicit def longWrapper(long: Long) : SIL = Coproduct[SIL](long)
  implicit def doubleWrapper(dbl: Double) : SIL = Coproduct[SIL](dbl)

  implicit def strSeqWrapper(ids: Seq[String]) : Seq[SIL] = ids.map { x ⇒ Coproduct[SIL](x) }
  implicit def intSeqWrapper(ids: Seq[Int]) : Seq[SIL] = ids.map { x ⇒ Coproduct[SIL](x) }
  implicit def longSeqErapper(ids: Seq[Long]) : Seq[SIL] = ids.map { x ⇒ Coproduct[SIL](x) }
  implicit def doubleSeqErapper(ids: Seq[Double]) : Seq[SIL] = ids.map { x ⇒ Coproduct[SIL](x) }

  implicit def strSetWrapper(ids: Set[String]) : Set[SIL] = ids.map { x ⇒ Coproduct[SIL](x) }
  implicit def intSetWrapper(ids: Set[Int]) : Set[SIL] = ids.map { x ⇒ Coproduct[SIL](x) }
  implicit def longSetErapper(ids: Set[Long]) : Set[SIL] = ids.map { x ⇒ Coproduct[SIL](x) }
  implicit def doubleSetErapper(ids: Set[Double]) : Set[SIL] = ids.map { x ⇒ Coproduct[SIL](x) }

}

object AnyInteger_t
  extends RangeType('AnyInteger, "A type that accepts any integer value", Int.MinValue, Int.MaxValue)

/** The Scrupal Type for TCP port numbers */
object TcpPort_t
  extends RangeType('TcpPort, "A type for TCP port numbers", 1, 65535) {
}

