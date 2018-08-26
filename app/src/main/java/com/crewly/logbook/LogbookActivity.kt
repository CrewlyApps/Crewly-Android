package com.crewly.logbook

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.view.MenuItem
import com.crewly.R
import com.crewly.activity.AppNavigator
import com.crewly.app.NavigationScreen
import com.crewly.app.RxModule
import com.crewly.roster.RosterPeriod
import com.crewly.utils.plus
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.account_toolbar.*
import kotlinx.android.synthetic.main.logbook_activity.*
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
    @field: [Inject Named(RxModule.MAIN_THREAD)] lateinit var mainThread: Scheduler

    override lateinit var drawerLayout: DrawerLayout
    override lateinit var navigationView: NavigationView
    override lateinit var actionBar: ActionBar

    private lateinit var viewModel: LogbookViewModel

    private val disposables = CompositeDisposable()

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
        observeRosterDates()
        viewModel.fetchInitialRosterDates()
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

    private fun observeRosterDates() {
        disposables + viewModel
                .observeRosterDates()
                .observeOn(mainThread)
                .subscribe { rosterDates ->
                    setUpSectors(rosterDates)
                }
    }

    private fun setUpSectors(rosterDates: List<RosterPeriod.RosterDate>) {
        var numberOfSectors = 0
        rosterDates.forEach {
            numberOfSectors += it.sectors.size
        }

        text_number_of_sectors.text = numberOfSectors.toString()
    }
}