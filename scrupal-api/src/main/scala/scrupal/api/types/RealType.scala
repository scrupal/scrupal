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

import java.time.Instant

import scrupal.api._
import scrupal.utils.Validation.Location

import shapeless._

import scala.concurrent.duration.Duration


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
  max : Double = Double.MaxValue) extends Type[Atom] {
  require(min <= max)

  override def kind = 'Real

  private object validation extends Poly1 {
    def check[T](value: Double, orig: T): Option[String] = {
      if (value < min)
        Some(s"Value $orig ($value) is out of range, below minimum of $min")
      else if (value > max)
        Some(s"Value $orig ($value) is out of range, above maximum of $max")
      else
        None
    }

    implicit def caseBoolean = at[Boolean] { b: Boolean ⇒ check(if (b) 1.0D else 0.0D, b) }

    implicit def caseByte = at[Byte] { b: Byte ⇒ check(b.toDouble, b) }

    implicit def caseShort = at[Short] { s: Short ⇒ check(s.toDouble, s) }

    implicit def caseInt = at[Int] { i: Int ⇒ check(i.toDouble, i) }

    implicit def caseLong = at[Long] { l: Long ⇒ check(l.toDouble, l) }

    implicit def caseFloat = at[Float] { f: Float ⇒ check(f.toDouble, f) }

    implicit def caseDouble = at[Double] { d: Double ⇒ check(d, d) }

    implicit def caseSymbol = at[Symbol] { s : Symbol ⇒ checkString(s.name) }

    implicit def caseString = at[String] { s : String ⇒ checkString(s) }

    implicit def caseInstant = at[Instant] { i : Instant ⇒ check(i.toEpochMilli.toDouble, i) }

    implicit def caseDuration = at[Duration] { i : Duration ⇒ check(i.toMillis.toDouble, i)  }


    def checkString(s: String) : Option[String] = {
      try {
        check(s.toLong, s)
      } catch {
        case x: Throwable ⇒
          Some(s"Value '$s' is not convertible to a number: ${x.getClass.getSimpleName}: ${x.getMessage}")
      }
    }
  }

  def validate(ref : Location, value : Atom) : VResult = {
    simplify(ref, value, "Double, Long or Integer") { value ⇒
      value.map(validation).unify
    }
  }
}


object AnyReal_t
  extends RealType('AnyReal, "A type that accepts any double floating point value", Double.MinValue, Double.MaxValue)

