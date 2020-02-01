package com.crewly.logbook

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.logbook_flight.view.*

/**
 * Created by Derek on 22/04/2019
 */
class LogbookFlightViewHolder(
  rootView: View
): RecyclerView.ViewHolder(rootView) {

  fun bindData(
    flightData: LogbookDayData.FlightDetailsData
  ) {
    itemView.flight_details_view.flightData = flightData.data
    itemView.flight_details_view.includeBottomMargin(flightData.includeBottomMargin)
  }
}