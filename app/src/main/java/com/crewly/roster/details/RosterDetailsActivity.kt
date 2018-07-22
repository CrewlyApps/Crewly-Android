package com.crewly.roster.details

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.crewly.R
import com.crewly.app.RxModule
import com.crewly.duty.Sector
import com.crewly.utils.plus
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.roster_details_activity.*
import kotlinx.android.synthetic.main.roster_details_toolbar.*
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
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
    @field: [Inject Named(RxModule.MAIN_THREAD)] lateinit var mainThread: Scheduler

    private val timeFormatter = PeriodFormatterBuilder()
            .appendHours()
            .appendSuffix("h ")
            .appendMinutes()
            .appendSuffix("m")
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
                        val totalFlightsDuration = sectors[0].getFlightDuration()
                        for (i in 1 until sectors.size) {
                            totalFlightsDuration.plus(sectors[i].getFlightDuration())
                        }

                        displayFlightDuration(totalFlightsDuration)
                        displayDutyTime(totalFlightsDuration)
                        displayFlightDutyPeriod(totalFlightsDuration)
                        displaySectors(sectors)
                    }
                }
    }

    private fun displayFlightDuration(duration: Period) {
        text_flight_time.text = timeFormatter.print(duration)
    }

    private fun displayDutyTime(flightsDuration: Period) {
        val dutyTime = Period(flightsDuration)
        dutyTime.plusMinutes(105)
        text_duty_time.text = timeFormatter.print(dutyTime)
    }

    private fun displayFlightDutyPeriod(flightsDuration: Period) {
        val dutyPeriod = Period(flightsDuration)
        dutyPeriod.plusMinutes(75)
        text_flight_duty_period.text = timeFormatter.print(dutyPeriod)
    }

    private fun displaySectors(sectors: List<Sector>) {
        val sectorSize = sectors.size

        sectors.forEachIndexed { index, sector ->
            val hasReturnFlight = if (index + 1 < sectorSize) {
                sectors[index + 1].isReturnFlight(sector)
            } else {
                false
            }

            val sectorView = RosterDetailsSectorView(this)
            sectorView.sector = sector
            if (!hasReturnFlight) { sectorView.addBottomMargin() }
            list_sectors.addView(sectorView)
        }
    }
}