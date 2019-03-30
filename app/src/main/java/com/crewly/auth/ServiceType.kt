package com.crewly.auth

/**
 * Created by Derek on 16/08/2018
 * Definition of the services used by each airline for managing their employees.
 */
enum class ServiceType(
  val serviceName: String,
  val baseUrl: String
) {

  RYANAIR(
    serviceName = "Crewdock",
    baseUrl = "${CrewDockWebView.BASE_URL}${CrewDockWebView.LOGIN_URL}"
  )
}