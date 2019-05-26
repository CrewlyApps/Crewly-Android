package com.crewly.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.crewly.R
import com.crewly.utils.inflate

/**
 * Created by Derek on 05/08/2018
 */
class LoadingView: FrameLayout {

  constructor(context: Context): super(context)
  constructor(context: Context, attributes: AttributeSet?): super(context, attributes)
  constructor(context: Context, attributes: AttributeSet?, defStyle: Int = 0): super(context, attributes, defStyle)

  init {
    inflate(R.layout.loading_view, attachToRoot = true)
  }
}