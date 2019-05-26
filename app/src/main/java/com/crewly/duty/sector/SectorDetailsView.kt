package com.crewly.duty.sector

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.crewly.R
import com.crewly.db.sector.Sector
import com.crewly.utils.getColorCompat
import com.crewly.utils.smartPadding
import kotlinx.android.synthetic.main.sector_details_view.view.*
import org.joda.time.format.DateTimeFormat

/**
 * Created by Derek on 16/07/2018
 */
class SectorDetailsView: ConstraintLayout {

  companion object {
    private val timeFormatter = DateTimeFormat.forPattern("HH:mm")
  }

  constructor(context: Context): super(context)
  constructor(context: Context, attributes: AttributeSet?): super(context, attributes)
  constructor(context: Context, attributes: AttributeSet?, defStyle: Int = 0): super(context, attributes, defStyle)

  var sector: Sector? = null
    set(value) {
      if (value != null) {
        displayTimes(value)
        displayAirports(value)
        displayFlightId(value)
      }

      field = value
    }

  init {
    View.inflate(context, R.layout.sector_details_view, this)
    setBackgroundColor(context.getColorCompat(R.color.highlight_background))
    val verticalPadding = context.resources.getDimensionPixelOffset(R.dimen.sector_details_vertical_padding)
    smartPadding(topPadding = verticalPadding, bottomPadding = verticalPadding)
  }

  fun includeBottomMargin(include: Boolean) {
    val bottomMargin = if (include) {
      context.resources.getDimensionPixelOffset(R.dimen.sector_details_bottom_margin)
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

  private fun displayTimes(sector: Sector) {
    text_departure_time.text = timeFormatter.print(sector.departureTime)
    text_arrival_time.text = timeFormatter.print(sector.arrivalTime)
  }

  private fun displayAirports(sector: Sector) {
    text_departure_airport.text = sector.departureAirport
    text_arrival_airport.text = sector.arrivalAirport
  }

  private fun displayFlightId(sector: Sector) {
    text_flight_id.text = sector.flightId
  }
}