package com.crewly.repositories

import javax.inject.Inject

class LoginDisplayDataUseCase @Inject constructor() {

  fun getRyanairFirstRosterFetchWarningMessage() =
    "Due to issues that prevent us from accurately reading some of the roster PDF, we are " +
      "only able to display the first week of each roster."
}