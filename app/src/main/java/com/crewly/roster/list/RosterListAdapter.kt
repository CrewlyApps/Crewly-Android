package com.crewly.roster.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crewly.R
import com.crewly.activity.AppNavigator
import com.crewly.activity.ScreenDimensions
import com.crewly.models.roster.RosterPeriod
import com.crewly.utils.inflate
import javax.inject.Inject

/**
 * Created by Derek on 04/08/2018
 */
class RosterListAdapter @Inject constructor(
  private val appNavigator: AppNavigator,
  private val screenDimensions: ScreenDimensions
):
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val roster = mutableListOf<RosterPeriod.RosterMonth>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
    RosterListRow(parent.inflate(R.layout.roster_list_row), screenDimensions, this::handleDateClick)

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

  private fun handleDateClick(rosterDate: RosterPeriod.RosterDate) {
    appNavigator
      .start()
      .toRosterDetailsScreen(rosterDate.date.millis)
      .navigate()
  }
}