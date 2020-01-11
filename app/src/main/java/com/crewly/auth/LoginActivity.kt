package com.crewly.auth

import android.os.Bundle
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
            loading_view.isVisible = true
          }

          is ScreenState.Success -> {
            loading_view.isVisible = false
            Toast.makeText(this, R.string.login_save_roster_success, Toast.LENGTH_SHORT).show()
            finish()
          }

          is ScreenState.Error -> {
            text_error.text = screenState.message
            text_error.isVisible = true
            loading_view.isVisible = false
          }
        }
      }
  }

  private fun observeTitle() {
    disposables + viewModel
      .observeTitle()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { title ->
        text_login_title.text = getString(R.string.login_title, title)
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