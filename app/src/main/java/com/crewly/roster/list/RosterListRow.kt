package com.crewly.roster.list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.crewly.activity.ScreenDimensions
import com.crewly.duty.DutyDisplayHelper
import com.crewly.models.roster.RosterPeriod
import com.crewly.views.DateHeaderView
import kotlinx.android.synthetic.main.roster_list_row.view.*

/**
 * Created by Derek on 03/06/2018
 */
class RosterListRow(
  rootView: View,
  private val screenDimensions: ScreenDimensions,
  private val dutyDisplayHelper: DutyDisplayHelper,
  private val dateClickAction: ((rosterDate: RosterPeriod.RosterDate) -> Unit)? = null
):
  RecyclerView.ViewHolder(rootView) {

  fun bindData(
    rosterMonth: RosterPeriod.RosterMonth
  ) {
    itemView.text_month.displayDate(
      date = rosterMonth.rosterDates[0].date,
      formatStyle = DateHeaderView.FormatStyle.SHORT
    )

    itemView.roster_month.screenWidth = screenDimensions.screenWidth
    itemView.roster_month.dateClickAction = dateClickAction
    itemView.roster_month.rosterMonth = rosterMonth

    itemView.text_hours.text = dutyDisplayHelper.getDutyDisplayInfo(
      rosterDates = rosterMonth.rosterDates
    ).totalFlightDuration
  }
}