package com.crewly.roster.details

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.crewly.R
import com.crewly.app.RxModule
import com.crewly.utils.plus
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.roster_details_activity.*
import kotlinx.android.synthetic.main.roster_details_toolbar.*
import org.joda.time.DateTime
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
                        val totalDuration = sectors[0].getFlightDuration()
                        val formatter = PeriodFormatterBuilder()
                                .appendHours()
                                .appendSuffix("h ")
                                .appendMinutes()
                                .appendSuffix("m")
                                .toFormatter()

                        for (i in 1 until sectors.size) {
                            totalDuration.plus(sectors[i].getFlightDuration())
                        }

                        text_flight_time.text = formatter.print(totalDuration)
                    }
                }
    }
}