package com.crewly.duty

import com.crewly.db.duty.Duty
import com.crewly.models.Company
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
    date: DateTime = DateTime(0)
  ): Duty =
    Duty(
      company = Company.Ryanair,
      ownerId = ownerId,
      type = type,
      specialEventType = specialEventType,
      date = date
    )
}