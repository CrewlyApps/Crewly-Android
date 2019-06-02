package com.crewly.duty.ryanair

import com.crewly.R
import com.crewly.models.duty.DutyIcon
import com.crewly.models.duty.DutyIcon.Companion.NO_ICON

/**
 * Created by Derek on 22/04/2019
 */
class RyanairDutyIcon(
  private val dutyName: String
): DutyIcon {

  override val iconResourceId: Int
    get() = when (dutyName) {
      RyanairDutyType.AIRPORT_STANDBY -> R.drawable.icon_asby
      RyanairDutyType.HOME_STANDBY -> R.drawable.icon_home
      RyanairDutyType.OFF, RyanairDutyType.BANK_HOLIDAY -> R.drawable.icon_off
      RyanairDutyType.ANNUAL_LEAVE -> R.drawable.icon_annual_leave
      RyanairDutyType.SICK -> R.drawable.icon_sick
      RyanairDutyType.PARENTAL_LEAVE -> R.drawable.icon_parental_leave
      else -> NO_ICON
    }
}