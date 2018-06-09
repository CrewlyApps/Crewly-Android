package com.crewly.roster

import org.joda.time.format.DateTimeFormat
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.util.*

/**
 * Created by Derek on 04/06/2018
 */
class RosterParser {

    private val dateFormatter = DateTimeFormat.forPattern("dd MMM yy, E").withLocale(Locale.ENGLISH)
    private val dateTimeFormatter = DateTimeFormat.forPattern("dd MMM yy, E HH:mm").withLocale(Locale.ENGLISH)

    fun parseRosterFile(roster: String) {
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true

            val pullParser = factory.newPullParser()
            pullParser.setInput(StringReader(extractRosterTable(roster)))

            var eventType = pullParser.next()

            val dutyTypes = mutableListOf<DutyType>()
            var currentDuty: DutyType = DutyType.Off()
            var dutyDate = ""
            var tableDataIndex = 1

            loop@while (true) {
                when (eventType) {
                    XmlPullParser.TEXT -> {
                        if (!pullParser.isWhitespace) {
                            val tagText = pullParser.text.trim().toLowerCase()

                            when (tableDataIndex) {
                                1 -> { dutyDate = tagText }
                                2 -> {
                                    val parsedDuty = parseDutyType(tagText)
                                    if (parsedDuty == null) { continue@loop } else currentDuty = parsedDuty
                                }

                                3 -> {
                                    when (currentDuty) {
                                        is DutyType.Sector -> currentDuty.departureAirport = tagText
                                        else -> currentDuty.location = tagText
                                    }
                                }

                                4 -> {
                                    when (currentDuty) {
                                        is DutyType.Sector -> {
                                            val departureTime = "$dutyDate $tagText"
                                            currentDuty.departureTime = dateTimeFormatter.parseDateTime(departureTime)
                                        }

                                        else -> currentDuty.date = dateFormatter.parseDateTime(dutyDate)
                                    }
                                }

                                5 -> {
                                    when (currentDuty) {
                                        is DutyType.Sector -> {
                                            val arrivalTime = "$dutyDate $tagText"
                                            currentDuty.arrivalTime = dateTimeFormatter.parseDateTime(arrivalTime)
                                        }
                                    }
                                }

                                6 -> {
                                    when (currentDuty) {
                                        is DutyType.Sector -> currentDuty.arrivalAirport = tagText
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
                                dutyTypes.add(currentDuty)
                                tableDataIndex = 1
                            }

                            "data" -> break@loop
                        }
                    }
                }

                eventType = pullParser.next()
            }

        } catch (exc: Exception) { exc.printStackTrace() }
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
            text.matches(Regex("[0-9]+")) -> DutyType.Sector()
            text.contains("hsby") -> DutyType.HSBY()
            text.contains("sby") || (text.contains("ad") && !text.contains("cadet")) -> DutyType.ASBY()
            text.startsWith("off") -> DutyType.Off()
            text.contains("sick") -> DutyType.Sick()
            text.contains("b/hol") -> DutyType.BankHoliday()
            text.contains("a/l") -> DutyType.AnnualLeave()
            text.contains("u/l") -> DutyType.UnpaidLeave()
            text.contains("n/a") -> DutyType.NotAvailable()
            text.contains("pr/l") || text.contains("p/l") -> DutyType.ParentalLeave()
            else -> parseSpecialEvent(text)
        }
    }

    /**
     * Parses a special event duty type from a string representation.
     */
    private fun parseSpecialEvent(text: String): DutyType.SpecialEvent? {
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

        return if (eventDescription != null) DutyType.SpecialEvent(description = eventDescription) else null
    }
}