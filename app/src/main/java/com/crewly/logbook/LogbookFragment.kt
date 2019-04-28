package com.crewly.logbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crewly.R
import com.crewly.activity.AppNavigator
import com.crewly.app.RxModule
import com.crewly.duty.DutyDisplayHelper
import com.crewly.duty.ryanair.RyanairDutyIcon
import com.crewly.roster.RosterPeriod
import com.crewly.utils.plus
import com.crewly.utils.throttleClicks
import com.crewly.views.DatePickerDialog
import dagger.android.support.DaggerFragment
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.account_toolbar.*
import kotlinx.android.synthetic.main.logbook_fragment.*
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 28/04/2019
 */
class LogbookFragment: DaggerFragment() {

  @Inject lateinit var appNavigator: AppNavigator
  @Inject lateinit var viewModelFactory: ViewModelProvider.AndroidViewModelFactory
  @Inject lateinit var dutyDisplayHelper: DutyDisplayHelper
  @field: [Inject Named(RxModule.MAIN_THREAD)] lateinit var mainThread: Scheduler

  private lateinit var viewModel: LogbookViewModel

  private val logbookDayAdapter = LogbookDayAdapter()
  private val disposables = CompositeDisposable()

  private val timeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

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
      .observeOn(mainThread)
      .subscribe { account ->
        if (account.crewCode.isNotBlank()) {
          (requireActivity() as AppCompatActivity).title = getString(R.string.logbook_title, account.crewCode)
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
      .observeOn(mainThread)
      .subscribe { initialTime ->
        DatePickerDialog.getInstance(initialTime).apply {
          dateSelectedAction = viewModel::startDateSelected
          show((requireActivity() as AppCompatActivity).supportFragmentManager, this::class.java.name)
        }
      }
  }

  private fun observeEndDateSelectionEvents() {
    disposables + viewModel
      .observeEndDateSelectionEvents()
      .observeOn(mainThread)
      .subscribe { initialTime ->
        DatePickerDialog.getInstance(initialTime).apply {
          dateSelectedAction = viewModel::endDateSelected
          show((requireActivity() as AppCompatActivity).supportFragmentManager, this::class.java.name)
        }
      }
  }

  private fun setUpSummarySection(rosterDates: List<RosterPeriod.RosterDate>) {
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

  private fun setUpDaysSection(rosterDates: List<RosterPeriod.RosterDate>) {
    logbookDayAdapter.setData(
      rosterDates
        .fold(mutableListOf()) { data, rosterDate ->
          data.add(LogbookDayData.DateHeaderData(
            date = rosterDate.date,
            dutyIcon = RyanairDutyIcon(rosterDate.duties.firstOrNull()?.type ?: "")
          ))

          val sectors = rosterDate.sectors
          val sectorSize = sectors.size
          data.addAll(rosterDate.sectors.mapIndexed { index, sector ->
            val hasReturnFlight = if (index + 1 < sectorSize) {
              sectors[index + 1].isReturnFlight(sector)
            } else {
              false
            }

            LogbookDayData.SectorDetailsData(
              sector = sector,
              includeBottomMargin = !hasReturnFlight
            )
          })

          data
        })
  }
}