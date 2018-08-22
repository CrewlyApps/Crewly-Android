package com.crewly.roster.list

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.view.MenuItem
import android.view.View
import com.crewly.R
import com.crewly.ScreenState
import com.crewly.account.AccountManager
import com.crewly.activity.AppNavigator
import com.crewly.activity.ScreenDimensions
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
 * A list of roster dates user can scroll through.
 */
class RosterListActivity: DaggerAppCompatActivity(), NavigationScreen {

    @Inject override lateinit var appNavigator: AppNavigator
    @Inject lateinit var accountManager: AccountManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.AndroidViewModelFactory
    @field: [Inject Named(RxModule.MAIN_THREAD)] lateinit var mainThread: Scheduler
    @Inject lateinit var rosterListAdapter: RosterListAdapter
    @Inject lateinit var screenDimensions: ScreenDimensions

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

        val account = accountManager.getCurrentAccount()
        if (account.crewCode.isNotBlank()) {
            setUpNavigationDrawer(R.id.menu_roster)
            setUpNavigationHeader(account)
        } else {
            observeLogin()
        }

        list_roster.adapter = rosterListAdapter
        list_roster.layoutManager = RosterListLayoutManager(this, screenDimensions)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[RosterListViewModel::class.java]
        observeScreenState()
        observeRoster()
    }

    override fun onResume() {
        super.onResume()
        setSelectedNavigationDrawerItem(R.id.menu_roster)
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
                .observeOn(mainThread)
                .subscribe { screenState ->
                    when (screenState) {
                        is ScreenState.Loading -> { loading_view.visible(true) }
                        is ScreenState.Success -> { loading_view.visible(false) }
                        is ScreenState.NetworkError -> { loading_view.visible(false) }
                        is ScreenState.Error -> { loading_view.visible(false) }
                    }
                }
    }

    private fun observeLogin() {
        disposables + accountManager
                .observeAccount()
                .take(1)
                .observeOn(mainThread)
                .subscribe { account ->
                    setUpNavigationDrawer(R.id.menu_roster)
                    setUpNavigationHeader(account)
                }
    }

    private fun showDayTabs(show: Boolean) {
        group_day_tabs.visible(show)
    }

    private fun addEmptyView() {
        if (viewModel.showingEmptyView) { return }

        val emptyView = RosterListEmptyView(this, appNavigator = appNavigator)
        emptyView.id = R.id.roster_list_empty_view
        container_screen.addView(emptyView)
        viewModel.showingEmptyView = true

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
        viewModel.showingEmptyView = false
    }
}
