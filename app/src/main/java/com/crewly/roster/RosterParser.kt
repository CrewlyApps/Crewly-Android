package com.crewly.roster

import com.crewly.account.AccountManager
import com.crewly.app.CrewlyDatabase
import com.crewly.duty.DutyType
import com.crewly.duty.Sector
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
 */
class RosterParser @Inject constructor(private val crewlyDatabase: CrewlyDatabase,
                                       private val accountManager: AccountManager) {

    companion object {
        private const val CREW_CONSECUTIVE_DAYS_ON = 5
        private const val CREW_CONSECUTIVE_DAYS_OFF = 3

        private const val PILOT_CONSECUTIVE_DAYS_ON = 6
        private const val PILOT_CONSECUTIVE_DAYS_OFF = 4
    }

    private val dateFormatter = DateTimeFormat.forPattern("dd MMM yy, E").withLocale(Locale.ENGLISH)
    private val dateTimeFormatter = DateTimeFormat.forPattern("dd MMM yy, E HH:mm").withLocale(Locale.ENGLISH)

    fun parseRosterFile(roster: String): Completable {
        try {
            val account = accountManager.getCurrentAccount()
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true

            val pullParser = factory.newPullParser()
            pullParser.setInput(StringReader(extractRosterTable(roster)))

            var eventType = pullParser.next()

            val dutyTypes = mutableListOf<DutyType>()
            val sectors = mutableListOf<Sector>()
            var currentDuty = DutyType()
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
                                    val parsedDuty = parseDutyType(tagText)

                                    // Skip this row if unable to parse duty type
                                    if (parsedDuty == null) {
                                        tableDataIndex = 1
                                        eventType = pullParser.next()
                                        continue@loop
                                    } else {
                                        currentDuty = parsedDuty

                                        // If no duty type, this is a sector
                                        if (currentDuty.type == DutyType.NONE) { currentSector = Sector(flightId = tagText) }
                                    }
                                }

                                3 -> {
                                    when (currentDuty.type) {
                                        DutyType.NONE-> currentSector.departureAirport = tagText
                                        else -> currentDuty.location = tagText
                                    }
                                }

                                4 -> {
                                    when (currentDuty.type) {
                                        DutyType.NONE -> {
                                            val departureTime = "$dutyDate $tagText"
                                            currentSector.departureTime = dateTimeFormatter.parseDateTime(departureTime)
                                        }

                                        else -> currentDuty.date = dateFormatter.parseDateTime(dutyDate)
                                    }
                                }

                                5 -> {
                                    when (currentDuty.type) {
                                        DutyType.NONE -> {
                                            val arrivalTime = "$dutyDate $tagText"
                                            currentSector.arrivalTime = dateTimeFormatter.parseDateTime(arrivalTime)
                                        }
                                    }
                                }

                                6 -> {
                                    when (currentDuty.type) {
                                        DutyType.NONE -> currentSector.arrivalAirport = tagText
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
                                when (currentDuty.type) {
                                    DutyType.NONE -> {
                                        if (sectors.isEmpty() || sectors.last().departureTime.dayOfMonth
                                                != currentSector.departureTime.dayOfMonth) {
                                            currentDuty.crewCode = account.crewCode
                                            currentDuty.date = DateTime(currentSector.departureTime)
                                            dutyTypes.add(currentDuty)
                                        }

                                        sectors.add(currentSector)
                                    }

                                    else -> {
                                        currentDuty.crewCode = account.crewCode
                                        dutyTypes.add(currentDuty)
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

            addFutureDuties(dutyTypes)

            return clearDatabase()
                    .mergeWith(saveDuties(dutyTypes))
                    .mergeWith(saveSectors(sectors))

        } catch (exc: Exception) { return Completable.error(exc) }
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
     * Parse the duty type from a string representation.
     */
    private fun parseDutyType(text: String): DutyType? {
        return when {
            text.matches(Regex("[0-9]+")) -> DutyType(type = DutyType.NONE)
            text.contains("HSBY") -> DutyType(type = DutyType.HSBY)
            text.contains("SBY") || (text.contains("AD") && !text.contains("CADET")) -> DutyType(type = DutyType.ASBY)
            text.startsWith("OFF") -> DutyType(type = DutyType.OFF)
            text.contains("SICK") -> DutyType(type = DutyType.SICK)
            text.contains("B/HOL") -> DutyType(type = DutyType.BANK_HOLIDAY)
            text.contains("A/L") -> DutyType(type = DutyType.ANNUAL_LEAVE)
            text.contains("U/L") -> DutyType(type = DutyType.UNPAID_LEAVE)
            text.contains("N/A") -> DutyType(type = DutyType.NOT_AVAILABLE)
            text.contains("PR/L") || text.contains("P/L") -> DutyType(type = DutyType.PARENTAL_LEAVE)
            else -> parseSpecialEvent(text)
        }
    }

    /**
     * Parses a special event duty type from a string representation.
     */
    private fun parseSpecialEvent(text: String): DutyType? {
        var eventDescription: String? = null

        when (text) {
            "CLCK" -> {
                eventDescription = "Annual LCK rostered: JU/PU being checked"
            }

            "ICLCK" -> {
                eventDescription = "Annual LCK rostered: PU conducting check"
            }

            "PICLCK" -> {
                eventDescription = "Annual LCK rostered: PU (LC) conducting check on another PU"
            }

            "LCK", "ILCK" -> {
                eventDescription = "Line check"
            }

            "NOMED" -> {
                eventDescription = "No medical certificate"
            }

            "CSS", "CSS PU" -> {
                eventDescription = "PU promotion training course"
            }

            "CUST/C" -> {
                eventDescription = "Sales training"
            }

            "CRMS", "ICRMS" -> {
                eventDescription = "Annual recurrent training"
            }

            "STUDY DAY" -> {
                eventDescription = "Study day"
            }

            "MEETING" -> {
                eventDescription = "Internal meeting"
            }

            "INTERVIEW" -> {
                eventDescription = "Interview"
            }

            "SNYR", "SNY" -> {
                eventDescription = "Flying as supernumerous"
            }

            "SNY CADET" -> {
                eventDescription = "Supernumerous flight"
            }

            "PU" -> {
                eventDescription = "Flying as number one"
            }

            "3RTENROL" -> {
                eventDescription = "You have a fire training available"
            }

            "3R/T", "I3R/T" -> {
                eventDescription = "Fire training"
            }

            "WOFF", "IWOFF" -> {
                eventDescription = "Working on a day off"
            }

            "S/D", "S/D(Z)", "S/D (Z)", "S/DT" -> {
                eventDescription = "Special duty"
            }

            "G/D", "G/D(Z)", "G/D (Z)", "G/DT", "GRD SCH" -> {
                eventDescription = "Ground duty"
            }

            "P/D", "P/D(Z)", "P/D (Z)", "P/DT", "P/D UK" -> {
                eventDescription = "Pregnant duty"
            }

            "DRIVE", "GT/DRIVE", "GT", "GT G/T", "G/T", "CAR", "SGT", "SGT G/T", "TAXI", "TRAIN" -> {
                eventDescription = "Ground transportation"
            }

            "OSCARS" -> {
                eventDescription = "Awards day"
            }

            "TBR" -> {
                eventDescription = "To be rostered"
            }

            "AGB", "IAGB" -> {
                eventDescription = "Always Getting Better programme"
            }

            "SPLIT" -> {
                eventDescription = "This day was split into multiple duties."
            }

            "EXPCHK" -> {
                eventDescription = "Expired check flying license is out of date, you can't fly."
            }

            "RECON" -> {
                eventDescription = "Reconversion after maternity leave"
            }

            "NTSP", "INTSP" -> {
                eventDescription = "Night stop or overnight"
            }

            "DSGD" -> {
                eventDescription = "Deputy supervisor ground duty"
            }

            "BSGD" -> {
                eventDescription = "Base supervisor ground duty"
            }

            "BUS" -> {
                eventDescription = "Travel by bus"
            }

            "ICSS" -> {
                eventDescription = "Classroom training for promotion to CSS"
            }

            "FSF" -> {
                eventDescription = "Flight Safety Forum"
            }

            "TSIM" -> {
                eventDescription = "Training of unusual flight scenarios"
            }

            "NO ID" -> {
                eventDescription = "No identification"
            }

            "LINETRAIN" -> {
                eventDescription = "Line training"
            }

            "C/L" -> {
                eventDescription = "Compassionate leave"
            }

            "ASSESS" -> {
                eventDescription = "Assessment for a new position or role"
            }

            "+MNPS", "MNPS" -> {
                eventDescription = "Minimum Navigation Performance Specifications check"
            }

            "TRNREST" -> {
                eventDescription = "Training rest"
            }

            "LOPS" -> {
                eventDescription = "Line operations"
            }

            "SPF" -> {
                eventDescription = "Safety pilot flight"
            }

            "SFTY" -> {
                eventDescription = "Safety day"
            }

            "BTRG" -> {
                eventDescription = "Base training"
            }

            "UA/A" -> {
                eventDescription = "Unauthorised absence"
            }

            "45TG" -> {
                eventDescription = "Check after being off"
            }

            "IGS" -> {
                eventDescription = "Instructing Ground School"
            }

            "6TRG" -> {
                eventDescription = "6th day of the week due to training"
            }

            "WINGS" -> {
                eventDescription = "Wings ceremony"
            }

            "OVN" -> {
                eventDescription = "Overnight"
            }

            "COACH", "U/SELF", "FLEXI", "CLMT", "ESCORT", "EXS", "DIFF CR", "ISD" -> {
                eventDescription = "Unknown code. To improve this description, please contact me if you know what it means."
            }
        }

        if (eventDescription == null) {
            when {
                text.contains("DH") -> {
                    eventDescription = "Dead headed"
                }

                text.contains("RST") -> {
                    eventDescription = "Recursive simulator training"
                }

                text.contains("SIM") -> {
                    eventDescription = "Simulator training"
                }
            }
        }

        return if (eventDescription != null) DutyType(type = DutyType.SPECIAL_EVENT, description = eventDescription) else null
    }

    /**
     * Add future duties after the user's rostered duties. This is based on a pattern of x amount
     * of days on followed by x amount of days off.
     */
    private fun addFutureDuties(rosterDuties: MutableList<DutyType>) {
        val account = accountManager.getCurrentAccount()
        val daysOn = if (account.isPilot) PILOT_CONSECUTIVE_DAYS_ON else CREW_CONSECUTIVE_DAYS_ON
        val daysOff = if (account.isPilot) PILOT_CONSECUTIVE_DAYS_OFF else CREW_CONSECUTIVE_DAYS_OFF
        val lastRosterDate = rosterDuties.last().date
        val endDate = DateTime(lastRosterDate).dayOfMonth().withMaximumValue()
        val lastDay = 365 + (endDate.dayOfMonth - lastRosterDate.dayOfMonth) + 1
        var daysOnCount = 0
        var daysOffCount = 0

        for (i in 0 until lastDay) {
            val nextDuty: DutyType = if (daysOnCount < daysOn) {
                daysOnCount++
                DutyType(type = DutyType.NONE)

            } else {
                if (++daysOffCount >= daysOff) {
                    daysOnCount = 0
                    daysOffCount = 0
                }

                DutyType(type = DutyType.OFF)
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

    private fun saveDuties(duties: List<DutyType>): Completable {
        return Completable.fromAction { crewlyDatabase.dutyDao().insertDuties(duties) }
    }

    private fun saveSectors(sectors: List<Sector>): Completable {
        return Completable.fromAction { crewlyDatabase.sectorDao().insertSectors(sectors) }
    }
}