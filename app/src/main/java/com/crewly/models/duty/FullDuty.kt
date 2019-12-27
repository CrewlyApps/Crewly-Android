package com.crewly.models.duty

import com.crewly.persistence.duty.Duty

/**
 * Created by Derek on 01/06/2019
 */
data class FullDuty(
  val duty: Duty,
  val dutyType: DutyType,
  val dutyIcon: DutyIcon
)