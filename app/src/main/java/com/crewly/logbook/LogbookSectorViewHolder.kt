package com.crewly.logbook

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.logbook_sector.view.*

/**
 * Created by Derek on 22/04/2019
 */
class LogbookSectorViewHolder(rootView: View): RecyclerView.ViewHolder(rootView) {

  fun bindData(
    sectorData: LogbookDayData.SectorDetailsData
  ) {
    itemView.sector_details_view.sectorData = sectorData.data
    itemView.sector_details_view.includeBottomMargin(sectorData.includeBottomMargin)
  }
}