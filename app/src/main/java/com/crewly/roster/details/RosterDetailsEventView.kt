package com.crewly.roster.details

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.crewly.R
import com.crewly.duty.SpecialEvent
import com.crewly.utils.getColorCompat
import kotlinx.android.synthetic.main.roster_details_event_view.view.*

/**
 * Created by Derek on 18/08/2018
 * Display information for a special event on the roster details screen.
 */
class RosterDetailsEventView @JvmOverloads constructor(context: Context,
                                                       attributes: AttributeSet? = null,
                                                       defStyle: Int = 0):
        ConstraintLayout(context, attributes, defStyle) {

    var specialEvent: SpecialEvent? = null
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

    private fun displayEvent(specialEvent: SpecialEvent) {
        text_event_name.text = specialEvent.type
        text_event_description.text = specialEvent.description
    }
}