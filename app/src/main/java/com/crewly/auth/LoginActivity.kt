package com.crewly.auth

import android.app.ProgressDialog
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import com.crewly.R
import com.crewly.ScreenState
import com.crewly.utils.plus
import com.crewly.utils.throttleClicks
import com.jakewharton.rxbinding2.widget.textChanges
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.login_activity.*
import javax.inject.Inject

/**
 * Created by Derek on 10/06/2018
 */
class LoginActivity: DaggerAppCompatActivity() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.AndroidViewModelFactory

    private lateinit var viewModel: LoginViewModel
    private lateinit var crewDockWebView: CrewDockWebView
    private var progressDialog: ProgressDialog? = null

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[LoginViewModel::class.java]
        crewDockWebView = CrewDockWebView(this, loginViewModel = viewModel)

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
                            text_error.visibility = View.INVISIBLE
                            progressDialog = ProgressDialog.show(this, null,
                                    getString(R.string.login_logging_in), true, false)
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