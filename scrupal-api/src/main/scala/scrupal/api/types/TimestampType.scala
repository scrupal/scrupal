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

import org.joda.time.DateTime
import scrupal.api._
import scrupal.utils.Validation.Location

import scala.concurrent.duration.Duration

/** A point-in-time value between a minimum and maximum time point
  *
  * @param id
  * @param description
  * @param min
  * @param max
  */
case class TimestampType(
  id : Identifier,
  description : String,
  min : DateTime = new DateTime(0L),
  max : DateTime = new DateTime(Long.MaxValue / 2))
  (implicit val scrupal : Scrupal) extends Type[Long] {
  assert(min.getMillis <= max.getMillis)
  def validate(ref : Location, value : Long) = {
    simplify(ref, value, "Long") {
      case l : Long if l < min.getMillis ⇒
        Some(s"Timestamp $l is out of range, below minimum of $min")
      case l : Long if l > max.getMillis ⇒
        Some(s"Timestamp $l is out of range, above maximum of $max")
      case l : Long ⇒
        None
    }
  }
}


