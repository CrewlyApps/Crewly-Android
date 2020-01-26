package com.crewly.roster.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.crewly.R
import com.crewly.crew.CrewView
import com.crewly.duty.DutyDisplayHelper
import com.crewly.views.flight.FlightDetailsView
import com.crewly.views.flight.FlightViewData
import com.crewly.models.crew.Crew
import com.crewly.models.duty.Duty
import com.crewly.models.flight.Flight
import com.crewly.utils.TimeDisplay
import com.crewly.utils.plus
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.roster_details_activity.*
import kotlinx.android.synthetic.main.roster_details_toolbar.*
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject

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
  @Inject lateinit var timeDisplay: TimeDisplay

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
    observeFlights()
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
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { rosterDate ->
        val flights = rosterDate.flights

        if (flights.isNotEmpty()) {
          dutyDisplayHelper.getDutyDisplayInfo(listOf(rosterDate))
            .apply {
              displayFlightDuration(totalFlightDuration)
              displayFlightDutyPeriod(totalFlightDutyPeriod)
              displaySalary(totalSalary)
            }

          showFlightInfo(true)
          showStandbyInfo(false)
          showFlightsSection(true)

        } else {
          val standbyDuty = rosterDate.duties.find { duty ->
            duty.type.isAirportStandby() || duty.type.isHomeStandby()
          }

          standbyDuty?.let {
            displayStartTime(it)
            displayEndTime(it)
          }

          showFlightInfo(false)
          showStandbyInfo(standbyDuty != null)
          showFlightsSection(false)
        }

        displayEvents(
          duties = rosterDate.duties
        )
      }
  }

  private fun observeFlights() {
    disposables + viewModel.observeFlights()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { flights ->
        displayFlights(flights)

        if (flights.isNotEmpty()) {
          displayReportLocalTime(flights.first().flight)
          displayLandingLocalTime(flights.last().flight)
        }
      }
  }

  private fun observeCrew() {
    disposables + viewModel
      .observeCrew()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { crew ->
        displayCrew(crew)
      }
  }

  private fun displayDate() {
    val date = DateTime(intent.getLongExtra(DATE_MILLIS_KEY, 0))
    text_current_date.text = timeDisplay.buildDisplayTime(
      format = TimeDisplay.Format.DATE,
      time = date
    )
  }

  private fun displayCurrentTimezone() {
    text_current_timezone.text = TimeZone.getDefault().id
  }

  private fun displayReportLocalTime(
    firstFlight: Flight
  ) {
    text_report_local_time.text = timeDisplay.buildDisplayTime(
      format = TimeDisplay.Format.LOCAL_HOUR,
      time = firstFlight.departureTime.minusMinutes(45),
      timeZoneId = firstFlight.departureAirport.timezone
    )
  }

  private fun displayFlightDuration(
    flightDuration: String
  ) {
    text_flight_time.text = flightDuration
  }

  private fun displayFlightDutyPeriod(
    flightDutyPeriod: String
  ) {
    text_flight_duty_period.text = flightDutyPeriod
  }

  private fun displayLandingLocalTime(
    lastFlight: Flight
  ) {
    text_landing_local_time.text = timeDisplay.buildDisplayTime(
      format = TimeDisplay.Format.LOCAL_HOUR,
      time = lastFlight.arrivalTime,
      timeZoneId = lastFlight.arrivalAirport.timezone
    )
  }

  private fun displayStartTime(
    duty: Duty
  ) {
    text_start_time.text = timeDisplay.buildDisplayTime(
      format = TimeDisplay.Format.HOUR_WITH_LITERALS,
      time = duty.startTime
    )
  }

  private fun displayEndTime(
    duty: Duty
  ) {
    text_end_time.text = timeDisplay.buildDisplayTime(
      format = TimeDisplay.Format.HOUR_WITH_LITERALS,
      time = duty.endTime
    )
  }

  private fun displaySalary(
    salary: String
  ) {
    text_salary.text = salary
    text_salary_label.isVisible = salary.isNotBlank()
    text_salary.isVisible = salary.isNotBlank()
  }

  private fun displayEvents(
    duties: List<Duty>
  ) {
    duties.forEachIndexed { index, duty ->
      val eventView = RosterDetailsEventView(this)
      eventView.displayEvent(
        duty = duty
      )
      if (index < duties.size) {
        eventView.addBottomMargin()
      }
      list_events.addView(eventView)
    }

    showEvents(list_events.childCount > 0)
  }

  private fun showEvents(show: Boolean) {
    text_events_title.isVisible = show
    list_events.isVisible = show
  }

  private fun displayCrew(
    crewList: List<Crew>
  ) {
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

  private fun displayFlights(
    flights: List<FlightViewData>
  ) {
    val flightSize = flights.size

    flights.forEachIndexed { index, flight ->
      val hasReturnFlight = if (index + 1 < flightSize) {
        flights[index + 1].flight.isReturnFlight(flight.flight)
      } else {
        false
      }

      val flightView = FlightDetailsView(this)
      flightView.flightData = flight
      if (!hasReturnFlight) {
        flightView.includeBottomMargin(true)
      }
      list_flights.addView(flightView)
    }
  }

  private fun showFlightInfo(show: Boolean) {
    group_flight_info.isVisible = show
  }

  private fun showStandbyInfo(show: Boolean) {
    group_standby_info.isVisible = show
  }

  private fun showFlightsSection(
    show: Boolean
  ) {
    text_flights_title.isVisible = show
    list_flights.isVisible = show
  }
}