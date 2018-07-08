package com.crewly.roster

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.crewly.R
import com.crewly.duty.DutyType
import com.crewly.utils.inflate
import com.crewly.utils.visible
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

    fun bindToRosterDate(rosterDate: RosterPeriod.RosterDate,
                         isCurrentDay: Boolean) {
        text_date.text = rosterDate.date.dayOfMonth().asText
        view_selected.visible(isCurrentDay)

        when (rosterDate.dutyType.type) {
            DutyType.NONE -> {
                text_number.text = rosterDate.sectors.size.toString()
                showImage(false)
            }

            DutyType.ASBY -> {
                image_calendar_date.setImageResource(R.drawable.icon_asby)
                showImage(true)
            }

            DutyType.HSBY -> {
                image_calendar_date.setImageResource(R.drawable.icon_home)
                showImage(true)
            }

            DutyType.OFF -> {
                image_calendar_date.setImageResource(R.drawable.icon_off)
                showImage(true)
            }

            DutyType.ANNUAL_LEAVE -> {
                image_calendar_date.setImageResource(R.drawable.icon_annual_leave)
                showImage(true)
            }

            DutyType.SICK -> {
                image_calendar_date.setImageResource(R.drawable.icon_sick)
                showImage(true)
            }

            DutyType.PARENTAL_LEAVE -> {
                image_calendar_date.setImageResource(R.drawable.icon_parental_leave)
                showImage(true)
            }
        }
    }

    private fun showImage(show: Boolean) {
        image_calendar_date.visible(show)
        text_number.visible(!show)
    }
}