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
                                2 -> { currentDuty = parseDutyType(tagText) }

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
    private fun parseDutyType(text: String): DutyType {
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
            else -> DutyType.Off()
        }
    }
}