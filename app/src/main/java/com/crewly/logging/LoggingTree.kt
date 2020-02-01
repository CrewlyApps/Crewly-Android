package com.crewly.logging

import android.util.Log
import com.crashlytics.android.Crashlytics
import com.crewly.BuildConfig
import timber.log.Timber

class LoggingTree : Timber.Tree() {

  override fun e(
    error: Throwable?
  ) {
    e(
      error = error,
      message = ""
    )
  }

  override fun e(
    error: Throwable?,
    message: String?,
    vararg args: Any?
  ) {
    super.e(error, message, *args)
    Crashlytics.log(message ?: "")
    Crashlytics.logException(error)
  }

  override fun log(
    priority: Int,
    tag: String?,
    message: String,
    error: Throwable?
  ) {
    if (shouldLogToConsole()) Log.println(priority, tag, message)
  }

  private fun shouldLogToConsole(): Boolean = BuildConfig.DEBUG
}