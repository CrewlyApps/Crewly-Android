package com.crewly.logbook

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crewly.R
import com.crewly.logbook.LogbookDayData.DateHeaderData
import com.crewly.logbook.LogbookDayData.FlightDetailsData
import com.crewly.utils.inflate

/**
 * Created by Derek on 22/04/2019
 */
class LogbookDayAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  companion object {
    private const val DATE_HEADER_TYPE = 1
    private const val FLIGHT_DETAILS_TYPE = 2
  }

  private val data = mutableListOf<LogbookDayData>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
    when (viewType) {
      DATE_HEADER_TYPE -> LogbookDateHeaderViewHolder(parent.inflate(R.layout.logbook_date_header))
      FLIGHT_DETAILS_TYPE -> LogbookFlightViewHolder(parent.inflate(R.layout.logbook_flight))
      else -> super.createViewHolder(parent, viewType)
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val dataItem = data[position]
    when  {
      holder is LogbookDateHeaderViewHolder && dataItem is DateHeaderData -> holder.bindData(dataItem)
      holder is LogbookFlightViewHolder && dataItem is FlightDetailsData -> holder.bindData(dataItem)
    }
  }

  override fun getItemCount(): Int = data.size

  override fun getItemViewType(position: Int): Int =
    when (data[position]) {
      is DateHeaderData -> DATE_HEADER_TYPE
      is FlightDetailsData -> FLIGHT_DETAILS_TYPE
    }

  fun setData(data: List<LogbookDayData>) {
    this.data.clear()
    this.data.addAll(data)
    notifyDataSetChanged()
  }
}