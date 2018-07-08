package com.crewly.roster

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.crewly.R
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Created by Derek on 07/07/2018
 */
class RosterMonthView @JvmOverloads constructor(context: Context,
                                                attributes: AttributeSet? = null,
                                                defStyle: Int = 0):
        FrameLayout(context, attributes, defStyle) {

    companion object {
        private const val NUMBER_OF_VIEWS_PER_ROW = 7
    }

    private val datesHorizontalMargin = context.resources.getDimensionPixelOffset(R.dimen.roster_dates_horizontal_margin)
    private val datesVerticalMargin = context.resources.getDimensionPixelOffset(R.dimen.roster_dates_vertical_margin)

    // Used to determine starting position offset based on first day of the month in the week
    private var startingPos = 0

    private var numberOfRows = 0

    // The width of the device screen
    var screenWidth = 0

    var rosterMonth: RosterPeriod.RosterMonth = RosterPeriod.RosterMonth()
        set(value) {
            startingPos = value.rosterDates[0].date.dayOfWeek().get() - 1

            if (field.rosterDates.isEmpty()) {
                addRosterDatesToView(value, 0, value.rosterDates.size)
            } else {
                val childCount = childCount
                val dateCount = value.rosterDates.size

                when {
                    childCount > dateCount -> {
                        removeViews(0, childCount - dateCount)
                        bindDataToChildViews(value)
                    }

                    dateCount > childCount -> {
                        bindDataToChildViews(value)
                        addRosterDatesToView(value, childCount, dateCount)
                    }

                    else -> bindDataToChildViews(value)
                }
            }

            field = value
            calculateNumberOfRows()
        }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (rosterMonth.rosterDates.isEmpty()) { return }

        val childWidth = screenWidth / NUMBER_OF_VIEWS_PER_ROW
        val remainingHorizontalSpace = screenWidth % NUMBER_OF_VIEWS_PER_ROW

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val row = floor((i + startingPos) / NUMBER_OF_VIEWS_PER_ROW.toFloat()).toInt()
            val startingPosX = ((i + startingPos) % NUMBER_OF_VIEWS_PER_ROW) * childWidth +
                    (datesHorizontalMargin / 2) + (remainingHorizontalSpace / 2)
            val startingPosY = (row * childWidth)
            child.layout(startingPosX, startingPosY, startingPosX + childWidth, startingPosY + childWidth)
        }
    }

    private fun calculateNumberOfRows() {
        val lastPos = startingPos + rosterMonth.rosterDates.size
        numberOfRows = ceil(lastPos / NUMBER_OF_VIEWS_PER_ROW.toFloat()).toInt()

        val layoutParams = this.layoutParams
        layoutParams.height = ((screenWidth / NUMBER_OF_VIEWS_PER_ROW) * numberOfRows) +
                (numberOfRows * datesVerticalMargin)
        this.layoutParams = layoutParams
    }

    /**
     * Add a list of [RosterPeriod.RosterDate]s to the view. A new [RosterDateView] will be created
     * for each date and added to the view hierarchy.
     * @param fromIndex The starting index in the list of roster dates
     * @param toIndex The end index in the list of roster dates
     */
    private fun addRosterDatesToView(rosterMonth: RosterPeriod.RosterMonth,
                                     fromIndex: Int,
                                     toIndex: Int) {
        /*
         * Calculate the horizontal/vertical dimension of each view. The margins between each view
         * and between the edges of the screen are divided equally between all views.
         */
        val viewDimension = (screenWidth / NUMBER_OF_VIEWS_PER_ROW) -
                ((datesHorizontalMargin * 8f) / NUMBER_OF_VIEWS_PER_ROW).toInt()

        for (i in fromIndex until toIndex) {
            val calendarDateView = RosterDateView(context)
            calendarDateView.layoutParams = MarginLayoutParams(viewDimension, viewDimension)
            calendarDateView.bindToRosterDate(rosterMonth.rosterDates[i])
            addView(calendarDateView)
        }
    }

    /**
     * Binds roster data to all child views
     */
    private fun bindDataToChildViews(rosterMonth: RosterPeriod.RosterMonth) {
        for (i in 0 until childCount) {
            (getChildAt(i) as RosterDateView).bindToRosterDate(rosterMonth.rosterDates[i])
        }
    }
}