package com.crewly.duty.ryanair

import com.crewly.R
import com.crewly.duty.DutyIcon
import com.crewly.duty.DutyIcon.Companion.NO_ICON
import com.crewly.duty.RyanairDutyType

/**
 * Created by Derek on 22/04/2019
 */
class RyanairDutyIcon(
  private val dutyName: String
): DutyIcon {

  override val iconResourceId: Int
    get() = when (dutyName) {
      RyanairDutyType.AIRPORT_STANDBY.dutyName -> R.drawable.icon_asby
      RyanairDutyType.HOME_STANDBY.dutyName -> R.drawable.icon_home
      RyanairDutyType.OFF.dutyName, RyanairDutyType.BANK_HOLIDAY.dutyName -> R.drawable.icon_off
      RyanairDutyType.ANNUAL_LEAVE.dutyName -> R.drawable.icon_annual_leave
      RyanairDutyType.SICK.dutyName -> R.drawable.icon_sick
      RyanairDutyType.PARENTAL_LEAVE.dutyName -> R.drawable.icon_parental_leave
      else -> NO_ICON
    }
}