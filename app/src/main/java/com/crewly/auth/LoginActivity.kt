package com.crewly.auth

import android.app.ProgressDialog
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.crewly.R
import com.crewly.ScreenState
import com.crewly.app.RxModule
import com.crewly.roster.ryanair.RyanairRosterParser
import com.crewly.utils.addUrlClickSpan
import com.crewly.utils.plus
import com.crewly.utils.throttleClicks
import com.jakewharton.rxbinding3.widget.textChanges
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.login_activity.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 10/06/2018
 */
class LoginActivity: DaggerAppCompatActivity() {

  @Inject lateinit var viewModelFactory: ViewModelProvider.AndroidViewModelFactory
  @Inject lateinit var ryanairRosterParser: RyanairRosterParser
  @field: [Inject Named(RxModule.IO_THREAD)] lateinit var ioThread: Scheduler
  @field: [Inject Named(RxModule.MAIN_THREAD)] lateinit var mainThread: Scheduler

  private lateinit var viewModel: LoginViewModel
  private lateinit var crewDockWebView: CrewDockWebView
  private var progressDialog: ProgressDialog? = null

  private val disposables = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.login_activity)

    viewModel = ViewModelProviders.of(this, viewModelFactory)[LoginViewModel::class.java]
    crewDockWebView = CrewDockWebView(
      context = this,
      loginViewModel = viewModel,
      ryanairRosterParser = ryanairRosterParser,
      ioThread = ioThread,
      mainThread = mainThread
    ).apply {
      rosterParsedAction = viewModel::saveRoster
    }

    setUpCloseButton()
    setUpTitle()
    observeUserNameInput()
    observePasswordInput()
    observeLoginButtonClicks()
    observeScreenState()
  }

  override fun onDestroy() {
    disposables.dispose()
    progressDialog?.dismiss()
    crewDockWebView.destroy()
    super.onDestroy()
  }

  private fun setUpCloseButton() {
    disposables + image_close
      .throttleClicks()
      .subscribe { finish() }
  }

  private fun setUpTitle() {
    text_login_title.text = getString(R.string.login_title, viewModel.webServiceType.serviceName)
  }

  private fun observeUserNameInput() {
    disposables + input_username
      .textChanges()
      .subscribe { textChangeEvent -> viewModel.handleUserNameChange(textChangeEvent.toString()) }
  }

  private fun observePasswordInput() {
    disposables + input_password
      .textChanges()
      .subscribe { textChangeEvent -> viewModel.handlePasswordChange(textChangeEvent.toString()) }
  }

  private fun observeLoginButtonClicks() {
    disposables + button_login
      .throttleClicks()
      .subscribe { viewModel.handleLoginAttempt() }
  }

  private fun observeScreenState() {
    disposables + viewModel.observeScreenState()
      .observeOn(mainThread)
      .subscribe { screenState ->
        when (screenState) {
          is ScreenState.Loading -> {
            val loadingMessage = when (screenState.loadingId) {
              ScreenState.Loading.LOGGING_IN -> getString(R.string.login_logging_in)
              ScreenState.Loading.FETCHING_ROSTER -> getString(R.string.login_fetching_roster)
              else -> null
            }

            loadingMessage?.let {
              text_error.visibility = View.INVISIBLE
              progressDialog?.dismiss()
              progressDialog = ProgressDialog.show(this, null,
                loadingMessage, true, false)
            }
          }

          is ScreenState.Success -> {
            progressDialog?.dismiss()
            Toast.makeText(this, R.string.login_save_roster_success, Toast.LENGTH_SHORT).show()
            finish()
          }

          is ScreenState.Error -> {
            val errorMessage = addServiceTypeLink(screenState.errorMessage)
            text_error.movementMethod = LinkMovementMethod()
            text_error.text = errorMessage
            text_error.isVisible = true
            progressDialog?.dismiss()
          }
        }
      }
  }

  /**
   * Adds a link to the login url contained in [message] if present.
   */
  private fun addServiceTypeLink(message: String): SpannableString {
    val linkSpan = SpannableString(message)
    val serviceType = viewModel.webServiceType
    val indexOfServiceName = message.indexOf(serviceType.serviceName)

    if (indexOfServiceName != -1) {
      linkSpan.addUrlClickSpan(this, serviceType.loginUrl, indexOfServiceName,
        indexOfServiceName + serviceType.serviceName.length)
    }

    return linkSpan
  }
}