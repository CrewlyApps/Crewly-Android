package com.crewly.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

/**
 * Created by Derek on 27/05/2018
 */

fun ViewGroup.inflate(
  @LayoutRes layoutId: Int,
  parent: ViewGroup = this,
  attachToRoot: Boolean = false
): View =
  LayoutInflater.from(context).inflate(layoutId, parent, attachToRoot)