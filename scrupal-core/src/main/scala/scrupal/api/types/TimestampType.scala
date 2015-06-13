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

import org.joda.time.DateTime
import reactivemongo.bson.{BSONLong, BSONValue}
import scrupal.api._

import scala.concurrent.duration.Duration

/** A point-in-time value between a minimum and maximum time point
  *
  * @param id
  * @param description
  * @param min
  * @param max
  */
case class TimestampType (
  id : Identifier,
  description: String,
  min: DateTime = new DateTime(0L),
  max: DateTime = new DateTime(Long.MaxValue/2)
) extends Type {
  override type ScalaValueType = Duration
  assert(min.getMillis <= max.getMillis)
  def validate(ref: ValidationLocation, value: BSONValue) = {
    simplify(ref, value, "Long") {
      case BSONLong(l) if l < min.getMillis =>
        Some(s"Timestamp $l is out of range, below minimum of $min")
      case BSONLong(l) if l > max.getMillis =>
        Some(s"Timestamp $l is out of range, above maximum of $max")
      case BSONLong(l) =>
        None
      case x: BSONValue =>
        Some("")
    }
  }
}

object AnyTimestamp_t
  extends TimestampType('AnyTimestamp, "A type that accepts any timestamp value")

