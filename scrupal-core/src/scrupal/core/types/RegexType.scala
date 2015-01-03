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

import java.util.regex.Pattern

import reactivemongo.bson.{BSONString, BSONValue}
import scrupal.core.api._

import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

/** A type for a regular expression
  *
  * @param id The name of the Regex type
  * @param description A brief description of the regex type
  */
case class RegexType (
  id : Identifier,
  description: String
) extends Type {
  override type ScalaValueType = Regex
  def validate(value: BSONValue) : BVR = {
    simplify(value, "String") {
      case BSONString(bs) => Try {
        Pattern.compile(bs)
      } match {
        case Success(x) ⇒ None
        case Failure(x) ⇒ Some(s"Error in pattern: ${x.getClass.getName}: ${x.getMessage}")
      }
      case _ ⇒ Some("")
    }
  }
}

object Regex_t
  extends RegexType('Regex, "Regular expression type")

