package com.crewly.crew

/**
 * Created by Derek on 02/06/2018
 */
data class Crew(
  var code: String = "",
  var name: String = "",
  var crew: List<Crew> = listOf()
)