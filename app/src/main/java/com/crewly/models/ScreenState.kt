package com.crewly.models

/**
 * Created by Derek on 02/06/2018
 */
sealed class ScreenState {

  object Success: ScreenState()
  object NetworkError: ScreenState()

  data class Loading(val loadingId: Int = 0): ScreenState() {

    companion object {
      const val LOGGING_IN = 1
      const val FETCHING_ROSTER = 2
      const val LOADING_ROSTER = 3
    }
  }

  data class Error(var errorMessage: String = ""): ScreenState()
}