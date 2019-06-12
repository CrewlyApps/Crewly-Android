package com.crewly.roster.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crewly.R
import com.crewly.activity.ScreenDimensions
import com.crewly.duty.DutyDisplayHelper
import com.crewly.models.roster.RosterPeriod
import com.crewly.utils.inflate

/**
 * Created by Derek on 04/08/2018
 */
class RosterListAdapter(
  private val screenDimensions: ScreenDimensions,
  private val dutyDisplayHelper: DutyDisplayHelper,
  private val dateClickAction: ((rosterDate: RosterPeriod.RosterDate) -> Unit)? = null
):
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val roster = mutableListOf<RosterPeriod.RosterMonth>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
    RosterListRow(
      rootView = parent.inflate(R.layout.roster_list_row),
      screenDimensions = screenDimensions,
      dutyDisplayHelper = dutyDisplayHelper,
      dateClickAction = dateClickAction
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder as RosterListRow).bindData(roster[position])
  }

  override fun getItemCount(): Int = roster.size

  fun setRoster(roster: List<RosterPeriod.RosterMonth>) {
    this.roster.apply {
      clear()
      addAll(roster)
      notifyDataSetChanged()
    }
  }
}