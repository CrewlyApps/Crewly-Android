package com.crewly.account

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.crewly.R
import com.crewly.models.roster.future.FutureDaysPattern
import com.crewly.utils.*
import com.crewly.views.EnterExitRightView
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.future_days_pattern_view.view.*

class FutureDaysPatternView: ConstraintLayout, EnterExitRightView {

  constructor(context: Context): super(context)
  constructor(context: Context, attributes: AttributeSet?): super(context, attributes)
  constructor(context: Context, attributes: AttributeSet?, defStyle: Int = 0): super(context, attributes, defStyle)

  override val view: View = this

  var hideAction: ((pattern: FutureDaysPattern?) -> Unit)? = null

  var futureDaysPattern: FutureDaysPattern? = null
    set(value) {
      field = value
      newFutureDaysPattern = futureDaysPattern?.copy()
      input_first_days_on.setText("${futureDaysPattern?.firstNumberOfDaysOn}")
      input_first_days_off.setText("${futureDaysPattern?.firstNumberOfDaysOff}")
      input_second_days_on.setText("${futureDaysPattern?.secondNumberOfDaysOn}")
      input_second_days_off.setText("${futureDaysPattern?.secondNumberOfDaysOff}")
    }

  private var newFutureDaysPattern: FutureDaysPattern? = null
  private val disposables = CompositeDisposable()

  init {
    setBackgroundColor(context.getColorCompat(R.color.white))
    inflate(R.layout.future_days_pattern_view, attachToRoot = true)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    observeCloseClicks()
    observeFirstDaysOnInputChanges()
    observeFirstDaysOffInputChanges()
    observeSecondDaysOnInputChanges()
    observeSecondDaysOffInputChanges()
  }

  override fun onDetachedFromWindow() {
    disposables.dispose()
    super.onDetachedFromWindow()
  }

  override fun hideView() {
    hideAction?.invoke(newFutureDaysPattern)
    focusedChild.hideKeyboard()
    super.hideView()
  }

  private fun observeCloseClicks() {
    disposables + image_close
      .throttleClicks()
      .subscribe { hideView() }
  }

  private fun observeFirstDaysOnInputChanges() {
    disposables + input_first_days_on
      .textChanges()
      .skipInitialValue()
      .subscribe {
        newFutureDaysPattern = futureDaysPattern?.copy(
          firstNumberOfDaysOn = it.toString().toIntOrNull() ?: 0
        )
      }
  }

  private fun observeFirstDaysOffInputChanges() {
    disposables + input_first_days_off
      .textChanges()
      .skipInitialValue()
      .subscribe {
        newFutureDaysPattern = futureDaysPattern?.copy(
          firstNumberOfDaysOff = it.toString().toIntOrNull() ?: 0
        )
      }
  }

  private fun observeSecondDaysOnInputChanges() {
    disposables + input_second_days_on
      .textChanges()
      .skipInitialValue()
      .subscribe {
        newFutureDaysPattern = futureDaysPattern?.copy(
          secondNumberOfDaysOn = it.toString().toIntOrNull() ?: 0
        )
      }
  }

  private fun observeSecondDaysOffInputChanges() {
    disposables + input_second_days_off
      .textChanges()
      .skipInitialValue()
      .subscribe {
        newFutureDaysPattern = futureDaysPattern?.copy(
          secondNumberOfDaysOff = it.toString().toIntOrNull() ?: 0
        )
      }
  }
}