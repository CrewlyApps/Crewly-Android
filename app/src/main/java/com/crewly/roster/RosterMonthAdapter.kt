package com.crewly.roster

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.crewly.R
import com.crewly.activity.ScreenDimensions
import com.crewly.utils.inflate

/**
 * Created by Derek on 03/06/2018
 */
class RosterMonthAdapter(var roster: List<RosterPeriod.RosterMonth> = listOf(),
                         var screenDimensions: ScreenDimensions):
        PagedListAdapter<RosterPeriod.RosterMonth, RecyclerView.ViewHolder>(DIFF_UTIL) {

    companion object {
        private val DIFF_UTIL = object: DiffUtil.ItemCallback<RosterPeriod.RosterMonth>() {

            override fun areItemsTheSame(oldItem: RosterPeriod.RosterMonth?,
                                         newItem: RosterPeriod.RosterMonth?): Boolean =
                    oldItem?.rosterDates?.get(0)?.date?.monthOfYear() ==
                        newItem?.rosterDates?.get(0)?.date?.monthOfYear()

            override fun areContentsTheSame(oldItem: RosterPeriod.RosterMonth?,
                                            newItem: RosterPeriod.RosterMonth?): Boolean = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            RosterMonthListRow(parent.inflate(R.layout.roster_list_row), screenDimensions)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as RosterMonthListRow).bindData(getItem(position)!!)
    }
}