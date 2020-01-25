package com.crewly.utils

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

class TimeDisplay {

  enum class Format {
    ZULU_HOUR,
    LOCAL_HOUR
  }

  private val hourFormatter by lazy {
    DateTimeFormat.forPattern("HH:mm")
  }

  fun buildDisplayTime(
    format: Format,
    time: DateTime,
    timeZoneId: String? = null
  ): String =
    when (format) {
      Format.ZULU_HOUR -> "Z ${hourFormatter.print(time)}"
      Format.LOCAL_HOUR -> {
        val zone = if (timeZoneId != null) DateTimeZone.forID(timeZoneId) else DateTimeZone.UTC
        "L ${hourFormatter.print(time.withZone(zone))}"
      }
    }
}