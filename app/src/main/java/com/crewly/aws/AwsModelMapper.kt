package com.crewly.aws

import com.crewly.account.Account
import com.crewly.aws.models.AwsFlight
import com.crewly.aws.models.AwsUser
import com.crewly.duty.Flight
import com.crewly.models.Crew
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatterBuilder
import javax.inject.Inject

/**
 * Created by Derek on 05/05/2019
 */
class AwsModelMapper @Inject constructor() {

  private val dateTimeFormatter = DateTimeFormatterBuilder()
    .appendYear(4, 4)
    .appendLiteral('-')
    .appendMonthOfYear(2)
    .appendLiteral('-')
    .appendDayOfMonth(2)
    .toFormatter()

  fun accountToAwsUser(
    account: Account
  ): AwsUser =
    AwsUser().apply {
      id = account.crewCode
      companyId = account.company.id
      base = account.base
      name = account.name
      rankId = account.rank.getValue()
      isPilot = account.isPilot
      isVisible = account.showCrew
      joinedDate = dateTimeFormatter.print(account.joinedCompanyAt)
      lastSeenDate = dateTimeFormatter.print(DateTime())
    }

  fun awsUserToCrew(
    awsUser: AwsUser
  ): Crew =
    Crew(
      id = awsUser.id,
      name = awsUser.name
    )

  fun flightToAwsFlight(
    crewId: String,
    flight: Flight
  ): AwsFlight =
    AwsFlight().apply {
      id = generateAwsFlightId(flight)
      companyId = flight.departureSector.company.id
      airportOrigin = flight.departureAirport.codeIata
      countryOrigin = flight.departureAirport.country
      date = dateTimeFormatter.print(flight.departureSector.departureTime)
      crewIds = setOf(crewId)
    }

  fun generateAwsFlightId(
    flight: Flight
  ): String {
    val formattedDate = dateTimeFormatter
      .print(flight.departureSector.departureTime)
      .replace("-", "")

    return flight.run {
      "${formattedDate}_${departureSector.flightId}_${departureAirport.codeIata}_${arrivalAirport.codeIata}"
    }
  }
}