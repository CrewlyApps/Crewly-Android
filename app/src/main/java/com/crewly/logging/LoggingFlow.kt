package com.crewly.logging

/**
 * Created by Derek on 04/08/2018
 */
enum class LoggingFlow(
  var loggingTag: String
) {

  ACCOUNT("CLOG Account"),
  AWS("CLOG AWS"),
  ROSTER_LIST("CLOG RosterList")
}