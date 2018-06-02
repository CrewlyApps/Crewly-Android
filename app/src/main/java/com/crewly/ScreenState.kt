package com.crewly

/**
 * Created by Derek on 02/06/2018
 */
sealed class ScreenState {

    object Loading: ScreenState()
    object Success: ScreenState()
    object NetworkError: ScreenState()

    data class Error(var errorMessage: String = ""): ScreenState()
}