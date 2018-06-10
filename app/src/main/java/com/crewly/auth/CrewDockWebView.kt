package com.crewly.auth

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.crewly.ScreenState
import com.crewly.roster.RosterParser
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
        private const val USER_PORTAL = "web/Portal"
        private const val CABIN_CREW_ROSTER = "Cabin%20Crew/Operational/Roster"
        private const val PILOT_ROSTER = "Pilot/Personal/Roster"

        private const val CREWDOCK_JS_INTERFACE = "CrewDockJs"
    }

    private class CrewDockJsInterface(private val extractUserNameAction: (String?) -> Unit,
                                      private val extractedRosterAction: (String?) -> Unit) {

        @JavascriptInterface
        fun extractUserName(userName: String?) {
            Log.d("username", userName + "")
            extractUserNameAction.invoke(userName)
        }

        @JavascriptInterface
        fun extractRoster(html: String?) {
            Log.d("roster", html + "")
            extractedRosterAction.invoke(html)
        }
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

            if (url == null) { return }

            when {
                url.contains(LOGIN_URL) -> {
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

                url.endsWith(FAILED_LOGIN_URL) -> {
                    loginViewModel?.updateScreenState(ScreenState.Error("Incorrect login details"))
                }

                url.contains("roster", true) -> {
                    extractRoster()
                }

                url.contains(USER_PORTAL) -> {
                    extractUserInfo(url)
                    redirectToRoster()
                }
            }
        }
    }

    init {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        addJavascriptInterface(CrewDockJsInterface(::storeUserName, ::parseRoster), CREWDOCK_JS_INTERFACE)
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

    private fun extractUserInfo(url: String) {
        val isPilot = url.contains("pilot", true)
        Log.d("user", "isPilot = $isPilot")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript("document.getElementById('username').textContent", { value ->
                storeUserName(value)
            })
        } else {
            loadUrl("javascript:window.$CREWDOCK_JS_INTERFACE.extractUserName(document.getElementById('username').textContent);")
        }
    }

    private fun redirectToRoster() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript("document.location.href = '$CABIN_CREW_ROSTER'", null)
        } else {
            loadUrl("javascript:document.location.href = '$CABIN_CREW_ROSTER'")
        }
    }

    /**
     * Extracts the roster part from the site HTML
     */
    private fun extractRoster() {
        loadUrl("javascript:window.$CREWDOCK_JS_INTERFACE.extractRoster(document.getElementById('roster-printable').cloneNode(true).outerHTML);")
    }

    private fun storeUserName(userName: String?) {
        val cleanedUserName = userName
                ?.trim()
                ?.replace("\\n", "")
                ?.replace("\\r", "")
        Log.d("username", "cleaned = $cleanedUserName")
    }

    private fun parseRoster(rosterHtml: String?) {
        if (rosterHtml == null) {
            // TODO Handle pending documents user must read before accessing roster
        } else {
            val rosterParser = RosterParser()
            rosterParser.parseRosterFile(rosterHtml)
            loginViewModel?.updateScreenState(ScreenState.Success)
        }
    }
}