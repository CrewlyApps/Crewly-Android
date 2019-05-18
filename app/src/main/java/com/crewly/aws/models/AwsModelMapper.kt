package com.crewly.aws.models

import com.crewly.account.Account
import com.crewly.crew.Rank
import com.crewly.db.Crew
import com.crewly.duty.Airport
import com.crewly.duty.Flight
import com.crewly.duty.Sector
import com.crewly.models.Company
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatterBuilder
import javax.inject.Inject

/**
 * Created by Derek on 05/05/2019
 */
class AwsModelMapper @Inject constructor() {

  private val awsDateFormatter = DateTimeFormatterBuilder()
    .appendYear(4, 4)
    .appendLiteral('-')
    .appendMonthOfYear(2)
    .appendLiteral('-')
    .appendDayOfMonth(2)
    .toFormatter()

  private val awsIdDateFormatter = DateTimeFormat.forPattern("yyyyMMdd")

  fun crewToAwsUser(
    crew: Crew
  ): AwsUser =
    AwsUser().apply {
      id = crew.id
      companyId = crew.company.id
      base = crew.base
      name = crew.name
      rankId = crew.rank.getValue()
      isPilot = crew.isPilot
      isVisible = crew.showCrew
      joinedDate = awsDateFormatter.print(crew.joinedCompanyAt)
      lastSeenDate = awsDateFormatter.print(DateTime())
    }

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
      joinedDate = awsDateFormatter.print(account.joinedCompanyAt)
      lastSeenDate = awsDateFormatter.print(DateTime())
    }

  fun awsUserToCrew(
    awsUser: AwsUser
  ): Crew =
    Crew(
      id = awsUser.id,
      name = awsUser.name,
      company = Company.fromId(awsUser.companyId),
      base = awsUser.base,
      rank = Rank.fromRank(awsUser.rankId),
      isPilot = awsUser.isPilot,
      showCrew = awsUser.isVisible,
      joinedCompanyAt = awsDateFormatter.parseDateTime(awsUser.joinedDate),
      lastSeenAt = awsDateFormatter.parseDateTime(awsUser.lastSeenDate)
    )

  fun flightToAwsFlight(
    flight: Flight
  ): AwsFlight =
    AwsFlight().apply {
      id = generateAwsFlightId(flight)
      companyId = flight.departureSector.company.id
      airportOrigin = flight.departureAirport.codeIata
      countryOrigin = flight.departureAirport.country
      date = awsDateFormatter.print(flight.departureSector.departureTime)
      crewIds = flight.departureSector.crew.toSet()
    }

  fun awsFlightToFlight(
    awsFlight: AwsFlight
  ): Flight {
    val splitId = awsFlight.id.split('_')
    return Flight(
      departureSector = Sector(
        departureTime = awsIdDateFormatter.parseDateTime(splitId.getOrNull(0) ?: ""),
        flightId = splitId.getOrNull(1) ?: "",
        departureAirport = awsFlight.airportOrigin,
        arrivalAirport = splitId.getOrNull(3) ?: "",
        company = Company.fromId(awsFlight.companyId),
        crew = awsFlight.crewIds.toMutableList()
      ),

      departureAirport = Airport(
        codeIata = awsFlight.airportOrigin,
        country = awsFlight.countryOrigin
      )
    )
  }

  fun generateAwsFlightId(
    flight: Flight
  ): String {
    val formattedDate = awsDateFormatter
      .print(flight.departureSector.departureTime)
      .replace("-", "")

    return flight.run {
      "${formattedDate}_${departureSector.flightId}_${departureAirport.codeIata}_${arrivalAirport.codeIata}"
    }
  }
}