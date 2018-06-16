package com.crewly.roster

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import com.crewly.R
import com.crewly.ScreenState
import com.crewly.app.NavigationScreen
import com.crewly.app.RxModule
import com.crewly.auth.LoginActivity
import com.crewly.utils.plus
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.roster_activity.*
import kotlinx.android.synthetic.main.roster_toolbar.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 27/05/2018
 */
class RosterActivity: DaggerAppCompatActivity(), NavigationScreen {

    @Inject lateinit var viewModelFactory: ViewModelProvider.AndroidViewModelFactory
    @field: [Inject Named(RxModule.MAIN_THREAD)] lateinit var mainThread: Scheduler

    override lateinit var drawerLayout: DrawerLayout
    override lateinit var navigationView: NavigationView
    override lateinit var actionBar: ActionBar

    private lateinit var viewModel: RosterViewModel
    private lateinit var rosterMonthAdapter: RosterMonthAdapter

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.roster_activity)

        setSupportActionBar(toolbar_roster)
        drawerLayout = drawer_layout
        navigationView = navigation_view
        actionBar = supportActionBar!!
        setUpNavigationDrawer(R.id.menu_roster)

        rosterMonthAdapter = RosterMonthAdapter()
        list_roster.adapter = rosterMonthAdapter
        list_roster.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[RosterViewModel::class.java]
        observeScreenState()
        //observeRoster()

        startActivity(Intent(this, LoginActivity::class.java))
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

    private fun observeRoster() {
        disposables + viewModel.observeRoster()
                .observeOn(mainThread)
                .subscribe { roster ->
                    rosterMonthAdapter.roster = roster
                    rosterMonthAdapter.notifyDataSetChanged()
                }
    }

    private fun observeScreenState() {
        disposables + viewModel.observeScreenState()
                .subscribe { screenState ->
                    when (screenState) {
                        is ScreenState.Loading -> {}
                        is ScreenState.Success -> {}
                        is ScreenState.NetworkError -> {}
                        is ScreenState.Error -> {}
                    }
                }
    }
}
