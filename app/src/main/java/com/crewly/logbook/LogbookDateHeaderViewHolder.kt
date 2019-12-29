package com.crewly.logbook

import android.view.View
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.crewly.models.duty.DutyIcon
import com.crewly.views.DateHeaderView
import kotlinx.android.synthetic.main.logbook_date_header.view.*

/**
 * Created by Derek on 22/04/2019
 */
class LogbookDateHeaderViewHolder(
  rootView: View
): RecyclerView.ViewHolder(rootView) {

  fun bindData(
    data: LogbookDayData.DateHeaderData
  ) {
    itemView.text_date_header.displayDate(
      date = data.date,
      formatStyle = DateHeaderView.FormatStyle.FULL
    )

    val dutyIconResource = data.dutyIcon
    val hasIcon = dutyIconResource != DutyIcon.NO_ICON
    itemView.image_duty_icon.apply {
      isInvisible = hasIcon
      if (hasIcon) setImageResource(dutyIconResource)
    }
  }
}