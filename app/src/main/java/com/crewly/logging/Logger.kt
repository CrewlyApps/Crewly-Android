package com.crewly.logging

object Logger {

  var logDebugAction: ((String) -> Unit)? = null
  var logErrorAction: ((Throwable) -> Unit)? = null

  fun logDebug(
    message: String
  ) {
    logDebugAction?.invoke(message)
  }

  fun logError(
    error: Throwable?
  ) {
    error?.let { logErrorAction?.invoke(it) }
  }
}