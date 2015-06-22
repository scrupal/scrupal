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

case class BooleanType(
  id : Identifier,
  description : String)(implicit val scrupal: Scrupal) extends Type[Atom] {
  override def kind = 'Boolean
  def verity = List("true", "on", "yes", "confirmed")
  def falseness = List("false", "off", "no", "denied")

  private object validation extends Poly1 {
    def check[T](value: T): Option[String] = {
      if (value == 0 || value == 1)
        None
      else
        Some(s"Value '$value' could not be converted to boolean (0 or 1 required")
    }
    def checkString(s: String) : Option[String] = {
      if (verity.contains(s)) None
      else if (falseness.contains(s)) None
      else Some(s"Value '$s' could not be interpreted as a boolean")
    }


    implicit def caseBoolean = at[Boolean] { b: Boolean ⇒ None}

    implicit def caseByte = at[Byte] { b: Byte ⇒ check(b) }

    implicit def caseShort = at[Short] { s: Short ⇒ check(s) }

    implicit def caseInt = at[Int] { i: Int ⇒ check(i) }

    implicit def caseLong = at[Long] { l: Long ⇒ check(l) }

    implicit def caseFloat = at[Float] { f: Float ⇒ check(f) }

    implicit def caseDouble = at[Double] { d: Double ⇒ check(d) }

    implicit def caseSymbol = at[Symbol] { s : Symbol ⇒ checkString(s.name) }

    implicit def caseString = at[String] { s ⇒ checkString(s) }

    implicit def caseInstant = at[Instant] { i : Instant ⇒ check(i.toEpochMilli, i) }

    implicit def caseDuration = at[Duration] { i : Duration ⇒ check(i.toMillis, i)  }

  }
  def validate(ref : Location, value : Atom) : VResult = {
    simplify(ref, value, "Boolean, Integer, Long, or String") { v  ⇒
      val mapped = v.map(validation)
      mapped.select[Option[String]].flatten
    }
  }
}


