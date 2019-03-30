package com.crewly.utils

import android.app.Activity
import android.view.ViewGroup

/**
 * Created by Derek on 06/08/2018
 */

/**
 * Get the content view for an [Activity]
 */
fun Activity.findContentView(): ViewGroup = findViewById(android.R.id.content)