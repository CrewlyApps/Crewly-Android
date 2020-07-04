package com.crewly.logging

import com.crewly.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class LoggingTree(
  private val crashlytics: FirebaseCrashlytics
) : Timber.Tree() {

  init {
    Logger.logDebugAction = { message ->
      d(message)
    }

    Logger.logErrorAction = { error ->
      e(error)
    }
  }

  override fun d(
    message: String?,
    vararg args: Any?
  ) {
    if (shouldLogToConsole()) super.d(message, *args)
  }

  override fun e(
    error: Throwable?
  ) {
    e(
      error = error,
      message = null
    )
  }

  override fun e(
    error: Throwable?,
    message: String?,
    vararg args: Any?
  ) {
    if (shouldLogToConsole()) {
      super.e(error, message, *args)
      error?.printStackTrace()
    }

    if (!message.isNullOrBlank()) crashlytics.log(message)
    if (error != null) crashlytics.recordException(error)
  }

  override fun log(
    priority: Int,
    tag: String?,
    message: String,
    error: Throwable?
  ) {
    if (shouldLogToConsole()) super.log(priority, tag, message)
  }

  private fun shouldLogToConsole(): Boolean = BuildConfig.DEBUG
}