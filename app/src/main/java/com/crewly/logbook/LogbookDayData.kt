package com.crewly.logbook

import com.crewly.duty.sector.SectorViewData
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
    val data: SectorViewData,
    val includeBottomMargin: Boolean
  ): LogbookDayData()
}