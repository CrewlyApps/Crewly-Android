package com.crewly.roster.list

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import com.crewly.R
import com.crewly.ScreenState
import com.crewly.activity.AppNavigator
import com.crewly.app.NavigationScreen
import com.crewly.app.RxModule
import com.crewly.utils.plus
import com.crewly.utils.visible
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
class RosterListActivity: DaggerAppCompatActivity(), NavigationScreen {

    @Inject override lateinit var appNavigator: AppNavigator
    @Inject lateinit var viewModelFactory: ViewModelProvider.AndroidViewModelFactory
    @field: [Inject Named(RxModule.MAIN_THREAD)] lateinit var mainThread: Scheduler
    @Inject lateinit var rosterListAdapter: RosterListAdapter

    override lateinit var drawerLayout: DrawerLayout
    override lateinit var navigationView: NavigationView
    override lateinit var actionBar: ActionBar

    private lateinit var viewModel: RosterListViewModel

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.roster_activity)

        setSupportActionBar(toolbar_roster)
        supportActionBar?.title = getString(R.string.roster_list_title)
        drawerLayout = drawer_layout
        navigationView = navigation_view
        actionBar = supportActionBar!!
        setUpNavigationDrawer(R.id.menu_roster)

        list_roster.adapter = rosterListAdapter
        list_roster.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[RosterListViewModel::class.java]
        observeScreenState()
        observeRoster()
    }

    override fun onDestroy() {
        rosterListAdapter.onDestroy()
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

    private fun observeRoster() {
        disposables + viewModel
                .observeRosterMonths()
                .observeOn(mainThread)
                .subscribe { rosterMonths ->
                    if (rosterMonths.isEmpty()) {
                        addEmptyView()
                        showDayTabs(false)
                    } else {
                        removeEmptyView()
                        showDayTabs(true)
                    }
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

    private fun showDayTabs(show: Boolean) {
        tab_monday.visible(show)
        tab_tuesday.visible(show)
        tab_wednesday.visible(show)
        tab_thursday.visible(show)
        tab_friday.visible(show)
        tab_saturday.visible(show)
        tab_sunday.visible(show)
    }

    private fun addEmptyView() {
        val emptyView = RosterListEmptyView(this, appNavigator = appNavigator)
        emptyView.id = R.id.roster_list_empty_view
        container_screen.addView(emptyView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(container_screen)
        constraintSet.constrainHeight(emptyView.id, 0)
        constraintSet.constrainWidth(emptyView.id, 0)
        constraintSet.connect(emptyView.id, ConstraintSet.TOP, R.id.toolbar_roster, ConstraintSet.BOTTOM)
        constraintSet.connect(emptyView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(emptyView.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)
        constraintSet.connect(emptyView.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
        constraintSet.applyTo(container_screen)
    }

    private fun removeEmptyView() {
        val emptyView = findViewById<View>(R.id.roster_list_empty_view)
        emptyView?.let { container_screen.removeView(it) }
    }
}
