package com.crewly.roster.list

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.crewly.R
import com.crewly.activity.ActivityScope
import com.crewly.activity.AppNavigator
import com.crewly.activity.ScreenDimensions
import com.crewly.app.RxModule
import com.crewly.logging.LoggingManager
import com.crewly.roster.RosterPeriod
import com.crewly.utils.inflate
import com.crewly.utils.plus
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 04/08/2018
 */
@ActivityScope
class RosterListAdapter @Inject constructor(
  activity: AppCompatActivity,
  viewModelFactory: ViewModelProvider.AndroidViewModelFactory,
  private val loggingManager: LoggingManager,
  private val appNavigator: AppNavigator,
  private val screenDimensions: ScreenDimensions,
  @Named(RxModule.MAIN_THREAD) private val mainThread: Scheduler
):
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val viewModel = ViewModelProviders.of(activity, viewModelFactory)[RosterListViewModel::class.java]
  private val disposables = CompositeDisposable()
  private val roster = mutableListOf<RosterPeriod.RosterMonth>()

  init {
    disposables + viewModel
      .observeRosterMonths()
      .observeOn(mainThread)
      .subscribe({ rosterMonths ->
        roster.clear()
        roster.addAll(rosterMonths)
        notifyDataSetChanged()
      }, { error -> loggingManager.logError(error) })
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
    RosterListRow(parent.inflate(R.layout.roster_list_row), screenDimensions, this::handleDateClick)

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder as RosterListRow).bindData(roster[position])
  }

  override fun getItemCount(): Int = roster.size

  fun onDestroy() {
    disposables.dispose()
  }

  private fun handleDateClick(rosterDate: RosterPeriod.RosterDate) {
    appNavigator
      .start()
      .toRosterDetailsScreen(rosterDate.date.millis)
      .navigate()
  }
}