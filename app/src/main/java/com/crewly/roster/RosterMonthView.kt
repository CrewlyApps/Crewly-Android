package com.crewly.roster

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.crewly.R
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Created by Derek on 27/05/2018
 */
class RosterMonthView @JvmOverloads constructor(context: Context,
                                                attributes: AttributeSet? = null,
                                                defStyle: Int = 0):
        ViewGroup(context, attributes, defStyle) {

    private val datesHorizontalMargin = context.resources.getDimensionPixelOffset(R.dimen.roster_dates_horizontal_margin)
    private val datesVerticalMargin = context.resources.getDimensionPixelOffset(R.dimen.roster_dates_vertical_margin)

    // Used to determine starting position offset based on first day of the month in the week
    private var startingPos = 0

    private var numberOfRows = 0

    var rosterMonth: RosterPeriod.RosterMonth = RosterPeriod.RosterMonth()
    set(value) {
        startingPos = value.rosterDates[0].date.dayOfWeek().get() - 1

        value.rosterDates.forEachIndexed { index, rosterDate ->
            val calendarDateView = RosterDateView(context)

            if (index == 0 || (index + startingPos + 1) % 7 != 0) {
                calendarDateView.subtractFromWidth = datesHorizontalMargin
            }

            calendarDateView.bindToRosterDate(rosterDate)
            addView(calendarDateView)
            field = value

            calculateNumberOfRows()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
        }

        val measureWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measureHeight = (getChildAt(0).measuredHeight * numberOfRows) + (numberOfRows * datesVerticalMargin) //MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(measureWidth, measureHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (rosterMonth.rosterDates.isEmpty()) { return }

        val width = right - left
        val childWidth = width / 7
        val remainingHorizontalSpace = width % 7

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val row = floor((i + startingPos) / 7f).toInt()
            val startingPosX = ((i + startingPos) % 7) * childWidth + (remainingHorizontalSpace / 2)
            val startingPosY = (row * childWidth) + (row * datesVerticalMargin)
            child.layout(startingPosX, startingPosY, startingPosX + childWidth, startingPosY + childWidth)
        }
    }

    private fun calculateNumberOfRows() {
        val lastPos = startingPos + rosterMonth.rosterDates.size
        numberOfRows = ceil(lastPos / 7f).toInt()
    }
}