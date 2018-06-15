package com.crewly.auth

import android.app.ProgressDialog
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import com.crewly.R
import com.crewly.ScreenState
import com.crewly.app.RxModule
import com.crewly.roster.RosterParser
import com.crewly.utils.plus
import com.crewly.utils.throttleClicks
import com.jakewharton.rxbinding2.widget.textChanges
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
    @Inject lateinit var rosterParser: RosterParser
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
        crewDockWebView = CrewDockWebView(this, loginViewModel = viewModel, rosterParser = rosterParser,
                ioThread = ioThread, mainThread = mainThread)

        setUpCloseButton()
        setUpUserNameInput()
        setUpPasswordInput()
        setUpLoginButton()
        observeScreenState()
    }

    override fun onDestroy() {
        progressDialog?.dismiss()
        crewDockWebView.destroy()
        super.onDestroy()
    }

    private fun setUpCloseButton() {
        disposables + image_close.throttleClicks()
                .subscribe { finish() }
    }

    private fun setUpUserNameInput() {
        val userNameTextChanges = input_username
                .textChanges()
                .map { textChangeEvent -> textChangeEvent.toString() }
        disposables + viewModel.processUserNameChanges(userNameTextChanges).subscribe()
    }

    private fun setUpPasswordInput() {
        val passwordTextChanges = input_password
                .textChanges()
                .map { textChangeEvent -> textChangeEvent.toString() }
        disposables + viewModel.processPasswordChanges(passwordTextChanges).subscribe()
    }

    private fun setUpLoginButton() {
        val loginButtonClicks = button_login.throttleClicks()
        disposables + viewModel.processLoginButtonClicks(loginButtonClicks).subscribe()
    }

    private fun observeScreenState() {
        disposables + viewModel.observeScreenState()
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
                        }

                        is ScreenState.Error -> {
                            text_error.text = screenState.errorMessage
                            text_error.visibility = View.VISIBLE
                            progressDialog?.dismiss()
                        }
                    }
                }
    }
}