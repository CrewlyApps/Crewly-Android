package com.crewly.roster.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.crewly.R
import com.crewly.ScreenState
import com.crewly.activity.AppNavigator
import com.crewly.activity.ScreenDimensions
import com.crewly.app.RxModule
import com.crewly.utils.plus
import com.crewly.utils.visible
import dagger.android.support.DaggerFragment
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.roster_list_fragment.*
import kotlinx.android.synthetic.main.roster_toolbar.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 27/04/2019
 */
class RosterListFragment: DaggerFragment() {

  @Inject lateinit var appNavigator: AppNavigator
  @Inject lateinit var viewModelFactory: ViewModelProvider.AndroidViewModelFactory
  @field: [Inject Named(RxModule.MAIN_THREAD)] lateinit var mainThread: Scheduler
  @Inject lateinit var rosterListAdapter: RosterListAdapter
  @Inject lateinit var screenDimensions: ScreenDimensions

  private lateinit var viewModel: RosterListViewModel

  private val disposables = CompositeDisposable()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
    inflater.inflate(R.layout.roster_list_fragment, container, false)

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setUpToolbar()
    setUpRosterList()

    viewModel = ViewModelProviders.of(this, viewModelFactory)[RosterListViewModel::class.java]
    observeScreenState()
    observeRoster()
  }

  override fun onDestroy() {
    disposables.dispose()
    super.onDestroy()
  }

  private fun setUpToolbar() {
    (requireActivity() as AppCompatActivity).apply {
      setSupportActionBar(toolbar_roster)
      supportActionBar?.title = getString(R.string.roster_list_title)
    }
  }

  private fun setUpRosterList() {
    list_roster.adapter = rosterListAdapter
    list_roster.layoutManager = RosterListLayoutManager(requireContext(), screenDimensions)
  }

  private fun observeRoster() {
    disposables + viewModel
      .observeRosterMonths()
      .observeOn(mainThread)
      .subscribe { rosterMonths ->
        rosterListAdapter.setRoster(rosterMonths)

        if (rosterMonths.isEmpty()) {
          addEmptyView()
          showDayTabs(false)
        } else {
          removeEmptyView()
          showDayTabs(true)
        }
      }
  }

  private fun observeScreenState() {
    disposables + viewModel.observeScreenState()
      .observeOn(mainThread)
      .subscribe { screenState ->
        when (screenState) {
          is ScreenState.Loading -> {
            loading_view.visible(true)
          }
          is ScreenState.Success -> {
            loading_view.visible(false)
          }
          is ScreenState.NetworkError -> {
            loading_view.visible(false)
          }
          is ScreenState.Error -> {
            loading_view.visible(false)
          }
        }
      }
  }

  private fun showDayTabs(show: Boolean) {
    group_day_tabs.visible(show)
  }

  private fun addEmptyView() {
    if (viewModel.showingEmptyView) return

    val emptyView = RosterListEmptyView(requireContext(), appNavigator = appNavigator)
    emptyView.id = R.id.roster_list_empty_view
    container_screen.addView(emptyView)
    viewModel.showingEmptyView = true

    val constraintSet = ConstraintSet()
    constraintSet.clone(container_screen)
    constraintSet.constrainHeight(emptyView.id, 0)
    constraintSet.constrainWidth(emptyView.id, 0)
    constraintSet.connect(emptyView.id, ConstraintSet.TOP, R.id.toolbar_roster, ConstraintSet.BOTTOM)
    constraintSet.connect(emptyView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
    constraintSet.connect(emptyView.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)
    constraintSet.connect(emptyView.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
    constraintSet.applyTo(container_screen)
  }

  private fun removeEmptyView() {
    val emptyView = view?.findViewById<View>(R.id.roster_list_empty_view)
    emptyView?.let { container_screen.removeView(it) }
    viewModel.showingEmptyView = false
  }
}