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

/** A Type for Any Kind of Numeric Value */

case class NumbersType(
  id : Identifier,
  description : String,
  min : Double = Double.MinValue,
  max : Double = Double.MaxValue) extends Type[NumbersType.Numbers] {
  override def validate(ref : Location, value : NumbersType.Numbers) : Results[NumbersType.Numbers] = {
    simplify(ref, value, "Numeric Types") { v : NumbersType.Numbers ⇒
      object numbersValidation extends Poly1 {
        implicit def caseByte = at[Byte] { b ⇒ if (b >= min && b <= max) None else errormsg(b) }
        implicit def caseShort = at[Short] { s ⇒ if (s >= min && s <= max) None else errormsg(s) }
        implicit def caseInt = at[Int] { i ⇒ if (i >= min && i <= max) None else errormsg(i) }
        implicit def caseLong = at[Long] { l ⇒ if (l >= min && l <= max) None else errormsg(l) }
        implicit def caseFloat = at[Float] { f ⇒ if (f >= min && f <= max) None else errormsg(f) }
        implicit def caseDouble = at[Double] { d ⇒ if (d >= min && d <= max) None else errormsg(d) }
        private def errormsg(v : Double) : Option[String] = Some(s"Value $v not in [$min,$max].")
      }
      // FIXME: implicit failure on this line:
      // FIXME:
      val mapped = v.map(numbersValidation)
      mapped.select[Option[String]].flatten
    }
  }
}

object NumbersType {
  type Numbers = Byte :+: Short :+: Int :+: Long :+: Float :+: Double :+: CNil
}

