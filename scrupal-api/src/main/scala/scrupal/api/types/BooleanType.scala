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
import shapeless._

case class BooleanType(
  id : Identifier,
  description : String) extends Type[BooleanType.BLISS] {
  override def kind = 'Boolean
  def verity = List("true", "on", "yes", "confirmed")
  def falseness = List("false", "off", "no", "denied")

  def validate(ref : Location, value : BooleanType.BLISS) : VResult = {
    simplify(ref, value, "Boolean, Integer, Long, or String") { v  ⇒
      object booleanValidator extends Poly1 {
        implicit def caseBoolean = at[Boolean](b ⇒ None)
        implicit def caseLong = at[Long] { l ⇒
          if (l == 0 || l == 1) None else Some(s"Value '$l' could not be converted to boolean (0 or 1 required")
        }
        implicit def caseInt = at[Int] { i ⇒
          if (i == 0 || i == 1) None else Some(s"Value '$i' could not be converted to boolean (0 or 1 required")
        }
        implicit def caseSrhot = at[Short] { i ⇒
          if (i == 0 || i == 1) None else Some(s"Value '$i' could not be converted to boolean (0 or 1 required")
        }
        implicit def caseString = at[String] { s ⇒
          if (verity.contains(s)) None
          else if (falseness.contains(s)) None
          else Some(s"Value '$s' could not be interpreted as a boolean")
        }
      }
      val mapped = v.map(booleanValidator)
      mapped.select[Option[String]].flatten
    }
  }
}

object BooleanType {
  type BLISS = Boolean :+: Long :+: Int :+: Short :+: String :+: CNil
}

object Boolean_t extends BooleanType('TheBoolean, "A type that accepts true/false values")

