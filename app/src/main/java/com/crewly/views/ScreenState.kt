package com.crewly.views

/**
 * Created by Derek on 02/06/2018
 */
sealed class ScreenState {

  object Success: ScreenState()
  object NetworkError: ScreenState()
  data class Loading(val id: Int = 0): ScreenState()
  data class Error(var message: String = ""): ScreenState()
}