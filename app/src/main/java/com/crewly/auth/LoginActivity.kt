package com.crewly.auth

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.crewly.R
import com.crewly.logging.LoggingManager
import com.crewly.models.Company
import com.crewly.views.ScreenState
import com.crewly.utils.plus
import com.crewly.utils.throttleClicks
import com.jakewharton.rxbinding3.widget.textChanges
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.login_activity.*
import javax.inject.Inject

/**
 * Created by Derek on 10/06/2018
 */
class LoginActivity: DaggerAppCompatActivity() {

  @Inject lateinit var viewModelFactory: ViewModelProvider.AndroidViewModelFactory
  @Inject lateinit var loggingManager: LoggingManager

  private lateinit var viewModel: LoginViewModel
  private var progressDialog: ProgressDialog? = null

  private val disposables = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.login_activity)
    viewModel = ViewModelProviders.of(this, viewModelFactory)[LoginViewModel::class.java]
    viewModel.supplyCompany(Company.Norwegian)

    setUpCloseButton()

    observeTitle()
    observeUserNameInput()
    observePasswordInput()
    observeLoginButtonClicks()
    observeScreenState()
    observeUserName()
    observePassword()
  }

  override fun onDestroy() {
    disposables.dispose()
    progressDialog?.dismiss()
    super.onDestroy()
  }

  private fun setUpCloseButton() {
    disposables + image_close
      .throttleClicks()
      .subscribe { finish() }
  }

  private fun observeScreenState() {
    disposables + viewModel.observeScreenState()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { screenState ->
        when (screenState) {
          is ScreenState.Loading -> {
            val loadingMessage = when (screenState.id) {
              LoginViewModel.LOADING_LOGGING_IN -> getString(R.string.login_logging_in)
              LoginViewModel.LOADING_FETCHING_ROSTER -> getString(R.string.login_fetching_roster)
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
            text_error.text = screenState.message
            text_error.isVisible = true
            progressDialog?.dismiss()
          }
        }
      }
  }

  private fun observeTitle() {
    disposables + viewModel
      .observeTitle()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { title ->
        text_login_title.text = title
      }
  }

  private fun observeUserName() {
    disposables + viewModel
      .observeUserName()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { userName ->
        if (input_username.text.toString() != userName) {
          input_username.setText(userName)
        }
      }
  }

  private fun observePassword() {
    disposables + viewModel
      .observePassword()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { password ->
        if (input_password.text.toString() != password) {
          input_password.setText(password)
        }
      }
  }

  private fun observeUserNameInput() {
    disposables + input_username
      .textChanges()
      .skipInitialValue()
      .subscribe { textChangeEvent ->
        viewModel.handleUserNameChange(textChangeEvent.toString())
      }
  }

  private fun observePasswordInput() {
    disposables + input_password
      .textChanges()
      .skipInitialValue()
      .subscribe { textChangeEvent ->
        viewModel.handlePasswordChange(textChangeEvent.toString())
      }
  }

  private fun observeLoginButtonClicks() {
    disposables + button_login
      .throttleClicks()
      .subscribe {
        viewModel.handleLoginAttempt()
      }
  }
}