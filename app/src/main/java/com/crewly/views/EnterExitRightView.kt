package com.crewly.views

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import com.crewly.R

/**
 * Created by Derek on 06/08/2018
 * Describes a view that has a slide in and slide out animation from the right of the screen.
 */
interface EnterExitRightView {

  val view: View

  fun showView() {
    view.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.enter_from_right))
    view.isVisible = true
  }

  fun hideView() {
    val exitAnimation = AnimationUtils.loadAnimation(view.context, R.anim.exit_to_right)
    exitAnimation.setAnimationListener(object: Animation.AnimationListener {
      override fun onAnimationStart(animation: Animation?) {}
      override fun onAnimationRepeat(animation: Animation?) {}

      override fun onAnimationEnd(animation: Animation?) {
        view.parent?.let { (it as ViewGroup).removeView(view) }
        view.isVisible = false
      }
    })

    view.startAnimation(exitAnimation)
  }
}