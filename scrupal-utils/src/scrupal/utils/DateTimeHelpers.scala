package scrupal.utils

import java.util.Locale
import java.util.concurrent.TimeUnit

import org.joda.time.Period
import org.joda.time.format.{PeriodFormatter, PeriodFormatterBuilder}

import scala.concurrent.duration._

/** A Suite of utilities for manipulating Duration, DateTime, Date, etc.
 */
object DateTimeHelpers {


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