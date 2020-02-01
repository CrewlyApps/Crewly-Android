package com.crewly.logbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crewly.R
import com.crewly.activity.AppNavigator
import com.crewly.duty.DutyDisplayHelper
import com.crewly.views.flight.FlightViewData
import com.crewly.models.duty.DutyType
import com.crewly.models.roster.RosterPeriod
import com.crewly.utils.TimeDisplay
import com.crewly.utils.plus
import com.crewly.utils.throttleClicks
import com.crewly.views.DatePickerDialog
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.account_toolbar.*
import kotlinx.android.synthetic.main.logbook_fragment.*
import javax.inject.Inject

/**
 * Created by Derek on 28/04/2019
 */
class LogbookFragment: DaggerFragment() {

  @Inject lateinit var appNavigator: AppNavigator
  @Inject lateinit var viewModelFactory: ViewModelProvider.AndroidViewModelFactory
  @Inject lateinit var dutyDisplayHelper: DutyDisplayHelper
  @Inject lateinit var timeDisplay: TimeDisplay

  private lateinit var viewModel: LogbookViewModel

  private val logbookDayAdapter = LogbookDayAdapter()
  private val disposables = CompositeDisposable()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
    inflater.inflate(R.layout.logbook_fragment, container, false)

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setUpToolbar()
    setUpDayList()

    viewModel = ViewModelProviders.of(this, viewModelFactory)[LogbookViewModel::class.java]

    observeAccount()
    observeDateTimePeriod()
    observeRosterDates()
    observeFromDateButtonClicks()
    observeToDateButtonClicks()
    observeStartDateSelectionEvents()
    observeEndDateSelectionEvents()
  }

  override fun onDestroy() {
    list_day_details.adapter = null
    disposables.dispose()
    super.onDestroy()
  }

  private fun setUpToolbar() {
    (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar_account)
  }

  private fun setUpDayList() {
    list_day_details.adapter = logbookDayAdapter
    list_day_details.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
  }

  private fun observeAccount() {
    disposables + viewModel
      .observeAccount()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { account ->
        if (account.crewCode.isNotBlank()) {
          (requireActivity() as AppCompatActivity).title = getString(R.string.logbook_title, account.crewCode)
        }
      }
  }

  private fun observeDateTimePeriod() {
    disposables + viewModel
      .observeDateTimePeriod()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { dateTimePeriod ->
        button_from_date.text = timeDisplay.buildDisplayTime(
          format = TimeDisplay.Format.DATE,
          time = dateTimePeriod.startDateTime
        )

        button_to_date.text = timeDisplay.buildDisplayTime(
          format = TimeDisplay.Format.DATE,
          time = dateTimePeriod.endDateTime
        )
      }
  }

  private fun observeRosterDates() {
    disposables + viewModel
      .observeRosterDates()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { rosterDates ->
        setUpSummarySection(rosterDates)
        setUpDaysSection(rosterDates)
      }
  }

  private fun observeFromDateButtonClicks() {
    disposables + button_from_date
      .throttleClicks()
      .subscribe { viewModel.startStartDateSelection() }
  }

  private fun observeToDateButtonClicks() {
    disposables + button_to_date
      .throttleClicks()
      .subscribe { viewModel.startEndDateSelection() }
  }

  private fun observeStartDateSelectionEvents() {
    disposables + viewModel
      .observeStartDateSelectionEvents()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { initialTime ->
        DatePickerDialog.getInstance(initialTime).apply {
          dateSelectedAction = viewModel::startDateSelected
        }.show((requireActivity() as AppCompatActivity).supportFragmentManager, this::class.java.name)
      }
  }

  private fun observeEndDateSelectionEvents() {
    disposables + viewModel
      .observeEndDateSelectionEvents()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { initialTime ->
        DatePickerDialog.getInstance(initialTime).apply {
          dateSelectedAction = viewModel::endDateSelected
        }.show((requireActivity() as AppCompatActivity).supportFragmentManager, this::class.java.name)
      }
  }

  private fun setUpSummarySection(
    rosterDates: List<RosterPeriod.RosterDate>
  ) {
    dutyDisplayHelper.getDutyDisplayInfo(rosterDates)
      .apply {
        displayNumberOfFlights(totalNumberOfFlights)
        displayDutyTime(totalDutyTime)
        displayFlightTime(totalFlightDuration)
        displayFlightDutyPeriod(totalFlightDutyPeriod)
        displaySalary(totalSalary)
      }
  }

  private fun displayNumberOfFlights(
    numberOfFlights: Int
  ) {
    text_number_of_flights.text = numberOfFlights.toString()
  }

  private fun displayDutyTime(
    dutyTime: String
  ) {
    text_duty_time.text = dutyTime
  }

  private fun displayFlightTime(
    flightTime: String
  ) {
    text_flight_time.text = flightTime
  }

  private fun displayFlightDutyPeriod(
    flightDutyPeriod: String
  ) {
    text_flight_duty_time.text = flightDutyPeriod
  }

  private fun displaySalary(
    salary: String
  ) {
    text_salary.text = salary
    text_salary.isVisible = salary.isNotBlank()
    text_salary_label.isVisible = salary.isNotBlank()
  }

  private fun setUpDaysSection(
    rosterDates: List<RosterPeriod.RosterDate>
  ) {
    logbookDayAdapter.setData(
      rosterDates
        .fold(mutableListOf()) { data, rosterDate ->
          data.add(LogbookDayData.DateHeaderData(
            date = rosterDate.date,
            dutyIcon = dutyDisplayHelper.getDutyIcon(
              dutyType = rosterDate.duties.firstOrNull()?.type ?: DutyType("", "")
            )
          ))

          val flights = rosterDate.flights
          val flightSize = flights.size
          data.addAll(rosterDate.flights.mapIndexed { index, flight ->
            val hasReturnFlight = if (index + 1 < flightSize) {
              flights[index + 1].isReturnFlight(flight)
            } else {
              false
            }

            LogbookDayData.FlightDetailsData(
              data = FlightViewData(
                flight = flight,
                arrivalTimeZulu = timeDisplay.buildDisplayTime(
                  format = TimeDisplay.Format.ZULU_HOUR,
                  time = flight.arrivalTime
                ),
                arrivalTimeLocal = timeDisplay.buildDisplayTime(
                  format = TimeDisplay.Format.LOCAL_HOUR,
                  time = flight.arrivalTime,
                  timeZoneId = flight.arrivalAirport.timezone
                ),
                departureTimeZulu = timeDisplay.buildDisplayTime(
                  format = TimeDisplay.Format.ZULU_HOUR,
                  time = flight.departureTime
                ),
                departureTimeLocal = timeDisplay.buildDisplayTime(
                  format = TimeDisplay.Format.LOCAL_HOUR,
                  time = flight.departureTime,
                  timeZoneId = flight.departureAirport.timezone
                ),
                duration = timeDisplay.buildDisplayTimePeriod(
                  startTime = flight.departureTime,
                  endTime = flight.arrivalTime
                )
              ),
              includeBottomMargin = !hasReturnFlight
            )
          })

          data
        })
  }
}