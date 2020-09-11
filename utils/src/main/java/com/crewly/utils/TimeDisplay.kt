package com.crewly.utils

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatterBuilder

class TimeDisplay {

  companion object {
    private const val HOUR_IN_MILLIS = 1000 * 60 * 60
    private const val MINUTE_IN_MILLIS = 1000 * 60
  }

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

  fun buildDisplayTimeFromDuration(
    durationInMillis: Long
  ): String {
    val hours = durationInMillis.div(HOUR_IN_MILLIS)
    val mins = (durationInMillis % HOUR_IN_MILLIS).div(MINUTE_IN_MILLIS)
    return "${hours}h ${mins}m"
  }

  private fun DateTime.addTimeZoneIfNeeded(
    timeZoneId: String?
  ): DateTime {
    if (timeZoneId == null || timeZoneId.isEmpty()) return this
    val zone = DateTimeZone.forID(timeZoneId)
    return withZone(zone)
  }
}