package com.crewly.duty

import com.crewly.models.Company
import com.crewly.models.duty.Duty
import org.joda.time.DateTime
import javax.inject.Inject

/**
 * Created by Derek on 09/06/2019
 */
class DutyFactory @Inject constructor() {

  fun createRyanairDuty(
    ownerId: String = "",
    type: String = "",
    specialEventType: String = "",
    startTime: DateTime = DateTime(0)
  ): Duty =
    Duty(
      company = Company.Ryanair,
      ownerId = ownerId,
      type = type,
      specialEventType = specialEventType,
      startTime = startTime
    )
}