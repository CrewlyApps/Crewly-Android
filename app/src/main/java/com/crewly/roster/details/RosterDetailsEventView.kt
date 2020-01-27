package com.crewly.roster.details

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.crewly.R
import com.crewly.models.duty.Duty
import com.crewly.utils.getColorCompat
import kotlinx.android.synthetic.main.roster_details_event_view.view.*

/**
 * Created by Derek on 18/08/2018
 * Display information for an event on the roster details screen.
 */
class RosterDetailsEventView: ConstraintLayout {

  constructor(context: Context): super(context)
  constructor(context: Context, attributes: AttributeSet?): super(context, attributes)
  constructor(context: Context, attributes: AttributeSet?, defStyle: Int = 0): super(context, attributes, defStyle)

  init {
    setBackgroundColor(context.getColorCompat(R.color.highlight_background))
    View.inflate(context, R.layout.roster_details_event_view, this)
    setUpPadding()
  }

  fun displayEvent(
    data: EventViewData
  ) {
    displayEventName(data.duty.type.name)
    displayFromLocation(data.duty.from.codeIata)
    displayStartTime(data.startTime)
    displayToLocation(data.duty.to.codeIata)
    displayEndTime(data.endTime)
  }

  fun addBottomMargin() {
    val bottomMargin = context.resources.getDimensionPixelOffset(R.dimen.roster_details_event_bottom_margin)
    var layoutParams = layoutParams

    if (layoutParams != null) {
      (layoutParams as MarginLayoutParams).bottomMargin = bottomMargin
    } else {
      layoutParams = MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT)
      layoutParams.bottomMargin = bottomMargin
    }

    this.layoutParams = layoutParams
  }

  private fun setUpPadding() {
    val verticalPadding = context.resources.getDimensionPixelOffset(R.dimen.roster_details_event_vertical_padding)
    val horizontalPadding = context.resources.getDimensionPixelOffset(R.dimen.roster_details_event_horizontal_padding)
    setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
  }

  private fun displayEventName(
    name: String
  ) {
    text_event_name.text = name
  }

  private fun displayFromLocation(
    fromLocation: String
  ) {
    text_from_location.text = fromLocation
    text_from_location.isVisible = fromLocation.isNotBlank()
  }

  private fun displayStartTime(
    time: String
  ) {
    text_from_time.text = time
    text_from_time.isVisible = time.isNotBlank()
  }

  private fun displayToLocation(
    toLocation: String
  ) {
    text_to_location.text = toLocation
    text_to_location.isVisible = toLocation.isNotBlank()
  }

  private fun displayEndTime(
    time: String
  ) {
    text_to_time.text = time
    text_to_time.isVisible = time.isNotBlank()
  }
}