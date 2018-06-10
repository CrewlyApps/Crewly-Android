package com.crewly.auth

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import com.crewly.ScreenState
import com.crewly.utils.plus
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by Derek on 09/06/2018
 * Connects to the CrewDock website allowing users to login there. Once a user successfully logs in,
 * we can get their roster file from their account.
 */
class CrewDockWebView @JvmOverloads constructor(context: Context,
                                                attributes: AttributeSet? = null,
                                                defStyle: Int = 0,
                                                private val loginViewModel: LoginViewModel? = null):
        WebView(context, attributes, defStyle) {

    companion object {
        private const val BASE_URL = "https://crewdock.com/pport/"
        private const val LOGIN_URL = "web/Login"
        private const val FAILED_LOGIN_URL = "web"
    }

    private val disposables = CompositeDisposable()

    private val crewDockClient = object: WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            Log.d("Started url", url + "")
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.d("Loaded url", url + "")

            when (url) {
                BASE_URL + LOGIN_URL -> {
                    loginViewModel?.let {
                        disposables + it.observeScreenState()
                                .take(1)
                                .subscribe { screenState ->
                                    when (screenState) {
                                        is ScreenState.Loading -> inputCredentials()
                                    }
                                }
                    }
                }

                BASE_URL + FAILED_LOGIN_URL -> {
                    loginViewModel?.updateScreenState(ScreenState.Error("Incorrect login details"))
                }
            }
        }
    }

    init {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        webViewClient = crewDockClient

        loginViewModel?.let {
            disposables + it.observeScreenState()
                    .subscribe { screenState ->
                        when (screenState) {
                            is ScreenState.Loading -> loadUrl(BASE_URL + LOGIN_URL)
                        }
                    }
        }
    }

    override fun destroy() {
        disposables.dispose()
        super.destroy()
    }

    private fun inputCredentials() {
        val userName = loginViewModel?.userName
        val passWord = loginViewModel?.password

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript("document.getElementsByName('LoginWidgetH_userName')[0].value = '$userName'", null)
            evaluateJavascript("document.getElementsByName('LoginWidgetH_password')[0].value = '$passWord'", null)
            evaluateJavascript("document.LoginWidgetH_MainForm.submit()", null)
        } else {
            loadUrl("javascript: {" +
                    "document.getElementsByName('LoginWidgetH_userName')[0].value = '$userName';" +
                    "document.getElementsByName('LoginWidgetH_password')[0].value = '$passWord';" +
                    "var mainForm = document.getElementsByName('LoginWidgetH_MainForm');" +
                    "mainForm[0].submit(); };")
        }
    }
}