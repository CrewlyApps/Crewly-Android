package com.crewly.roster

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.crewly.R
import com.crewly.utils.inflate

/**
 * Created by Derek on 03/06/2018
 */
class RosterMonthAdapter(var roster: List<RosterPeriod.RosterMonth> = listOf()):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RosterMonthListRow(parent.inflate(R.layout.roster_list_row))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as RosterMonthListRow).bindData(roster[position])
    }

    override fun getItemCount(): Int = roster.size
}