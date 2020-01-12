package com.crewly.auth

import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.crewly.R
import com.crewly.logging.LoggingManager
import com.crewly.models.account.CrewType
import com.crewly.views.ScreenState
import com.crewly.utils.plus
import com.crewly.utils.throttleClicks
import com.jakewharton.rxbinding3.widget.checkedChanges
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

    setUpCloseButton()

    observeScreenState()
    observeCompany()
    observeCrewType()
    observeName()
    observeCrewCode()
    observePassword()

    observeCrewTypeButtonClicks()
    observeNameInput()
    observeCrewCodeInput()
    observePasswordInput()
    observeRequestRosterButtonClicks()
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

  private fun observeCrewType() {
    disposables + viewModel
      .observeCrewType()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { crewType ->
        when (crewType) {
          CrewType.CABIN -> button_crew_type.check(R.id.button_cabin_crew)
          CrewType.FLIGHT -> button_crew_type.check(R.id.button_flight_crew)
          else -> {}
        }
      }
  }

  private fun observeCompany() {
    disposables + viewModel
      .observeCompany()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { company ->
        text_company.text = company.name
      }
  }

  private fun observeName() {
    disposables + viewModel
      .observeName()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { name ->
        if (input_name.text.toString() != name) {
          input_name.setText(name)
        }
      }
  }

  private fun observeCrewCode() {
    disposables + viewModel
      .observeCrewCode()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { crewCode ->
        if (input_crew_code.text.toString() != crewCode) {
          input_crew_code.setText(crewCode)
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

  private fun observeCrewTypeButtonClicks() {
    disposables + button_crew_type
      .checkedChanges()
      .skipInitialValue()
      .subscribe { id ->
        when (id) {
          R.id.button_cabin_crew -> viewModel.handleCrewTypeChange(CrewType.CABIN)
          R.id.button_flight_crew -> viewModel.handleCrewTypeChange(CrewType.FLIGHT)
        }
      }
  }

  private fun observeNameInput() {
    disposables + input_name
      .textChanges()
      .skipInitialValue()
      .subscribe { textChangeEvent ->
        viewModel.handleNameChange(textChangeEvent.toString())
      }
  }

  private fun observeCrewCodeInput() {
    disposables + input_crew_code
      .textChanges()
      .skipInitialValue()
      .subscribe { textChangeEvent ->
        viewModel.handleCrewCodeChange(textChangeEvent.toString())
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

  private fun observeRequestRosterButtonClicks() {
    disposables + button_request_roster
      .throttleClicks()
      .subscribe {
        viewModel.handleRequestRosterAttempt()
      }
  }
}