package com.crewly.models

import org.joda.time.DateTime

/**
 * Created by Derek on 21/04/2019
 */
data class DateTimePeriod(
  val startDateTime: DateTime,
  val endDateTime: DateTime
)