package com.crewly.roster

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.crewly.R
import com.crewly.duty.DutyType
import com.crewly.utils.inflate
import kotlinx.android.synthetic.main.calendar_date_view.view.*

/**
 * Created by Derek on 07/07/2018
 */
class RosterDateView @JvmOverloads constructor(context: Context,
                                               attributes: AttributeSet? = null,
                                               defStyle: Int = 0):
        RelativeLayout(context, attributes, defStyle) {

    init {
        inflate(R.layout.calendar_date_view, attachToRoot = true)
    }

    fun bindToRosterDate(rosterDate: RosterPeriod.RosterDate) {
        text_date.text = rosterDate.date.dayOfMonth().asText

        when (rosterDate.dutyType.type) {
            DutyType.NONE -> {
                if (rosterDate.sectors.isNotEmpty()) { text_number.text = rosterDate.sectors.size.toString() }
            }

            DutyType.ASBY -> {
                image_calendar_date.setImageResource(R.drawable.icon_airplane)
                text_number.visibility = View.GONE
                layout_selected.visibility = View.GONE
            }

            DutyType.HSBY -> {
                image_calendar_date.setImageResource(R.drawable.icon_home)
                text_number.visibility = View.GONE
            }

            DutyType.SICK -> {
                image_calendar_date.setImageResource(R.drawable.icon_sick)
                text_number.visibility = View.GONE
                layout_selected.visibility = View.GONE
            }

            DutyType.OFF -> {
                image_calendar_date.setImageResource(R.drawable.icon_off)
                text_number.visibility = View.GONE
                layout_selected.visibility = View.GONE
            }
        }
    }
}