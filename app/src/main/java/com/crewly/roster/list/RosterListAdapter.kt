package com.crewly.roster.list

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.crewly.R
import com.crewly.activity.ScreenDimensions
import com.crewly.roster.RosterPeriod
import com.crewly.utils.inflate

/**
 * Created by Derek on 03/06/2018
 */
class RosterListAdapter(private val screenDimensions: ScreenDimensions,
                        private val dateClickAction: ((rosterDate: RosterPeriod.RosterDate) -> Unit)? = null,
                        var roster: List<RosterPeriod.RosterMonth> = listOf()):
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
            RosterListRow(parent.inflate(R.layout.roster_list_row), screenDimensions, dateClickAction)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as RosterListRow).bindData(getItem(position)!!)
    }
}