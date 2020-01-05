package com.crewly.logbook

import com.crewly.models.sector.Sector
import org.joda.time.DateTime

/**
 * Created by Derek on 22/04/2019
 */
sealed class LogbookDayData {

  data class DateHeaderData(
    val date: DateTime,
    val dutyIcon: Int
  ): LogbookDayData()

  data class SectorDetailsData(
    val sector: Sector,
    val includeBottomMargin: Boolean
  ): LogbookDayData()
}