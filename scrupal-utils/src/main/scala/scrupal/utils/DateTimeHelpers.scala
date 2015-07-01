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

package scrupal.utils

import java.time.format.DateTimeFormatter
import java.time.{Period, Instant}

import scala.concurrent.duration._

/** A Suite of utilities for manipulating Duration, DateTime, Date, etc.
  */
object DateTimeHelpers {

  def dateStr(millis : Long) : String = dateStr(Instant.ofEpochMilli(millis))
  def dateStr(dt : Instant) : String = DateTimeFormatter.ISO_INSTANT.format(dt)

  implicit class StringBuilderPimps(bldr: StringBuilder) {
    def appendSuffix(value: Long, singular: String) : StringBuilder = {
      bldr.append(" ").append{if (value == 1) singular else Pluralizer.pluralize(singular) }.append(", ")
    }
  }

  def makeReadable(period: Period) : String = {
    val builder = new StringBuilder()
    if (period.getYears > 0) {
      builder.appendSuffix(period.getYears, "year")
    }
    if (period.getMonths > 0) {
      builder.appendSuffix(period.getMonths, "month")
    }
    if (period.getDays > 0) {
      builder.appendSuffix(period.getDays, "day")
    }
    builder.toString().dropRight(2)
  }

  def makeReadable(duration: java.time.Duration) : String = {
    val builder = new StringBuilder()

    val days : java.time.Duration = {
      if (duration.toDays >= 365.2425) {
        val num_years = (duration.toDays / 365.2425).toInt
        builder.append(num_years).appendSuffix(num_years, "year")
        duration.minusDays((num_years*365.2425).toLong)
      } else {
        duration
      }
    }

    val hours : java.time.Duration = if (days.toHours > 24) {
      val num_days = days.toDays
      builder.append(num_days).appendSuffix(num_days, "day")
      days.minusDays(num_days)
    } else {
      days
    }

    val minutes : java.time.Duration  = if (hours.toMinutes > 60) {
      val num_hours = hours.toHours
      builder.append(num_hours).appendSuffix(num_hours, "hour")
      hours.minusHours(num_hours)
    } else {
      hours
    }

    val millis = if (minutes.toMillis > 60000L) {
      val num_minutes = minutes.toMinutes
      builder.append(num_minutes).appendSuffix(num_minutes, "minute")
      minutes.minusMinutes(num_minutes)
    } else {
      minutes
    }

    if (millis.toMillis > 1000L) {
      val num_seconds : Double = millis.toMillis / 1000.0
      builder.append(f"$num_seconds%2.3f").appendSuffix({if(num_seconds == 1.0) 1L else 2L}, "second")
    } else {
      builder.append(millis.toMillis).append(" ms, ")
    }
    builder.toString().drop(2)
  }

  def makeReadable(duration : Duration) : String = {
    makeReadable(java.time.Duration.ofNanos(duration.toNanos))
  }

  @deprecated("Use makeReadable(duration: Duration) instead", "0.2.0")
  def makeDurationReadable(duration : Duration) : String = makeReadable(duration)
}
