package com.crewly.roster.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.crewly.R
import com.crewly.app.RxModule
import com.crewly.duty.Duty
import com.crewly.duty.DutyDisplayHelper
import com.crewly.duty.Flight
import com.crewly.duty.Sector
import com.crewly.duty.sector.SectorDetailsView
import com.crewly.utils.plus
import com.crewly.utils.visible
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.roster_details_activity.*
import kotlinx.android.synthetic.main.roster_details_toolbar.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
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
          showSectors(true)

          dutyDisplayHelper.getDutyDisplayInfo(listOf(rosterDate))
            .apply {
              displayFlightDuration(totalFlightDuration)
              displayDutyTime(totalDutyTime)
              displayFlightDutyPeriod(totalFlightDutyPeriod)
            }

          displaySectors(sectors)
        } else {
          showSectors(false)
        }

        displayEvents(rosterDate.duties)
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

  private fun displayCurrentTimezone() {
    text_current_timezone.text = TimeZone.getDefault().id
  }

  private fun displayReportLocalTime(flight: Flight) {
    val airportTime = DateTime(flight.departureSector.departureTime,
      DateTimeZone.forID(flight.departureAirport.timezone))
    airportTime.minusMinutes(45)
    text_report_local_time.text = dateTimeFormatter.print(airportTime)
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

  private fun displayEvents(duties: List<Duty>) {
    duties.forEachIndexed { index, duty ->
      if (duty.description.isNotBlank()) {
        val eventView = RosterDetailsEventView(this)
        eventView.duty = duty
        if (index < duties.size) {
          eventView.addBottomMargin()
        }
        list_events.addView(eventView)
      }
    }

    showEvents(list_events.childCount > 0)
  }

  private fun showEvents(show: Boolean) {
    text_events_title.visible(show)
    list_events.visible(show)
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
        sectorView.addBottomMargin()
      }
      list_sectors.addView(sectorView)
    }
  }

  private fun showSectors(show: Boolean) {
    text_sectors_title.visible(show)
    list_sectors.visible(show)
  }
}