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

package scrupal.utils

import java.util.Locale
import java.util.concurrent.TimeUnit

import org.joda.time.{DateTime, Period}
import org.joda.time.format.{ISODateTimeFormat, PeriodFormatter, PeriodFormatterBuilder}

import scala.concurrent.duration._

/** A Suite of utilities for manipulating Duration, DateTime, Date, etc.
 */
object DateTimeHelpers {

  def dateStr(millis: Long) : String = new DateTime(millis).toString(ISODateTimeFormat.dateTime)
  def dateStr(dt: DateTime) : String = dateStr(dt.getMillis)

  def makeDurationReadable(duration: Duration) = {
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
