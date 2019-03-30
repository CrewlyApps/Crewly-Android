package com.crewly.roster.details

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.crewly.R
import com.crewly.duty.Duty
import com.crewly.duty.RyanairDutyType
import com.crewly.utils.getColorCompat
import kotlinx.android.synthetic.main.roster_details_event_view.view.*

/**
 * Created by Derek on 18/08/2018
 * Display information for an event on the roster details screen.
 */
class RosterDetailsEventView @JvmOverloads constructor(
  context: Context,
  attributes: AttributeSet? = null,
  defStyle: Int = 0
):
  ConstraintLayout(context, attributes, defStyle) {

  var duty: Duty? = null
    set (value) {
      if (value != null) {
        displayEvent(value)
      }

      field = value
    }

  init {
    setBackgroundColor(context.getColorCompat(R.color.roster_details_sector_background))
    View.inflate(context, R.layout.roster_details_event_view, this)
    setUpPadding()
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

  private fun displayEvent(duty: Duty) {
    text_event_name.text = if (duty.type == RyanairDutyType.SPECIAL_EVENT.dutyName) duty.specialEventType else duty.type
    text_event_description.text = duty.description
  }
}