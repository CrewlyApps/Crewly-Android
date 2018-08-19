package com.crewly.roster

import com.crewly.account.Account
import com.crewly.activity.ActivityScope
import com.crewly.app.CrewlyDatabase
import com.crewly.duty.Duty
import com.crewly.duty.RyanairDutyType
import com.crewly.duty.Sector
import com.crewly.logging.LoggingManager
import dagger.Lazy
import io.reactivex.Completable
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
class RyanairRosterParser @Inject constructor(private val crewlyDatabase: CrewlyDatabase,
                                              private val loggingManager: LoggingManager,
                                              private val ryanAirRosterHelper: Lazy<RyanAirRosterHelper>) {

    companion object {
        private const val CREW_CONSECUTIVE_DAYS_ON = 5
        private const val CREW_CONSECUTIVE_DAYS_OFF = 3

        private const val PILOT_CONSECUTIVE_DAYS_ON = 6
        private const val PILOT_CONSECUTIVE_DAYS_OFF = 4
    }

    private val dateFormatter = DateTimeFormat.forPattern("dd MMM yy, E").withLocale(Locale.ENGLISH)
    private val dateTimeFormatter = DateTimeFormat.forPattern("dd MMM yy, E HH:mm").withLocale(Locale.ENGLISH)

    fun parseRosterFile(account: Account,
                        roster: String): Completable {
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true

            val pullParser = factory.newPullParser()
            pullParser.setInput(StringReader(extractRosterTable(roster)))

            var eventType = pullParser.next()

            val dutyTypes = mutableListOf<Duty>()
            val sectors = mutableListOf<Sector>()
            var currentDuty = Duty()
            var currentSector = Sector()
            var dutyDate = ""
            var tableDataIndex = 1

            loop@while (true) {
                when (eventType) {
                    XmlPullParser.TEXT -> {
                        if (!pullParser.isWhitespace) {
                            val tagText = pullParser.text.trim().toUpperCase()

                            when (tableDataIndex) {
                                1 -> { dutyDate = tagText }

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
                                        if (currentDuty.type == RyanairDutyType.FLIGHT.dutyName) { currentSector = Sector(flightId = tagText) }
                                    }
                                }

                                3 -> {
                                    when (currentDuty.type) {
                                        RyanairDutyType.FLIGHT.dutyName-> currentSector.departureAirport = tagText
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
                                                dutyTypes.add(currentDuty)
                                            }

                                            currentSector.crewCode = account.crewCode
                                            currentSector.crew.add(account.crewCode)
                                            sectors.add(currentSector)
                                        }

                                        else -> {
                                            currentDuty.crewCode = account.crewCode
                                            dutyTypes.add(currentDuty)
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

            addFutureDuties(account, dutyTypes)

            return clearDatabase()
                    .mergeWith(saveDuties(dutyTypes))
                    .mergeWith(saveSectors(sectors))

        } catch (exc: Exception) {
            loggingManager.logError(exc)
            return Completable.error(exc)
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
     * Add future duties after the user's rostered duties. This is based on a pattern of x amount
     * of days on followed by x amount of days off.
     */
    private fun addFutureDuties(account: Account,
                                rosterDuties: MutableList<Duty>) {
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
            loop@for (i in 1 until daysOff) {
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
            loop@for (i in 1 until daysOn) {
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

    private fun clearDatabase(): Completable {
        return Completable.fromAction {
            crewlyDatabase.dutyDao().deleteAllDuties()
            crewlyDatabase.sectorDao().deleteAllSectors()
        }
    }

    private fun saveDuties(duties: List<Duty>): Completable {
        return Completable.fromAction { crewlyDatabase.dutyDao().insertDuties(duties) }
    }

    private fun saveSectors(sectors: List<Sector>): Completable {
        return Completable.fromAction { crewlyDatabase.sectorDao().insertSectors(sectors) }
    }
}