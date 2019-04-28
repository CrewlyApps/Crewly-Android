package com.crewly.utils

import android.app.Activity
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * Created by Derek on 06/08/2018
 */

/**
 * Get the content view for an [Activity]
 */
fun Activity.findContentView(): ViewGroup = findViewById(android.R.id.content)

fun FragmentActivity.replaceAndShow(
  fragment: Fragment,
  @IdRes container: Int
) {
  supportFragmentManager
    .beginTransaction()
    .replace(container, fragment, fragment::class.qualifiedName)
    .commit()
}