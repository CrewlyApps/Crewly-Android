package com.crewly.account

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.view.MenuItem
import com.crewly.R
import com.crewly.activity.AppNavigator
import com.crewly.app.NavigationScreen
import com.crewly.app.RxModule
import com.crewly.auth.Account
import com.crewly.utils.plus
import com.crewly.utils.throttleClicks
import com.crewly.views.DatePickerDialog
import com.jakewharton.rxbinding2.widget.checkedChanges
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.BackpressureStrategy
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.account_activity.*
import kotlinx.android.synthetic.main.roster_toolbar.*
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 17/06/2018
 */
class AccountActivity: DaggerAppCompatActivity(), NavigationScreen {

    @Inject override lateinit var appNavigator: AppNavigator
    @Inject lateinit var viewModelFactory: ViewModelProvider.AndroidViewModelFactory
    @field: [Inject Named(RxModule.MAIN_THREAD)] lateinit var mainThread: Scheduler

    override lateinit var drawerLayout: DrawerLayout
    override lateinit var navigationView: NavigationView
    override lateinit var actionBar: ActionBar

    private lateinit var viewModel: AccountViewModel

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_activity)

        setSupportActionBar(toolbar_roster)
        drawerLayout = drawer_layout
        navigationView = navigation_view
        actionBar = supportActionBar!!
        setUpNavigationDrawer(R.id.menu_account)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[AccountViewModel::class.java]
        observeAccount()
        observeCrewSwitch()
        setUpFetchRoster()
        setUpDeleteData()
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
        disposables + viewModel.observeAccount()
                .observeOn(mainThread)
                .subscribe { account ->
                    if (account.crewCode.isNotBlank()) {
                        setUpJoinedCompanySection(account)
                        setUpShowCrewSection(account)
                    }
                }
    }

    private fun observeCrewSwitch() {
        disposables + switch_show_crew
                .checkedChanges()
                .subscribe { checked ->
                    if (checked) {
                        indicator_show_crew.setBackgroundResource(R.drawable.vertical_indicator_selected)
                    } else {
                        indicator_show_crew.setBackgroundResource(R.drawable.vertical_indicator_unselected)
                    }
                }
    }

    private fun setUpJoinedCompanySection(account: Account) {
        val hasSetJoinedAt = account.joinedCompanyAt.millis > 0
        text_joined_company_label.text = getString(R.string.account_joined_company, account.company)

        if (hasSetJoinedAt) {
            val joinedCompanyDate = account.joinedCompanyAt
            text_joined_company_date.text = "${joinedCompanyDate.dayOfMonth().get()}\n${joinedCompanyDate.toString("MMM", Locale.ENGLISH)}\n${joinedCompanyDate.year().get()}"
            indicator_joined_company.setBackgroundResource(R.drawable.vertical_indicator_selected)
            text_joined_company_date.setBackgroundColor(ContextCompat.getColor(this, R.color.account_selected_indicator))

        } else {
            text_joined_company_date.text = getString(R.string.account_set)
            indicator_joined_company.setBackgroundResource(R.drawable.vertical_indicator_unselected)
            text_joined_company_date.setBackgroundColor(ContextCompat.getColor(this, R.color.account_unselected_indicator))
        }

        disposables + text_joined_company_date
                .throttleClicks()
                .subscribe {
                    val datePickerDialog = DatePickerDialog()
                    datePickerDialog.dateSelectedAction = this::handleJoinedCompanyDateSelected
                    datePickerDialog.show(supportFragmentManager, datePickerDialog::class.java.name)
                }
    }

    private fun setUpShowCrewSection(account: Account) {
        if (account.showCrew) {
            indicator_show_crew.setBackgroundResource(R.drawable.vertical_indicator_selected)
            switch_show_crew.isSelected = true
        } else {
            indicator_show_crew.setBackgroundResource(R.drawable.vertical_indicator_unselected)
            switch_show_crew.isSelected = false
        }
    }

    private fun setUpFetchRoster() {
        disposables + button_fetch_roster
                .throttleClicks()
                .subscribe {
                    appNavigator
                            .start()
                            .navigateToLoginScreen()
                            .navigate()
                }
    }

    private fun setUpDeleteData() {
        val deleteDataClicks = button_delete_data
                .throttleClicks()
                .toFlowable(BackpressureStrategy.BUFFER)

        disposables + viewModel.processDeleteDataClicks(deleteDataClicks).subscribe()
    }

    private fun handleJoinedCompanyDateSelected(selectedTime: DateTime) {
        viewModel.saveJoinedCompanyDate(selectedTime)
    }
}