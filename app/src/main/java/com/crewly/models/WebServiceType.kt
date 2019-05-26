package com.crewly.models

/**
 * Created by Derek on 16/08/2018
 * Definition of the services used by each airline for managing their employees.
 */
sealed class WebServiceType(
  val serviceName: String,
  val baseUrl: String,
  val loginPath: String,
  val failedLoginPath: String,
  val userPortalPath: String,
  val crewRosterPath: String,
  val pilotRosterPath: String
) {

  val loginUrl = "$baseUrl$loginPath"

  class CrewDock: WebServiceType(
    serviceName = "Crewdock",
    baseUrl = "https://crewdock.com/pport/",
    loginPath = "web/Login",
    failedLoginPath = "web",
    userPortalPath = "web/Portal",
    crewRosterPath = "Cabin%20Crew/Operational/Roster",
    pilotRosterPath = "Pilot/Personal/Roster"
  ) {
    val restrictPath = "Restrict"
    val crewSunPath = "(Sun)"
    val crewSunRosterPath = "Cabin%20Crew%20%28Sun%29/My%20Crewdock/View%20Roster"
  }
}