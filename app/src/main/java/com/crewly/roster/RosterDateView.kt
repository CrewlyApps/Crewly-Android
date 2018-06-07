package com.crewly.roster

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.crewly.R
import com.crewly.utils.inflate
import kotlinx.android.synthetic.main.calendar_date_view.view.*

/**
 * Created by Derek on 27/05/2018
 */
class RosterDateView @JvmOverloads constructor(context: Context,
                                               attributes: AttributeSet? = null,
                                               defStyle: Int = 0):
        RelativeLayout(context, attributes, defStyle) {

    // The value of width and height dimensions
    private var dimension = 0

    // Amount to subtract from the total width of this view
    var subtractFromWidth = 0

    init {
        inflate(R.layout.calendar_date_view, attachToRoot = true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        dimension = parentWidth / 7
        setMeasuredDimension(dimension - subtractFromWidth, dimension)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        setChildDimensions()
    }

    fun bindToRosterDate(rosterDate: RosterPeriod.RosterDate) {
        text_date.text = rosterDate.date.dayOfMonth().asText

        when (rosterDate.dutyType) {
            is DutyType.Sector -> {
                text_number.text = rosterDate.sectors.size.toString()
            }

            is DutyType.ASBY -> {
                image_calendar_date.setImageResource(R.drawable.icon_airplane)
                text_number.visibility = View.GONE
                layout_selected.visibility = View.GONE
            }

            is DutyType.HSBY -> {
                image_calendar_date.setImageResource(R.drawable.icon_home)
                text_number.visibility = View.GONE
            }

            is DutyType.Sick -> {
                image_calendar_date.setImageResource(R.drawable.icon_sick)
                text_number.visibility = View.GONE
                layout_selected.visibility = View.GONE
            }

            is DutyType.Off -> {
                image_calendar_date.setImageResource(R.drawable.icon_off)
                text_number.visibility = View.GONE
                layout_selected.visibility = View.GONE
            }
        }

        setChildDimensions()
    }

    private fun setChildDimensions() {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val layoutParams = child.layoutParams

            when (i) {
                0 -> {
                    layoutParams.width = dimension - subtractFromWidth
                    layoutParams.height = (dimension * 0.3).toInt()
                }

                1, 2 -> {
                    layoutParams.width = dimension - subtractFromWidth
                    layoutParams.height = (dimension * 0.7).toInt()
                }
            }

            child.layoutParams = layoutParams

            if (child.id == R.id.layout_selected) {
                view_selected_left_margin.layoutParams.width = ((dimension - subtractFromWidth) * 0.2f).toInt()
                view_selected.layoutParams.width = ((dimension - subtractFromWidth) * 0.6f).toInt()
                view_selected_right_margin.layoutParams.width = ((dimension - subtractFromWidth) * 0.2f).toInt()
            }
        }
    }
}