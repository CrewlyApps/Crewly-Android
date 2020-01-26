package com.crewly.duty.sector

import com.crewly.models.sector.Sector

class SectorViewData(
  val sector: Sector,
  val arrivalTimeZulu: String,
  val arrivalTimeLocal: String,
  val departureTimeZulu: String,
  val departureTimeLocal: String,
  val duration: String
)