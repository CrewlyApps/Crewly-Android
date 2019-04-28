package com.crewly.logbook

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.crewly.duty.DutyIcon
import com.crewly.utils.visible
import com.crewly.views.DateHeaderView
import kotlinx.android.synthetic.main.logbook_date_header.view.*

/**
 * Created by Derek on 22/04/2019
 */
class LogbookDateHeaderViewHolder(rootView: View): RecyclerView.ViewHolder(rootView) {

  fun bindData(data: LogbookDayData.DateHeaderData) {
    itemView.text_date_header.displayDate(
      date = data.date,
      formatStyle = DateHeaderView.FormatStyle.FULL
    )

    val dutyIconResource = data.dutyIcon.iconResourceId
    val hasIcon = dutyIconResource != DutyIcon.NO_ICON
    itemView.image_duty_icon.apply {
      visible(
        visible = hasIcon,
        withInvisibility = true
      )
      if (hasIcon) setImageResource(dutyIconResource)
    }
  }
}