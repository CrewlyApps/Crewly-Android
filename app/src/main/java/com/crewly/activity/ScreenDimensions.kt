package com.crewly.activity

import android.graphics.Point
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import javax.inject.Inject

/**
 * Created by Derek on 07/07/2018
 * Retrieve and exposes the dimensions of the device screen.
 */
@ActivityScope
class ScreenDimensions @Inject constructor(
  activity: AppCompatActivity
) {

  var screenWidth: Int
  var screenHeight: Int

  init {
    val display = activity.windowManager.defaultDisplay
    val metrics = DisplayMetrics()
    display.getMetrics(metrics)

    val screenSize = Point()
    display.getSize(screenSize)

    screenWidth = screenSize.x
    screenHeight = screenSize.y
  }
}