package com.crewly.account

import android.app.Dialog
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.view.MenuItem
import android.view.View
import com.crewly.BuildConfig
import com.crewly.R
import com.crewly.activity.AppNavigator
import com.crewly.app.NavigationScreen
import com.crewly.app.RxModule
import com.crewly.crew.RankSelectionView
import com.crewly.salary.SalaryView
import com.crewly.utils.*
import com.crewly.views.DatePickerDialog
import com.jakewharton.rxbinding2.widget.checkedChanges
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.account_activity.*
import kotlinx.android.synthetic.main.account_toolbar.*
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

    private var rankSelectionView: RankSelectionView? = null
    private var salaryView: SalaryView? = null
    private var deleteDataDialog: Dialog? = null
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_activity)

        setSupportActionBar(toolbar_account)
        drawerLayout = drawer_layout
        navigationView = navigation_view
        actionBar = supportActionBar!!
        setUpNavigationDrawer(R.id.menu_account)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[AccountViewModel::class.java]
        setUpAppVersion()

        observeAccount()
        observeJoinedCompany()
        observeCrewSwitch()
        observeRank()
        observeFetchRoster()
        observeSalary()
        observeCrewlyPrivacyPolicy()
        observeSendEmail()
        observeFacebookPage()
        observeRateApp()
    }

    override fun onResume() {
        super.onResume()
        setSelectedNavigationDrawerItem(R.id.menu_account)
    }

    override fun onDestroy() {
        deleteDataDialog?.dismiss()
        super.onDestroy()
    }

    override fun onBackPressed() {
        when {
            rankSelectionView != null && rankSelectionView?.isShown == true -> rankSelectionView?.hideView()
            salaryView != null && salaryView?.isShown == true -> salaryView?.hideView()
            else -> super.onBackPressed()
        }
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
                        supportActionBar?.title = account.crewCode
                        setUpJoinedCompanySection(account)
                        setUpShowCrewSection(account)
                        setUpRankSection(account)
                        setUpSalarySection(account)
                        setUpDeleteDataSection(account)
                        observeDeleteData(account)
                    }
                }
    }

    private fun observeJoinedCompany() {
        disposables + text_joined_company_date
                .throttleClicks()
                .mergeWith(text_joined_company_label.throttleClicks())
                .subscribe {
                    val datePickerDialog = DatePickerDialog()
                    datePickerDialog.dateSelectedAction = { selectedTime -> viewModel.saveJoinedCompanyDate(selectedTime)}
                    datePickerDialog.show(supportFragmentManager, datePickerDialog::class.java.name)
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

    private fun observeRank() {
        disposables + image_rank
                .throttleClicks()
                .mergeWith(text_rank_label.throttleClicks())
                .map { viewModel.getAccount() }
                .observeOn(mainThread)
                .subscribe { account ->
                    rankSelectionView = RankSelectionView(this)
                    rankSelectionView?.displayRanks(account.isPilot, account.rank)
                    rankSelectionView?.rankSelectedAction = { rank -> viewModel.saveRank(rank)}
                    rankSelectionView?.visibility = View.INVISIBLE
                    rankSelectionView.elevate()
                    findContentView().addView(rankSelectionView)
                    rankSelectionView?.showView()
                }
    }

    private fun observeFetchRoster() {
        disposables + button_fetch_roster
                .throttleClicks()
                .subscribe {
                    appNavigator
                            .start()
                            .toLoginScreen()
                            .navigate()
                }
    }

    private fun observeSalary() {
        disposables + button_salary
                .throttleClicks()
                .subscribe {
                    salaryView = SalaryView(this)
                    salaryView?.salary = viewModel.getAccount().salary.copy()
                    salaryView?.hideAction = { salary -> salary?.let { viewModel.saveSalary(it) }}
                    salaryView?.visibility = View.INVISIBLE
                    salaryView.elevate()
                    findContentView().addView(salaryView)
                    salaryView?.showView()
                }
    }

    private fun observeCrewlyPrivacyPolicy() {
        disposables + button_crewly_privacy
                .throttleClicks()
                .subscribe {
                    appNavigator
                            .start()
                            .toWebsite(getString(R.string.crewly_privacy_policy_url))
                            .navigate()
                }
    }

    private fun observeDeleteData(account: Account) {
        disposables + button_delete_data
                .throttleClicks()
                .subscribe {
                    AlertDialog.Builder(this)
                            .setMessage(getString(R.string.account_delete_data_message, account.crewCode))
                            .setPositiveButton(R.string.button_delete, {_, _ -> })
                            .setNegativeButton(R.string.button_cancel, {_, _ -> deleteDataDialog?.dismiss()})
                            .create()
                            .show()
                }
    }

    private fun observeSendEmail() {
        disposables + button_email
                .throttleClicks()
                .subscribe {
                    appNavigator
                            .start()
                            .toSendEmail(getString(R.string.support_email))
                            .navigate()
                }
    }

    private fun observeFacebookPage() {
        disposables + button_facebook
                .throttleClicks()
                .subscribe {
                    appNavigator
                            .start()
                            .toWebsite(getString(R.string.facebook_page_url))
                            .navigate()
                }
    }

    private fun observeRateApp() {
        disposables + button_rate_app
                .throttleClicks()
                .subscribe {
                    appNavigator
                            .start()
                            .toPlayStorePage()
                            .navigate()
                }
    }

    private fun setUpJoinedCompanySection(account: Account) {
        val hasSetJoinedAt = account.joinedCompanyAt.millis > 0

        indicator_joined_company.isSelected = hasSetJoinedAt
        text_joined_company_date.visible(hasSetJoinedAt)

        if (hasSetJoinedAt) {
            val joinedCompanyDate = account.joinedCompanyAt
            text_joined_company_label.text = getString(R.string.account_joined_company, account.company)
            text_joined_company_date.text = "${joinedCompanyDate.dayOfMonth().get()}\n${joinedCompanyDate.toString("MMM", Locale.ENGLISH)}\n${joinedCompanyDate.year().get()}"

        } else {
            text_joined_company_label.text = getString(R.string.account_joined_company_select, account.company)
            text_joined_company_date.text = ""
        }
    }

    private fun setUpShowCrewSection(account: Account) {
        val showCrew = account.showCrew
        indicator_show_crew.isSelected = showCrew
        switch_show_crew.isSelected = showCrew
    }

    private fun setUpRankSection(account: Account) {
        val rank = account.rank
        val hasRankValue = rank.getValue() > 0

        indicator_rank.isSelected = hasRankValue

        if (hasRankValue) {
            text_rank_label.text = getString(R.string.account_your_rank, ": \t${rank.getName()}")
            image_rank.visible(true)
            image_rank.setImageResource(rank.getIconRes())
        } else {
            text_rank_label.text = getString(R.string.account_your_rank_select)
            image_rank.visible(false)
        }
    }

    private fun setUpSalarySection(account: Account) {
        val salaryNotEmpty = !account.salary.isEmpty()
        indicator_salary.isSelected = salaryNotEmpty
        button_salary.isSelected = salaryNotEmpty
    }

    private fun setUpDeleteDataSection(account: Account) {
        button_delete_data.text = getString(R.string.account_delete_data, account.crewCode)
    }

    private fun setUpAppVersion() {
        text_app_version.text = BuildConfig.VERSION_NAME
    }
}