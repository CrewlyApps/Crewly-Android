package com.crewly.auth

import android.content.Context
import android.util.AttributeSet
import android.webkit.*
import com.crewly.R
import com.crewly.models.ScreenState
import com.crewly.app.RxModule
import com.crewly.models.Company
import com.crewly.models.WebServiceType
import com.crewly.models.roster.Roster
import com.crewly.roster.ryanair.RyanairRosterParser
import com.crewly.utils.plus
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Named

/**
 * Created by Derek on 09/06/2018
 * Connects to the CrewDock website allowing users to login there. Once a user successfully logs in,
 * we can get their roster file from their account.
 */
class CrewDockWebView @JvmOverloads constructor(
  context: Context,
  attributes: AttributeSet? = null,
  defStyle: Int = 0,
  private val loginViewModel: LoginViewModel? = null,
  private val ryanairRosterParser: RyanairRosterParser? = null,
  @Named(RxModule.IO_THREAD) private val ioThread: Scheduler? = null,
  @Named(RxModule.MAIN_THREAD) private val mainThread: Scheduler? = null
):
  WebView(context, attributes, defStyle) {

  companion object {
    private const val CREW_DOCK_JS_INTERFACE = "CrewDockJs"
  }

  var rosterParsedAction: ((roster: Roster) -> Unit)? = null

  private val webServiceType = WebServiceType.CrewDock()
  private var isSunCabinCrew = false

  private class CrewDockJsInterface(
    private val extractUserNameAction: (String?) -> Unit,
    private val extractedRosterAction: (String?) -> Unit
  ) {

    @JavascriptInterface
    fun extractUserName(userName: String?) {
      extractUserNameAction.invoke(userName)
    }

    @JavascriptInterface
    fun extractRoster(html: String?) {
      extractedRosterAction.invoke(html)
    }
  }

  private val crewDockClient = object: WebViewClient() {

    override fun onPageFinished(view: WebView?, url: String?) {
      super.onPageFinished(view, url)
      url ?: return

      when {
        url.contains(webServiceType.loginPath) -> {
          loginViewModel?.let {
            disposables + it.observeScreenState()
              .take(1)
              .filter { screenState -> screenState is ScreenState.Loading }
              .subscribe { inputCredentials() }
          }
        }

        url.endsWith(webServiceType.failedLoginPath) -> {
          loginViewModel?.updateScreenState(ScreenState.Error(context.getString(R.string.login_error_incorrect_details)))
        }

        url.contains("roster", true) -> {
          if (isRosterRestricted(url)) {
            loginViewModel?.updateScreenState(
              ScreenState.Error(
                errorMessage = context.getString(R.string.login_error_pending_documents)
              )
            )

            return
          }

          extractRoster()
        }

        url.contains(webServiceType.userPortalPath) -> {
          loginViewModel?.let {
            disposables + it.createAccount()
              .subscribeOn(ioThread)
              .observeOn(mainThread)
              .subscribe {
                isSunCabinCrew = url.contains(webServiceType.crewSunPath)
                extractUserInfo(url)
                it.updateScreenState(ScreenState.Loading(ScreenState.Loading.FETCHING_ROSTER))
                redirectToRoster()
              }
          }
        }

        else -> loginViewModel?.updateScreenState(ScreenState.Error(context.getString(R.string.login_error_unknown)))
      }
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
      super.onReceivedError(view, request, error)

      loginViewModel?.updateScreenState(
        ScreenState.Error(
          errorMessage = context.getString(R.string.login_error_crewdock)
        )
      )
    }
  }

  private val disposables = CompositeDisposable()

  init {
    CookieManager.getInstance().removeAllCookies(null)
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    addJavascriptInterface(CrewDockJsInterface(::storeUserName, ::parseRoster), CREW_DOCK_JS_INTERFACE)
    webViewClient = crewDockClient

    loginViewModel?.let {
      disposables + it.observeScreenState()
        .filter { screenState ->
          screenState is ScreenState.Loading &&
            screenState.loadingId == ScreenState.Loading.LOGGING_IN
        }
        .subscribe { loadUrl(webServiceType.loginUrl) }
    }
  }

  override fun destroy() {
    CookieManager.getInstance().removeAllCookies(null)
    disposables.dispose()
    super.destroy()
  }

  private fun inputCredentials() {
    val userName = loginViewModel?.userName
    val passWord = loginViewModel?.password

    evaluateJavascript("document.getElementsByName('LoginWidgetH_userName')[0].value = '$userName'", null)
    evaluateJavascript("document.getElementsByName('LoginWidgetH_password')[0].value = '$passWord'", null)
    evaluateJavascript("document.LoginWidgetH_MainForm.submit()", null)
  }

  private fun extractUserInfo(url: String) {
    val isPilot = url.contains("pilot", true)
    loginViewModel?.updateIsPilot(isPilot)
    loginViewModel?.account?.company = Company.Ryanair

    evaluateJavascript("document.getElementById('username').textContent") { value ->
      storeUserName(value)
    }
  }

  private fun redirectToRoster() {
    val rosterUrl = when {
      loginViewModel?.account?.isPilot == true -> webServiceType.pilotRosterPath
      isSunCabinCrew -> webServiceType.crewSunRosterPath
      else -> webServiceType.crewRosterPath
    }
    evaluateJavascript("document.location.href = '$rosterUrl'", null)
  }

  private fun isRosterRestricted(
    url: String
  ): Boolean =
    url.contains(
      other = webServiceType.restrictPath,
      ignoreCase = true
    )

  /**
   * Extracts the roster part from the site HTML
   */
  private fun extractRoster() {
    loadUrl("javascript:window.$CREW_DOCK_JS_INTERFACE.extractRoster(document.getElementById('roster-printable').cloneNode(true).outerHTML);")
  }

  private fun storeUserName(userName: String?) {
    val cleanedUserName = userName
      ?.trim()
      ?.replace("\\n", "")
      ?.replace("\\r", "")

    cleanedUserName?.let {
      loginViewModel?.account?.name = it
    }
  }

  private fun parseRoster(rosterHtml: String?) {
    if (rosterHtml == null) {
      loginViewModel?.updateScreenState(
        ScreenState.Error(
          errorMessage = context.getString(R.string.login_error_pending_documents)
        )
      )

      return
    }

    if (ryanairRosterParser != null && loginViewModel != null) {
      loginViewModel.account?.let { account ->
        disposables + ryanairRosterParser
          .parseRosterFile(
            account = account,
            roster = rosterHtml
          )
          .subscribeOn(ioThread)
          .subscribe({ roster ->
            rosterParsedAction?.invoke(roster)
          },
            {
              loginViewModel.updateScreenState(ScreenState.Error(
                errorMessage = context.getString(R.string.login_error_saving_roster)
              ))
            })
      }
    }
  }
}