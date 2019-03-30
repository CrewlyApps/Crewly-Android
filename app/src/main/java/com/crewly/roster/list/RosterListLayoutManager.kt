package com.crewly.roster.list

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.crewly.activity.ScreenDimensions

/**
 * Created by Derek on 04/08/2018
 * Increase the layout space so that views further ahead can be pre-loaded. This should improve
 * scrolling performance a bit by improving view render distance.
 */
class RosterListLayoutManager(
  context: Context,
  private val screenDimensions: ScreenDimensions
):
  LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {

  override fun getExtraLayoutSpace(state: RecyclerView.State?): Int =
    screenDimensions.screenHeight
}