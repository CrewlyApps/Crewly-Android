package com.crewly.logbook

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.crewly.duty.DutyIcon
import com.crewly.views.DateHeaderView
import kotlinx.android.synthetic.main.logbook_date_header.view.*
import org.joda.time.DateTime

/**
 * Created by Derek on 22/04/2019
 */
class LogbookDateHeaderViewHolder(rootView: View): RecyclerView.ViewHolder(rootView) {

  data class DateHeaderData(
    val date: DateTime,
    val dutyIcon: DutyIcon
  )

  fun bindData(data: DateHeaderData) {
    itemView.text_date_header.displayDate(
      date = data.date,
      formatStyle = DateHeaderView.FormatStyle.FULL
    )

    itemView.image_duty_icon.setImageResource(data.dutyIcon.iconResourceId)
  }
}