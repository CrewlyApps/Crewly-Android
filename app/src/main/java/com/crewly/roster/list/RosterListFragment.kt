package com.crewly.roster.list

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.crewly.R
import com.crewly.activity.AppNavigator
import com.crewly.activity.ScreenDimensions
import com.crewly.duty.DutyDisplayHelper
import com.crewly.logging.AnalyticsManger
import com.crewly.logging.LoggingFlow
import com.crewly.views.ScreenState
import com.crewly.models.roster.RosterPeriod
import com.crewly.roster.raw.RawRosterActivity
import com.crewly.utils.plus
import com.crewly.utils.throttleClicks
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.login_activity.view.*
import kotlinx.android.synthetic.main.login_activity.view.input_crew_code
import kotlinx.android.synthetic.main.login_activity.view.input_password
import kotlinx.android.synthetic.main.roster_list_fragment.*
import kotlinx.android.synthetic.main.roster_list_refresh_roster_dialog.view.*
import kotlinx.android.synthetic.main.roster_toolbar.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Derek on 27/04/2019
 */
class RosterListFragment: DaggerFragment() {

  @Inject lateinit var appNavigator: AppNavigator
  @Inject lateinit var analyticsManger: AnalyticsManger
  @Inject lateinit var viewModelFactory: ViewModelProvider.AndroidViewModelFactory
  @Inject lateinit var screenDimensions: ScreenDimensions
  @Inject lateinit var dutyDisplayHelper: DutyDisplayHelper

  private lateinit var viewModel: RosterListViewModel
  private lateinit var adapter: RosterListAdapter

  private val disposables = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
    inflater.inflate(R.layout.roster_list_fragment, container, false)

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setUpToolbar()
    setUpRosterList()

    viewModel = ViewModelProviders.of(this, viewModelFactory)[RosterListViewModel::class.java]
    observeScreenState()
    observeRoster()
    observeRefreshRosterInputEvents()
  }

  override fun onResume() {
    super.onResume()
    analyticsManger.recordScreenView("Roster List")
  }

  override fun onDestroy() {
    list_roster?.adapter = null
    disposables.dispose()
    super.onDestroy()
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.menu_roster, menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val handled = when (item.itemId) {
      R.id.button_refresh_roster -> {
        viewModel.handleRefreshRoster()
        true
      }

      R.id.button_raw_roster -> {
        analyticsManger.recordClick("Refresh Roster")
        Intent(context, RawRosterActivity::class.java).run {
          startActivity(this)
        }

        true
      }

      else -> false
    }

    return if (handled) true else super.onOptionsItemSelected(item)
  }

  private fun setUpToolbar() {
    (requireActivity() as AppCompatActivity).apply {
      setSupportActionBar(toolbar_roster)
      title = getString(R.string.roster_list_title)
    }
  }

  private fun setUpRosterList() {
    adapter = RosterListAdapter(
      screenDimensions = screenDimensions,
      dutyDisplayHelper = dutyDisplayHelper,
      dateClickAction = this@RosterListFragment::handleDateClick
    )

    list_roster.adapter = adapter
    list_roster.layoutManager = RosterListLayoutManager(requireContext(), screenDimensions)
  }

  private fun observeRoster() {
    disposables + viewModel
      .observeRosterMonths()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { rosterMonths ->
        adapter.setRoster(rosterMonths)

        if (rosterMonths.isEmpty()) {
          addEmptyView()
          showDayTabs(false)
          showToolbar(false)
          Timber.tag(LoggingFlow.ROSTER_LIST.loggingTag)
          Timber.d("Show empty view")
        } else {
          removeEmptyView()
          showDayTabs(true)
          showToolbar(true)
          Timber.tag(LoggingFlow.ROSTER_LIST.loggingTag)
          Timber.d("Show roster")
        }
      }
  }

  private fun observeScreenState() {
    disposables + viewModel.observeScreenState()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { screenState ->
        when (screenState) {
          is ScreenState.Loading -> {
            loading_view.isVisible = true
          }
          is ScreenState.Success -> {
            loading_view.isVisible = false
          }
          is ScreenState.NetworkError -> {
            loading_view.isVisible = false
          }
          is ScreenState.Error -> {
            loading_view.isVisible = false
          }
        }
      }
  }

  private fun observeRefreshRosterInputEvents() {
    disposables + viewModel.observeRefreshRosterInputEvents()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { data ->
        AlertDialog.Builder(requireContext()).run {
          val view = layoutInflater.inflate(R.layout.roster_list_refresh_roster_dialog, null)
          setView(view)

          view.input_crew_code.setText(data.crewCode)
          view.input_password.setText(data.password)

          show() to view
        }
          .run {
            val (dialog, view) = this

            disposables + view.button_refresh_roster
              .throttleClicks()
              .subscribe {
                viewModel.refreshRoster(view.input_password.text.toString())
                dialog.dismiss()
              }
          }
      }
  }

  private fun showDayTabs(show: Boolean) {
    group_day_tabs.isVisible = show
  }

  private fun showToolbar(
    show: Boolean
  ) {
    if (show) {
      (activity as? AppCompatActivity)?.supportActionBar?.show()
    } else {
      (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
  }

  private fun addEmptyView() {
    if (viewModel.showingEmptyView) return

    val emptyView = RosterListEmptyView(requireContext()).apply {
      appNavigator = this@RosterListFragment.appNavigator
      id = R.id.roster_list_empty_view
    }

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

  private fun handleDateClick(rosterDate: RosterPeriod.RosterDate) {
    appNavigator
      .start()
      .toRosterDetailsScreen(rosterDate.date.millis)
      .navigate()
  }
}