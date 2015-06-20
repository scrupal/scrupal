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

import scala.language.implicitConversions

import scrupal.utils.Validation.Location

import shapeless._

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
  max : Long = Long.MaxValue) extends Type[Atom] {
  require(min <= max)

  override def kind = 'Range

  private object validation extends Poly1 {
    def check[T](value: Long, orig: T): Option[String] = {
      if (value < min)
        Some(s"Value $orig ($value) is out of range, below minimum of $min")
      else if (value > max)
        Some(s"Value $orig ($value) is out of range, above maximum of $max")
      else
        None
    }

    implicit def caseBoolean = at[Boolean] { b: Boolean ⇒ check(if (b) 1 else 0, b) }

    implicit def caseByte = at[Byte] { b: Byte ⇒ check(b.toLong, b) }

    implicit def caseShort = at[Short] { s: Short ⇒ check(s.toLong, s) }

    implicit def caseInt = at[Int] { i: Int ⇒ check(i.toLong, i) }

    implicit def caseLong = at[Long] { l: Long ⇒ check(l, l) }

    implicit def caseFloat = at[Float] { f: Float ⇒ check(f.toLong, f) }

    implicit def caseDouble = at[Double] { d: Double ⇒ check(d.toLong, d) }

    implicit def caseSymbol = at[Symbol] { s : Symbol ⇒ checkString(s.name) }

    implicit def caseString = at[String] { s : String ⇒ checkString(s) }

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
    simplify(ref, value, "Atom") { value ⇒
      value.map(validation).unify
    }
  }

}

object AnyInteger_t
  extends RangeType('AnyInteger, "A type that accepts any integer value", Int.MinValue, Int.MaxValue)

/** The Scrupal Type for TCP port numbers */
object TcpPort_t
  extends RangeType('TcpPort, "A type for TCP port numbers", 1, 65535) {
}

