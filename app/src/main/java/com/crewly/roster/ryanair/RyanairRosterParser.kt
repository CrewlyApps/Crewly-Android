package com.crewly.roster.ryanair

import com.crewly.persistence.duty.Duty
import com.crewly.persistence.sector.Sector
import com.crewly.duty.DutyFactory
import com.crewly.duty.ryanair.RyanairDutyType
import com.crewly.models.Company
import com.crewly.models.account.Account
import com.crewly.models.roster.Roster
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
class RyanairRosterParser @Inject constructor(
  private val ryanAirRosterHelper: Lazy<RyanAirRosterHelper>,
  private val dutyFactory: DutyFactory
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
      var currentDuty = dutyFactory.createRyanairDuty()
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
                  if (parsedDuty.type == RyanairDutyType.UNKNOWN) {
                    tableDataIndex = 1
                    eventType = pullParser.next()
                    continue@loop
                  } else {
                    currentDuty = parsedDuty

                    // If duty is flight type, this is a sector
                    if (currentDuty.type == RyanairDutyType.FLIGHT) {
                      currentSector = Sector(flightId = tagText)
                    }
                  }
                }

                3 -> {
                  when (currentDuty.type) {
                    RyanairDutyType.FLIGHT-> currentSector.departureAirport = tagText
                    else -> currentDuty.location = tagText
                  }
                }

                4 -> {
                  when (currentDuty.type) {
                    RyanairDutyType.FLIGHT -> currentSector.departureTime = parseRosterTime(
                      dateText = dutyDate,
                      timeText = tagText
                    )

                    else -> currentDuty.startTime = parseRosterTime(
                      dateText = dutyDate,
                      timeText = tagText
                    )
                  }
                }

                5 -> {
                  when (currentDuty.type) {
                    RyanairDutyType.FLIGHT -> currentSector.arrivalTime = parseRosterTime(
                      dateText = dutyDate,
                      timeText = tagText
                    )

                    else -> currentDuty.endTime = parseRosterTime(
                      dateText = dutyDate,
                      timeText = tagText
                    )
                  }
                }

                6 -> {
                  when (currentDuty.type) {
                    RyanairDutyType.FLIGHT -> currentSector.arrivalAirport = tagText
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
                    RyanairDutyType.FLIGHT -> {
                      if (sectors.isEmpty() || sectors.last().departureTime.dayOfMonth
                        != currentSector.departureTime.dayOfMonth) {
                        currentDuty.ownerId = account.crewCode
                        currentDuty.startTime = DateTime(currentSector.departureTime)
                        addDutyToRoster(account, duties, currentDuty)
                      }

                      currentSector.ownerId = account.crewCode
                      currentSector.crew.add(account.crewCode)
                      currentSector.company = Company.Ryanair
                      sectors.add(currentSector)
                    }

                    else -> {
                      currentDuty.ownerId = account.crewCode
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
    duties.find { duty -> duty.type == RyanairDutyType.HOME_STANDBY }?.let {
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
      val isCurrentDay = lastDuty.startTime.dayOfMonth() == dutyToAdd.startTime.dayOfMonth()
      val isNextDay = lastDuty.startTime.plusDays(1).dayOfMonth() == dutyToAdd.startTime.dayOfMonth()

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
    val lastDay = dutyToAdd.startTime.withTimeAtStartOfDay()
    var currentDay = duties.last().startTime.withTimeAtStartOfDay().plusDays(1)
    while (currentDay.isBefore(lastDay.millis)) {
      duties.add(dutyFactory.createRyanairDuty(
        type = RyanairDutyType.OFF,
        ownerId = account.crewCode,
        startTime = currentDay
      ))
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
    val lastRosterDate = rosterDuties.last().startTime
    val endDate = DateTime(lastRosterDate).dayOfMonth().withMaximumValue()
    val lastDay = 365 + (endDate.dayOfMonth - lastRosterDate.dayOfMonth) + 1
    var daysOnCount = 0
    var daysOffCount = 0

    /*
     * Loop through the last days of the roster to determine how many consecutive days on/off
     * there is at the end of the roster. This will allow us to continue the pattern of x days
     * on/off for the future.
     */
    if (rosterDuties.last().type == RyanairDutyType.OFF) {
      loop@ for (i in 1 until daysOff) {
        val duty = rosterDuties[rosterDuties.size - i]
        if (duty.type != RyanairDutyType.OFF) {
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
        if (duty.type == RyanairDutyType.OFF) {
          daysOnCount = i
          break@loop
        }
      }
    }

    for (i in 1 until lastDay) {
      val dutyType = if (daysOnCount < daysOn) {
        daysOnCount++
        RyanairDutyType.UNKNOWN

      } else {
        if (++daysOffCount >= daysOff) {
          daysOnCount = 0
          daysOffCount = 0
        }

        RyanairDutyType.OFF
      }

      rosterDuties.add(dutyFactory.createRyanairDuty(
        type = dutyType,
        startTime = DateTime(lastRosterDate).plusDays(i),
        ownerId = account.crewCode
      ))
    }
  }

  private fun parseRosterTime(
    dateText: String,
    timeText: String
  ): DateTime {
    val cleanedTimeText = if (timeText.toCharArray().firstOrNull()?.isDigit() == true) {
      timeText
    } else { "" }

    return if (cleanedTimeText.isBlank()) {
      dateFormatter.parseDateTime(dateText)
    } else {
      dateTimeFormatter.parseDateTime(
        "$dateText ${cleanedTimeText.removeSuffix("Z").trim()}"
      )
    }
  }
}