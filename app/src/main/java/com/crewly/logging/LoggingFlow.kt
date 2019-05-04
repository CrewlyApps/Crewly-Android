package com.crewly.logging

/**
 * Created by Derek on 04/08/2018
 */
enum class LoggingFlow(
  var loggingTag: String
) {

  ACCOUNT("Account"),
  AWS("AWS"),
  ROSTER_LIST("RosterList")
}