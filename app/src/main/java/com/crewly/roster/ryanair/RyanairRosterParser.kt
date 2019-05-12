package com.crewly.roster.ryanair

import com.crewly.account.Account
import com.crewly.activity.ActivityScope
import com.crewly.duty.Duty
import com.crewly.duty.RyanairDutyType
import com.crewly.duty.Sector
import com.crewly.models.Company
import com.crewly.roster.Roster
import dagger.Lazy
import io.reactivex.Single
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.util.*
import javax.inject.Inject

/**
 * Created by Derek on 04/06/2018
 * Parses a fetched roster for Ryanair.
 */
@ActivityScope
class RyanairRosterParser @Inject constructor(
  private val ryanAirRosterHelper: Lazy<RyanAirRosterHelper>
) {

  companion object {
    private const val CREW_CONSECUTIVE_DAYS_ON = 5
    private const val CREW_CONSECUTIVE_DAYS_OFF = 3

    private const val PILOT_CONSECUTIVE_DAYS_ON = 6
    private const val PILOT_CONSECUTIVE_DAYS_OFF = 4
  }

  private val dateFormatter = DateTimeFormat.forPattern("dd MMM yy, E").withLocale(Locale.ENGLISH)
  private val dateTimeFormatter = DateTimeFormat.forPattern("dd MMM yy, E HH:mm").withLocale(Locale.ENGLISH)

  fun parseRosterFile(
    account: Account,
    roster: String
  ): Single<Roster> {
    return Single.fromCallable {
      val factory = XmlPullParserFactory.newInstance()
      factory.isNamespaceAware = true

      val pullParser = factory.newPullParser()
      pullParser.setInput(StringReader(extractRosterTable(roster)))

      var eventType = pullParser.next()

      val duties = mutableListOf<Duty>()
      val sectors = mutableListOf<Sector>()
      var currentDuty = Duty()
      var currentSector = Sector()
      var dutyDate = ""
      var tableDataIndex = 1

      loop@ while (true) {
        when (eventType) {
          XmlPullParser.TEXT -> {
            if (!pullParser.isWhitespace) {
              val tagText = pullParser.text.trim().toUpperCase()

              when (tableDataIndex) {
                1 -> {
                  dutyDate = tagText
                }

                2 -> {
                  val parsedDuty = ryanAirRosterHelper.get().getDutyType(tagText, account.isPilot)

                  // Skip this row if unable to parse duty type
                  if (parsedDuty.type == RyanairDutyType.UNKNOWN.dutyName) {
                    tableDataIndex = 1
                    eventType = pullParser.next()
                    continue@loop
                  } else {
                    currentDuty = parsedDuty

                    // If duty is flight type, this is a sector
                    if (currentDuty.type == RyanairDutyType.FLIGHT.dutyName) {
                      currentSector = Sector(flightId = tagText)
                    }
                  }
                }

                3 -> {
                  when (currentDuty.type) {
                    RyanairDutyType.FLIGHT.dutyName -> currentSector.departureAirport = tagText
                    else -> currentDuty.location = tagText
                  }
                }

                4 -> {
                  when (currentDuty.type) {
                    RyanairDutyType.FLIGHT.dutyName -> {
                      val departureTime = "$dutyDate ${tagText.removeSuffix("Z").trim()}"
                      currentSector.departureTime = dateTimeFormatter.parseDateTime(departureTime)
                    }

                    else -> currentDuty.date = dateFormatter.parseDateTime(dutyDate)
                  }
                }

                5 -> {
                  when (currentDuty.type) {
                    RyanairDutyType.FLIGHT.dutyName -> {
                      val arrivalTime = "$dutyDate ${tagText.removeSuffix("Z").trim()}"
                      currentSector.arrivalTime = dateTimeFormatter.parseDateTime(arrivalTime)
                    }
                  }
                }

                6 -> {
                  when (currentDuty.type) {
                    RyanairDutyType.FLIGHT.dutyName -> currentSector.arrivalAirport = tagText
                  }
                }
              }
            }
          }

          XmlPullParser.END_TAG -> {
            val tagName = pullParser.name

            when (tagName) {
              "td" -> tableDataIndex++

              "tr" -> {
                if (tableDataIndex > 3) {
                  when (currentDuty.type) {
                    RyanairDutyType.FLIGHT.dutyName -> {
                      if (sectors.isEmpty() || sectors.last().departureTime.dayOfMonth
                        != currentSector.departureTime.dayOfMonth) {
                        currentDuty.crewCode = account.crewCode
                        currentDuty.date = DateTime(currentSector.departureTime)
                        addDutyToRoster(account, duties, currentDuty)
                      }

                      currentSector.crewCode = account.crewCode
                      currentSector.crew.add(account.crewCode)
                      currentSector.company = Company.Ryanair
                      sectors.add(currentSector)
                    }

                    else -> {
                      currentDuty.crewCode = account.crewCode
                      addDutyToRoster(account, duties, currentDuty)
                    }
                  }
                }

                tableDataIndex = 1
              }

              "data" -> break@loop
            }
          }
        }

        eventType = pullParser.next()
      }

      populateUserBase(account, duties)
      addFutureDuties(account, duties)

      Roster(
        duties = duties,
        sectors = sectors
      )
    }
  }

  private fun populateUserBase(
    account: Account,
    duties: List<Duty>
  ) {
    duties.find { duty -> duty.type == RyanairDutyType.HOME_STANDBY.dutyName }?.let {
      account.base = it.location
    }
  }

  /**
   * Extracts the roster table from the HTML file. Also wraps the extracted table in <data> tags
   * in order to detect the start and end of the file while looping through it.
   */
  private fun extractRosterTable(roster: String): String {
    val openingIndex = roster.indexOf("<tbody class=\"table-text\">")
    val endingIndex = roster.findAnyOf(listOf("</tbody>"), openingIndex, false)

    endingIndex?.let {
      var extractedRoster = roster.subSequence(openingIndex, it.first + 8)
      extractedRoster = "<data>$extractedRoster"
      extractedRoster += "</data>"
      return extractedRoster
    }

    return roster
  }

  /**
   * Adds [dutyToAdd] as the next duty in [duties]. Will also fill in any missing information
   * needed between the duty days.
   */
  private fun addDutyToRoster(
    account: Account,
    duties: MutableList<Duty>,
    dutyToAdd: Duty
  ) {
    if (duties.isNotEmpty()) {
      val lastDuty = duties.last()
      val isCurrentDay = lastDuty.date.dayOfMonth() == dutyToAdd.date.dayOfMonth()
      val isNextDay = lastDuty.date.plusDays(1).dayOfMonth() == dutyToAdd.date.dayOfMonth()

      if (!isCurrentDay && !isNextDay) {
        addMissingDutyDays(account, duties, dutyToAdd)
      }
    }

    duties.add(dutyToAdd)
  }

  /**
   * Adds any missing days between the last parsed duty in [duties] and the next [dutyToAdd]
   * as [RyanairDutyType.OFF] days. This is required as the roster can have 'missing' days.
   */
  private fun addMissingDutyDays(
    account: Account,
    duties: MutableList<Duty>,
    dutyToAdd: Duty
  ) {
    val lastDay = dutyToAdd.date.withTimeAtStartOfDay()
    var currentDay = duties.last().date.withTimeAtStartOfDay().plusDays(1)
    while (currentDay.isBefore(lastDay.millis)) {
      duties.add(Duty(type = RyanairDutyType.OFF.dutyName, crewCode = account.crewCode,
        date = currentDay))
      currentDay = currentDay.plusDays(1)
    }
  }

  /**
   * Add future duties after the user's [rosterDuties]. This is based on a pattern of x amount
   * of days on followed by x amount of days off.
   */
  private fun addFutureDuties(
    account: Account,
    rosterDuties: MutableList<Duty>
  ) {
    val daysOn = if (account.isPilot) PILOT_CONSECUTIVE_DAYS_ON else CREW_CONSECUTIVE_DAYS_ON
    val daysOff = if (account.isPilot) PILOT_CONSECUTIVE_DAYS_OFF else CREW_CONSECUTIVE_DAYS_OFF
    val lastRosterDate = rosterDuties.last().date
    val endDate = DateTime(lastRosterDate).dayOfMonth().withMaximumValue()
    val lastDay = 365 + (endDate.dayOfMonth - lastRosterDate.dayOfMonth) + 1
    var daysOnCount = 0
    var daysOffCount = 0

    /*
     * Loop through the last days of the roster to determine how many consecutive days on/off
     * there is at the end of the roster. This will allow us to continue the pattern of x days
     * on/off for the future.
     */
    if (rosterDuties.last().type == RyanairDutyType.OFF.dutyName) {
      loop@ for (i in 1 until daysOff) {
        val duty = rosterDuties[rosterDuties.size - i]
        if (duty.type != RyanairDutyType.OFF.dutyName) {
          daysOffCount = i
          break@loop
        }
      }

      if (daysOffCount >= daysOff) {
        daysOnCount = 0
        daysOffCount = 0
      }

    } else {
      loop@ for (i in 1 until daysOn) {
        val duty = rosterDuties[rosterDuties.size - 1]
        if (duty.type == RyanairDutyType.OFF.dutyName) {
          daysOnCount = i
          break@loop
        }
      }
    }

    for (i in 1 until lastDay) {
      val nextDuty: Duty = if (daysOnCount < daysOn) {
        daysOnCount++
        Duty(type = RyanairDutyType.UNKNOWN.dutyName)

      } else {
        if (++daysOffCount >= daysOff) {
          daysOnCount = 0
          daysOffCount = 0
        }

        Duty(type = RyanairDutyType.OFF.dutyName)
      }

      nextDuty.date = DateTime(lastRosterDate).plusDays(i)
      nextDuty.crewCode = account.crewCode
      rosterDuties.add(nextDuty)
    }
  }
}