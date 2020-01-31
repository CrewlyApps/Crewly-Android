package com.crewly.utils

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatterBuilder

class TimeDisplay {

  enum class Format {
    DATE,
    ZULU_HOUR,
    LOCAL_HOUR,
    HOUR_WITH_LITERALS
  }

  private val dateFormatter by lazy {
    DateTimeFormat.forPattern("YYYY-MM-dd")
  }

  private val hourWithZuluFormatter by lazy {
    DateTimeFormatterBuilder()
      .appendLiteral("Z ")
      .appendHourOfDay(2)
      .appendLiteral(":")
      .appendMinuteOfHour(2)
      .toFormatter()
  }

  private val hourWithLocalFormatter by lazy {
    DateTimeFormatterBuilder()
      .appendLiteral("L ")
      .appendHourOfDay(2)
      .appendLiteral(":")
      .appendMinuteOfHour(2)
      .toFormatter()
  }

  private val hourLiteralFormatter by lazy {
    DateTimeFormatterBuilder()
      .appendHourOfDay(2)
      .appendLiteral("h ")
      .appendMinuteOfHour(2)
      .appendLiteral("m")
      .toFormatter()
  }

  fun buildDisplayTime(
    format: Format,
    time: DateTime,
    timeZoneId: String? = null
  ): String =
    when (format) {
      Format.DATE -> dateFormatter.print(time.addTimeZoneIfNeeded(timeZoneId))
      Format.ZULU_HOUR -> hourWithZuluFormatter.print(time)
      Format.LOCAL_HOUR -> hourWithLocalFormatter.print(time.addTimeZoneIfNeeded(timeZoneId))
      Format.HOUR_WITH_LITERALS -> hourLiteralFormatter.print(time.addTimeZoneIfNeeded(timeZoneId))
    }

  private fun DateTime.addTimeZoneIfNeeded(
    timeZoneId: String?
  ): DateTime {
    if (timeZoneId == null) return this
    val zone = DateTimeZone.forID(timeZoneId)
    return withZone(zone)
  }
}