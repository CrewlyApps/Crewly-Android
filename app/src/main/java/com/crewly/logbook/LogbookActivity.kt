package com.crewly.logbook

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.crewly.R
import com.crewly.activity.AppNavigator
import com.crewly.app.NavigationScreen
import com.crewly.app.RxModule
import com.crewly.duty.DutyDisplayHelper
import com.crewly.roster.RosterPeriod
import com.crewly.utils.plus
import com.google.android.material.navigation.NavigationView
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.account_toolbar.*
import kotlinx.android.synthetic.main.logbook_activity.*
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 26/08/2018
 * Displays a log of the user's roster history. They can select a range of dates and information
 * and stats for those roster dates will be shown.
 */
class LogbookActivity: DaggerAppCompatActivity(), NavigationScreen {

  @Inject override lateinit var appNavigator: AppNavigator
  @Inject lateinit var viewModelFactory: ViewModelProvider.AndroidViewModelFactory
  @Inject lateinit var dutyDisplayHelper: DutyDisplayHelper
  @field: [Inject Named(RxModule.MAIN_THREAD)] lateinit var mainThread: Scheduler

  override lateinit var drawerLayout: DrawerLayout
  override lateinit var navigationView: NavigationView
  override lateinit var actionBar: ActionBar

  private lateinit var viewModel: LogbookViewModel

  private val disposables = CompositeDisposable()

  private val timeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.logbook_activity)

    setSupportActionBar(toolbar_account)
    drawerLayout = drawer_layout
    navigationView = navigation_view
    actionBar = supportActionBar!!
    setUpNavigationDrawer(R.id.menu_logbook)

    viewModel = ViewModelProviders.of(this, viewModelFactory)[LogbookViewModel::class.java]

    observeAccount()
    observeDateTimePeriod()
    observeRosterDates()
  }

  override fun onResume() {
    super.onResume()
    setSelectedNavigationDrawerItem(R.id.menu_logbook)
  }

  override fun onDestroy() {
    disposables.dispose()
    super.onDestroy()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        drawerLayout.openDrawer(GravityCompat.START)
        true
      }

      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun observeAccount() {
    disposables + viewModel
      .observeAccount()
      .observeOn(mainThread)
      .subscribe { account ->
        if (account.crewCode.isNotBlank()) {
          supportActionBar?.title = getString(R.string.logbook_title, account.crewCode)
        }
      }
  }

  private fun observeDateTimePeriod() {
    disposables + viewModel
      .observeDateTimePeriod()
      .observeOn(mainThread)
      .subscribe { dateTimePeriod ->
        button_from_date.text = timeFormatter.print(dateTimePeriod.startDateTime)
        button_to_date.text = timeFormatter.print(dateTimePeriod.endDateTime)
      }
  }

  private fun observeRosterDates() {
    disposables + viewModel
      .observeRosterDates()
      .observeOn(mainThread)
      .subscribe { rosterDates ->
        setUpSectors(rosterDates)
      }
  }

  private fun setUpSectors(rosterDates: List<RosterPeriod.RosterDate>) {
    dutyDisplayHelper.getDutyDisplayInfo(rosterDates)
      .apply {
        displayNumberOfSectors(totalNumberOfSectors)
        displayDutyTime(totalDutyTime)
        displayFlightTime(totalFlightDuration)
        displayFlightDutyPeriod(totalFlightDutyPeriod)
      }
  }

  private fun displayNumberOfSectors(numberOfSectors: Int) {
    text_number_of_sectors.text = numberOfSectors.toString()
  }

  private fun displayDutyTime(dutyTime: String) {
    text_duty_time.text = dutyTime
  }

  private fun displayFlightTime(flightTime: String) {
    text_flight_time.text = flightTime
  }

  private fun displayFlightDutyPeriod(flightDutyPeriod: String) {
    text_flight_duty_time.text = flightDutyPeriod
  }
}