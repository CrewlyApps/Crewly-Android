package com.crewly.logging

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class AnalyticsManger @Inject constructor(
  private val firebaseAnalytics: FirebaseAnalytics
) {

  fun recordScreenView(
    screenName: String
  ) {
    firebaseAnalytics.logEvent(
      FirebaseAnalytics.Event.VIEW_ITEM,
      buildBundle(
        name = screenName
      )
    )
  }

  fun recordClick(
    name: String
  ) {
    firebaseAnalytics.logEvent(
      FirebaseAnalytics.Event.SELECT_CONTENT,
      buildBundle(
        name = name
      )
    )
  }

  private fun buildBundle(
    name: String
  ) =
    Bundle().apply {
      putString(FirebaseAnalytics.Param.ITEM_NAME, name)
    }
}