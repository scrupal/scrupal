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

import java.util.Locale
import java.util.concurrent.TimeUnit

import org.joda.time.{ DateTime, Period }
import org.joda.time.format.{ ISODateTimeFormat, PeriodFormatterBuilder }

import scala.concurrent.duration._

/** A Suite of utilities for manipulating Duration, DateTime, Date, etc.
  */
object DateTimeHelpers {

  def dateStr(millis : Long) : String = new DateTime(millis).toString(ISODateTimeFormat.dateTime)
  def dateStr(dt : DateTime) : String = dateStr(dt.getMillis)

  def makeDurationReadable(duration : Duration) = {
    var builder = new PeriodFormatterBuilder()
    val days = if (duration.toDays > 365) {
      builder = builder.appendYears().appendSuffix(" year", "years").appendSeparator(", ")
      val remaining_days = duration.toDays % 365
      Duration(remaining_days, TimeUnit.DAYS)
    } else {
      duration
    }

    val hours : Duration = if (days.toHours > 24) {
      builder = builder.appendDays().appendSuffix(" day", "days").appendSeparator(", ")
      val remaining_hours = days.toHours % 24
      Duration(remaining_hours, TimeUnit.HOURS)
    } else {
      days
    }

    val minutes = if (hours.toMinutes > 60) {
      builder = builder.appendHours().appendSuffix(" hour", "hours").appendSeparator(", ")
      val remaining_minutes = hours.toMinutes % 60
      Duration(remaining_minutes, TimeUnit.MINUTES)
    } else {
      hours
    }

    val seconds = if (minutes.toSeconds > 60) {
      builder = builder.appendMinutes().appendSuffix(" minute", "minutes").appendSeparator(", ")
      val remaining_seconds = minutes.toSeconds % 60
      Duration(remaining_seconds, TimeUnit.SECONDS)
    } else {
      minutes
    }

    if (seconds.toMillis > 1000) {
      builder = builder.appendSecondsWithMillis().appendSuffix(" second", "seconds")
    } else {
      builder = builder.appendMillis().appendSuffix(" ms", " ms")
    }

    val formatter = builder.toFormatter

    val period = new Period(duration.toMillis)

    val buff = new StringBuffer

    formatter.getPrinter.printTo(buff, period, Locale.US)

    buff.toString

  }
}
