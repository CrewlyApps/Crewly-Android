package com.crewly.roster.ryanair

import android.app.Application
import com.crewly.R
import com.crewly.db.duty.Duty
import com.crewly.duty.DutyFactory
import com.crewly.duty.ryanair.RyanairDutyType
import javax.inject.Inject

/**
 * Created by Derek on 18/08/2018
 * Helps process data from a Ryanair roster.
 */
class RyanAirRosterHelper @Inject constructor(
  private val app: Application,
  private val dutyFactory: DutyFactory
) {

  /**
   * Return the [Duty] for [text].
   */
  fun getDutyType(text: String, isPilot: Boolean): Duty {
    val dutyType = when {
      text.matches(Regex("[0-9]+")) -> dutyFactory.createRyanairDuty(type = RyanairDutyType.FLIGHT)
      text.contains(RyanairDutyType.HOME_STANDBY) -> dutyFactory.createRyanairDuty(type = RyanairDutyType.HOME_STANDBY)
      text.contains(RyanairDutyType.AIRPORT_STANDBY) ||
        (text.contains("AD") && !text.contains("CADET")) -> dutyFactory.createRyanairDuty(type = RyanairDutyType.AIRPORT_STANDBY)
      text.startsWith(RyanairDutyType.OFF) -> dutyFactory.createRyanairDuty(type = RyanairDutyType.OFF)
      text.contains(RyanairDutyType.SICK) -> dutyFactory.createRyanairDuty(type = RyanairDutyType.SICK)
      text.contains(RyanairDutyType.BANK_HOLIDAY) -> dutyFactory.createRyanairDuty(type = RyanairDutyType.BANK_HOLIDAY)
      text.contains(RyanairDutyType.ANNUAL_LEAVE) -> dutyFactory.createRyanairDuty(type = RyanairDutyType.ANNUAL_LEAVE)
      text.contains(RyanairDutyType.UNPAID_LEAVE) -> dutyFactory.createRyanairDuty(type = RyanairDutyType.UNPAID_LEAVE)
      text.contains(RyanairDutyType.NOT_AVAILABLE) -> dutyFactory.createRyanairDuty(type = RyanairDutyType.NOT_AVAILABLE)
      text.contains(RyanairDutyType.PARENTAL_LEAVE) ||
        text.contains("PR/L") -> dutyFactory.createRyanairDuty(type = RyanairDutyType.PARENTAL_LEAVE)

      else -> {
        dutyFactory.createRyanairDuty(
          type = RyanairDutyType.SPECIAL_EVENT,
          specialEventType = text
        )
      }
    }

    // All standby duties for pilots are home standbys
    if (isPilot && dutyType.type == RyanairDutyType.AIRPORT_STANDBY) {
      dutyType.type = RyanairDutyType.HOME_STANDBY
    }

    return dutyType
  }

  /**
   * Generates and adds the description to [duty].
   */
  fun populateDescription(duty: Duty) {
    val description = when (duty.type) {
      RyanairDutyType.ANNUAL_LEAVE -> app.getString(R.string.ryanair_description_annual_leave)
      RyanairDutyType.AIRPORT_STANDBY -> app.getString(R.string.ryanair_description_airport_standby)
      RyanairDutyType.BANK_HOLIDAY -> app.getString(R.string.ryanair_description_bank_holiday)
      RyanairDutyType.HOME_STANDBY -> app.getString(R.string.ryanair_description_home_standby)
      RyanairDutyType.OFF -> app.getString(R.string.ryanair_description_off)
      RyanairDutyType.PARENTAL_LEAVE -> app.getString(R.string.ryanair_description_parental_leave)
      RyanairDutyType.SICK -> app.getString(R.string.ryanair_description_sick)
      RyanairDutyType.UNPAID_LEAVE -> app.getString(R.string.ryanair_description_unpaid_leave)
      RyanairDutyType.SPECIAL_EVENT -> duty.specialEventType
      else -> ""
    }

    duty.description = description
  }
}