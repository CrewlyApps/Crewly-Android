package com.crewly.logbook

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.crewly.duty.Sector
import kotlinx.android.synthetic.main.logbook_sector.view.*

/**
 * Created by Derek on 22/04/2019
 */
class LogbookSectorViewHolder(rootView: View): RecyclerView.ViewHolder(rootView) {

  fun bindData(sector: Sector) {
    itemView.sector_details_view.sector = sector
  }
}