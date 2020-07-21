package com.crewly.views.flight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.crewly.R
import com.crewly.models.flight.Flight
import com.crewly.utils.getColorCompat
import com.crewly.utils.smartPadding
import kotlinx.android.synthetic.main.flight_details_view.view.*

/**
 * Created by Derek on 16/07/2018
 */
class FlightDetailsView: ConstraintLayout {

  constructor(context: Context): super(context)
  constructor(context: Context, attributes: AttributeSet?): super(context, attributes)
  constructor(context: Context, attributes: AttributeSet?, defStyle: Int = 0): super(context, attributes, defStyle)

  var flightData: FlightViewData? = null
    set(value) {
      if (value != null) {
        displayTimes(value)
        displayAirports(value.flight)
        displayFlightId(value.flight)
        displayDuration(value.duration)
      }

      field = value
    }

  init {
    View.inflate(context, R.layout.flight_details_view, this)
    setBackgroundColor(context.getColorCompat(R.color.highlight_background))
    val verticalPadding = context.resources.getDimensionPixelOffset(R.dimen.flight_details_vertical_padding)
    smartPadding(topPadding = verticalPadding, bottomPadding = verticalPadding)
  }

  fun includeBottomMargin(include: Boolean) {
    val bottomMargin = if (include) {
      context.resources.getDimensionPixelOffset(R.dimen.flight_details_bottom_margin)
    } else {
      0
    }

    var layoutParams = layoutParams

    if (layoutParams != null) {
      (layoutParams as MarginLayoutParams).bottomMargin = bottomMargin
    } else {
      layoutParams = MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT)
      layoutParams.bottomMargin = bottomMargin
    }

    this.layoutParams = layoutParams
  }

  private fun displayTimes(
    data: FlightViewData
  ) {
    text_departure_time_zulu.text = data.departureTimeZulu
    text_arrival_time_zulu.text = data.arrivalTimeZulu

    with(text_departure_time_local) {
      text = data.departureTimeLocal
      isVisible = data.departureTimeLocal.isNotEmpty()
    }

    with(text_arrival_time_local) {
      text = data.arrivalTimeLocal
      isVisible = data.arrivalTimeLocal.isNotEmpty()
    }
  }

  private fun displayAirports(
    flight: Flight
  ) {
    text_departure_airport.text = flight.departureAirport.codeIata
    text_arrival_airport.text = flight.arrivalAirport.codeIata
  }

  private fun displayFlightId(
    flight: Flight
  ) {
    text_flight_id.text = flight.flightId
  }

  private fun displayDuration(
    duration: String
  ) {
    text_flight_duration.text = duration
  }
}