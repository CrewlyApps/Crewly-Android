package com.crewly.roster

import android.support.v7.widget.RecyclerView
import android.view.View
import com.crewly.activity.ScreenDimensions
import kotlinx.android.synthetic.main.roster_list_row.view.*

/**
 * Created by Derek on 03/06/2018
 */
class RosterMonthListRow(rootView: View,
                         private val screenDimensions: ScreenDimensions):
        RecyclerView.ViewHolder(rootView) {

    fun bindData(rosterMonth: RosterPeriod.RosterMonth) {
        itemView.text_month.text = rosterMonth.rosterDates[0].date.monthOfYear().asText
        itemView.text_hours.text = rosterMonth.rosterDates[0].date.hourOfDay().asText
        itemView.roster_month.screenWidth = screenDimensions.screenWidth
        itemView.roster_month.rosterMonth = rosterMonth
    }
}