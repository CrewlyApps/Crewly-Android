package com.crewly.account

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.crewly.BuildConfig
import com.crewly.R
import com.crewly.activity.AppNavigator
import com.crewly.views.ScreenState
import com.crewly.crew.RankDisplay
import com.crewly.models.account.Account
import com.crewly.salary.SalaryView
import com.crewly.utils.elevate
import com.crewly.utils.findContentView
import com.crewly.utils.plus
import com.crewly.utils.throttleClicks
import com.crewly.views.DatePickerDialog
import com.jakewharton.rxbinding3.widget.checkedChanges
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.account_fragment.*
import kotlinx.android.synthetic.main.account_toolbar.*
import java.util.*
import javax.inject.Inject

/**
 * Created by Derek on 28/04/2019
 */
class AccountFragment: DaggerFragment() {

  @Inject lateinit var appNavigator: AppNavigator
  @Inject lateinit var viewModelFactory: ViewModelProvider.AndroidViewModelFactory
  @Inject lateinit var rankDisplay: RankDisplay

  private lateinit var viewModel: AccountViewModel

  private var salaryView: SalaryView? = null
  private var deleteDataDialog: Dialog? = null
  private val disposables = CompositeDisposable()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
    inflater.inflate(R.layout.account_fragment, container, false)

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setUpToolbar()

    viewModel = ViewModelProviders.of(this, viewModelFactory)[AccountViewModel::class.java]
    setUpAppVersion()

    observeScreenState()
    observeAccount()
    observeJoinedCompany()
    observeCrewSwitch()
    observeFetchRoster()
    observeSalaryClicks()
    observeSalarySelectionEvents()
    observeCrewlyPrivacyPolicy()
    observeSendEmail()
    observeFacebookPage()
    observeRateApp()
  }

  override fun onDestroy() {
    disposables.dispose()
    deleteDataDialog?.dismiss()
    super.onDestroy()
  }

  fun onBackPressed(): Boolean =
    when {
      salaryView != null && salaryView?.isShown == true -> {
        salaryView?.hideView()
        true
      }

      else -> false
    }

  private fun setUpToolbar() {
    (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar_account)
  }

  private fun observeScreenState() {
    disposables + viewModel
      .observeScreenState()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { state ->
        when (state) {
          is ScreenState.Loading -> loading_view.isVisible = true
          is ScreenState.Success -> {
            loading_view.isVisible = false
            Toast.makeText(
              requireContext(),
              getString(R.string.account_delete_data_success),
              Toast.LENGTH_LONG
            ).show()
          }
          is ScreenState.Error -> {
            loading_view.isVisible = false
            Toast.makeText(
              requireContext(),
              state.message,
              Toast.LENGTH_LONG
            ).show()
          }
        }
      }
  }

  private fun observeAccount() {
    disposables + viewModel.observeAccount()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { account ->
        if (account.crewCode.isNotBlank()) {
          (requireActivity() as AppCompatActivity).supportActionBar?.title = account.crewCode
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
        val datePickerDialog = DatePickerDialog.getInstance(
          initialDate = System.currentTimeMillis(),
          maxSelectionDate = System.currentTimeMillis()
        )

        datePickerDialog.dateSelectedAction = { selectedTime -> viewModel.saveJoinedCompanyDate(selectedTime) }
        datePickerDialog.show((requireActivity() as AppCompatActivity).supportFragmentManager,
          datePickerDialog::class.java.name)
      }
  }

  private fun observeCrewSwitch() {
    disposables + switch_show_crew
      .checkedChanges()
      .skipInitialValue()
      .subscribe { checked -> viewModel.saveShowCrew(checked) }
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

  private fun observeSalaryClicks() {
    disposables + button_salary
      .throttleClicks()
      .subscribe { viewModel.handleSalarySelection() }
  }

  private fun observeSalarySelectionEvents() {
    disposables + viewModel
      .observeSalarySelectionEvents()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { account ->
        salaryView = SalaryView(requireContext()).apply {
          salary = account.salary.copy()
          hideAction = { salary -> salary?.let { viewModel.saveSalary(it) } }
          visibility = View.INVISIBLE
          elevate()
          requireActivity().findContentView().addView(this)
          showView()
        }
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
        AlertDialog.Builder(requireContext())
          .setMessage(getString(R.string.account_delete_data_message, account.crewCode))
          .setPositiveButton(R.string.button_delete) { _, _ -> viewModel.deleteUserData() }
          .setNegativeButton(R.string.button_cancel) { _, _ -> deleteDataDialog?.dismiss() }
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
    text_joined_company_date.isVisible = hasSetJoinedAt

    if (hasSetJoinedAt) {
      val joinedCompanyDate = account.joinedCompanyAt
      text_joined_company_label.text = getString(R.string.account_joined_company, account.company.name)
      text_joined_company_date.text = "${joinedCompanyDate.dayOfMonth().get()}\n${joinedCompanyDate.toString("MMM", Locale.ENGLISH)}\n${joinedCompanyDate.year().get()}"

    } else {
      text_joined_company_label.text = getString(R.string.account_joined_company_select, account.company.name)
      text_joined_company_date.text = ""
    }
  }

  private fun setUpShowCrewSection(account: Account) {
    val showCrew = account.showCrew
    indicator_show_crew.isSelected = showCrew
    switch_show_crew.isChecked = showCrew

    if (showCrew) {
      indicator_show_crew.setBackgroundResource(R.drawable.vertical_indicator_selected)
    } else {
      indicator_show_crew.setBackgroundResource(R.drawable.vertical_indicator_unselected)
    }
  }

  private fun setUpRankSection(account: Account) {
    val rank = account.rank
    val hasRankValue = rank.getValue() > 0

    indicator_rank.isSelected = hasRankValue
    indicator_rank.isVisible = hasRankValue
    text_rank_label.isVisible = hasRankValue
    image_rank.isVisible = hasRankValue

    if (hasRankValue) {
      text_rank_label.text = getString(R.string.account_your_rank, ": \t${rank.getName()}")
      image_rank.setImageResource(rankDisplay.getIconForRank(rank))
    }
  }

  private fun setUpSalarySection(account: Account) {
    val hasSalaryInfo = account.salary.hasSalaryInfo()
    indicator_salary.isSelected = hasSalaryInfo
    button_salary.isSelected = hasSalaryInfo
  }

  private fun setUpDeleteDataSection(account: Account) {
    button_delete_data.text = getString(R.string.account_delete_data, account.crewCode)
  }

  private fun setUpAppVersion() {
    text_app_version.text = BuildConfig.VERSION_NAME
  }
}