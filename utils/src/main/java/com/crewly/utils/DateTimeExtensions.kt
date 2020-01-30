package com.crewly.utils

import org.joda.time.DateTime

/**
 * Created by Derek on 26/08/2018
 */

fun DateTime.withTimeAtEndOfDay(): DateTime = millisOfDay().withMaximumValue()

fun DateTime.isSameTime(
  otherTime: DateTime
): Boolean =
  millis == otherTime.millis