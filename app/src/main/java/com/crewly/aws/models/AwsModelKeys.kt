package com.crewly.aws.models

/**
 * Created by Derek on 05/05/2019
 */
object AwsModelKeys {

  object Flight {
    const val ID = "id"
    const val COMPANY_ID = "companyId"
    const val AIRPORT_ORIGIN = "airportOrigin"
    const val COUNTRY_ORIGIN = "countryOrigin"
    const val DATE = "date"
    const val CREW = "crew"
  }

  object User {
    const val ID = "id"
    const val COMPANY_ID = "companyId"
    const val BASE = "base"
    const val IS_PILOT = "isPilot"
    const val IS_PREMIUM = "isPremium"
    const val IS_VISIBLE = "isVisible"
    const val JOINED_DATE = "joinedDate"
    const val LAST_SEEN_DATE = "lastSeenDate"
    const val NAME = "name"
    const val RANK_ID = "rankId"
    const val REGISTRATION_DATE = "registrationDate"
  }
}