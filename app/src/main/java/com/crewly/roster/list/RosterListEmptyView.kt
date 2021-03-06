package com.crewly.roster.list

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.crewly.R
import com.crewly.activity.AppNavigator
import com.crewly.utils.inflate
import com.crewly.utils.plus
import com.crewly.utils.smartPadding
import com.crewly.utils.throttleClicks
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.roster_list_empty_view.view.*

/**
 * Created by Derek on 04/08/2018
 */
class RosterListEmptyView: ConstraintLayout {

  constructor(context: Context): super(context)
  constructor(context: Context, attributes: AttributeSet?): super(context, attributes)
  constructor(context: Context, attributes: AttributeSet?, defStyle: Int = 0): super(context, attributes, defStyle)

  var appNavigator: AppNavigator? = null
  private val disposables = CompositeDisposable()

  init {
    inflate(R.layout.roster_list_empty_view, attachToRoot = true)
    setUpPadding()
    observeFetchRosterButtonClicks()
  }

  override fun onDetachedFromWindow() {
    disposables.dispose()
    super.onDetachedFromWindow()
  }

  private fun setUpPadding() {
    val horizontalPadding = context.resources.getDimensionPixelOffset(R.dimen.roster_list_empty_horizontal_padding)
    val topPadding = context.resources.getDimensionPixelOffset(R.dimen.roster_list_empty_top_padding)
    smartPadding(leftPadding = horizontalPadding, rightPadding = horizontalPadding, topPadding = topPadding)
  }

  private fun observeFetchRosterButtonClicks() {
    disposables + button_fetch_roster
      .throttleClicks()
      .subscribe {
        appNavigator
          ?.start()
          ?.toLoginScreen()
          ?.navigate()
      }
  }
}