package com.crewly.roster.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.crewly.R
import com.crewly.app.RxModule
import com.crewly.crew.CrewView
import com.crewly.db.crew.Crew
import com.crewly.db.duty.Duty
import com.crewly.db.sector.Sector
import com.crewly.duty.DutyDisplayHelper
import com.crewly.duty.sector.SectorDetailsView
import com.crewly.models.Flight
import com.crewly.models.duty.FullDuty
import com.crewly.utils.plus
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.roster_details_activity.*
import kotlinx.android.synthetic.main.roster_details_toolbar.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatterBuilder
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 15/07/2018
 */
class RosterDetailsActivity: DaggerAppCompatActivity() {

  companion object {
    private const val DATE_MILLIS_KEY = "DateMillis"

    fun getInstance(context: Context, dateMillis: Long): Intent {
      val intent = Intent(context, RosterDetailsActivity::class.java)
      intent.putExtra(DATE_MILLIS_KEY, dateMillis)
      return intent
    }
  }

  @Inject lateinit var viewModelFactory: ViewModelProvider.AndroidViewModelFactory
  @Inject lateinit var dutyDisplayHelper: DutyDisplayHelper
  @field: [Inject Named(RxModule.MAIN_THREAD)] lateinit var mainThread: Scheduler

  private val dateTimeFormatter = DateTimeFormatterBuilder()
    .appendHourOfDay(2)
    .appendLiteral("h ")
    .appendMinuteOfHour(2)
    .appendLiteral("m")
    .toFormatter()

  private val dateFormatter = DateTimeFormat.forPattern("dd/MM/YY")

  private lateinit var viewModel: RosterDetailsViewModel

  private val disposables = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.roster_details_activity)

    setSupportActionBar(toolbar_roster_details)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = getString(R.string.roster_details_title)

    viewModel = ViewModelProviders.of(this, viewModelFactory)[RosterDetailsViewModel::class.java]

    observeRosterDate()
    observeFlight()
    observeCrew()
    displayDate()
    displayCurrentTimezone()
    viewModel.fetchRosterDate(DateTime(intent.getLongExtra(DATE_MILLIS_KEY, 0)))
  }

  override fun onDestroy() {
    disposables.dispose()
    super.onDestroy()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        onBackPressed()
        true
      }

      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun observeRosterDate() {
    disposables + viewModel
      .observeRosterDate()
      .observeOn(mainThread)
      .subscribe { rosterDate ->
        val sectors = rosterDate.sectors

        if (sectors.isNotEmpty()) {
          dutyDisplayHelper.getDutyDisplayInfo(listOf(rosterDate))
            .apply {
              displayFlightDuration(totalFlightDuration)
              displayDutyTime(totalDutyTime)
              displayFlightDutyPeriod(totalFlightDutyPeriod)
              displaySalary(totalSalary)
            }

          displaySectors(sectors)
          showFlightInfo(true)
          showStandbyInfo(false)
          showSectorsSection(true)

        } else {
          val standbyDuty = rosterDate.fullDuties.find { fullDuty ->
            fullDuty.dutyType.isAirportStandby() || fullDuty.dutyType.isHomeStandby()
          }

          standbyDuty?.let {
            displayStartTime(it.duty)
            displayEndTime(it.duty)
          }

          showFlightInfo(false)
          showStandbyInfo(standbyDuty != null)
          showSectorsSection(false)
        }

        displayEvents(
          fullDuties = rosterDate.fullDuties
        )
      }
  }

  private fun observeFlight() {
    disposables + viewModel
      .observeFlight()
      .observeOn(mainThread)
      .subscribe { flight ->
        displayReportLocalTime(flight)
        displayLandingLocalTime(flight)
      }
  }

  private fun observeCrew() {
    disposables + viewModel
      .observeCrew()
      .observeOn(mainThread)
      .subscribe { crew ->
        displayCrew(crew)
      }
  }

  private fun displayDate() {
    val date = DateTime(intent.getLongExtra(DATE_MILLIS_KEY, 0))
    text_current_date.text = dateFormatter.print(date)
  }

  private fun displayCurrentTimezone() {
    text_current_timezone.text = TimeZone.getDefault().id
  }

  private fun displayReportLocalTime(flight: Flight) {
    val airportTime = DateTime(flight.departureSector.departureTime,
      DateTimeZone.forID(flight.departureAirport.timezone))
    text_report_local_time.text = dateTimeFormatter.print(airportTime.minusMinutes(45))
  }

  private fun displayFlightDuration(flightDuration: String) {
    text_flight_time.text = flightDuration
  }

  private fun displayDutyTime(dutyTime: String) {
    text_duty_time.text = dutyTime
  }

  private fun displayFlightDutyPeriod(flightDutyPeriod: String) {
    text_flight_duty_period.text = flightDutyPeriod
  }

  private fun displayLandingLocalTime(flight: Flight) {
    val airportTime = DateTime(flight.arrivalSector.arrivalTime,
      DateTimeZone.forID(flight.arrivalAirport.timezone))
    text_landing_local_time.text = dateTimeFormatter.print(airportTime)
  }

  private fun displayStartTime(duty: Duty) {
    text_start_time.text = dateTimeFormatter.print(duty.startTime)
  }

  private fun displayEndTime(duty: Duty) {
    text_end_time.text = dateTimeFormatter.print(duty.endTime)
  }

  private fun displaySalary(salary: String) {
    text_salary.text = salary
  }

  private fun displayEvents(fullDuties: List<FullDuty>) {
    fullDuties.forEachIndexed { index, fullDuty ->
      if (fullDuty.duty.description.isNotBlank()) {
        val eventView = RosterDetailsEventView(this)
        eventView.displayEvent(
          duty = fullDuty.duty,
          dutyType = fullDuty.dutyType
        )
        if (index < fullDuties.size) {
          eventView.addBottomMargin()
        }
        list_events.addView(eventView)
      }
    }

    showEvents(list_events.childCount > 0)
  }

  private fun showEvents(show: Boolean) {
    text_events_title.isVisible = show
    list_events.isVisible = show
  }

  private fun displayCrew(crewList: List<Crew>) {
    if (crewList.isNotEmpty()) {
      crewList.forEachIndexed { index, crew ->
        val crewView = CrewView(this)
        crewView.crew = crew

        if (index < crewList.size) {
          crewView.addBottomMargin()
        }

        list_crew.addView(crewView)
      }
    }

    showCrew(crewList.isNotEmpty())
  }

  private fun showCrew(show: Boolean) {
    text_crew_title.isVisible = show
    list_crew.isVisible = show
  }

  private fun displaySectors(sectors: List<Sector>) {
    val sectorSize = sectors.size

    sectors.forEachIndexed { index, sector ->
      val hasReturnFlight = if (index + 1 < sectorSize) {
        sectors[index + 1].isReturnFlight(sector)
      } else {
        false
      }

      val sectorView = SectorDetailsView(this)
      sectorView.sector = sector
      if (!hasReturnFlight) {
        sectorView.includeBottomMargin(true)
      }
      list_sectors.addView(sectorView)
    }
  }

  private fun showFlightInfo(show: Boolean) {
    group_flight_info.isVisible = show
  }

  private fun showStandbyInfo(show: Boolean) {
    group_standby_info.isVisible = show
  }

  private fun showSectorsSection(show: Boolean) {
    text_sectors_title.isVisible = show
    list_sectors.isVisible = show
  }
}