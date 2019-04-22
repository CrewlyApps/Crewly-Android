package com.crewly.logbook

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crewly.R
import com.crewly.logbook.LogbookDayData.DateHeaderData
import com.crewly.logbook.LogbookDayData.SectorDetailsData
import com.crewly.utils.inflate

/**
 * Created by Derek on 22/04/2019
 */
class LogbookDayAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  companion object {
    private const val DATE_HEADER_TYPE = 1
    private const val SECTOR_DETAILS_TYPE = 2
  }

  private val data = mutableListOf<LogbookDayData>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
    when (viewType) {
      DATE_HEADER_TYPE -> LogbookDateHeaderViewHolder(parent.inflate(R.layout.logbook_date_header))
      SECTOR_DETAILS_TYPE -> LogbookSectorViewHolder(parent.inflate(R.layout.logbook_sector))
      else -> super.createViewHolder(parent, viewType)
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val dataItem = data[position]
    when  {
      holder is LogbookDateHeaderViewHolder && dataItem is DateHeaderData -> holder.bindData(dataItem)
      holder is LogbookSectorViewHolder && dataItem is SectorDetailsData -> holder.bindData(dataItem.sector)
    }
  }

  override fun getItemCount(): Int = data.size

  override fun getItemViewType(position: Int): Int =
    when (data[position]) {
      is DateHeaderData -> DATE_HEADER_TYPE
      is SectorDetailsData -> SECTOR_DETAILS_TYPE
    }

  fun setData(data: List<LogbookDayData>) {
    this.data.clear()
    this.data.addAll(data)
    notifyDataSetChanged()
  }
}