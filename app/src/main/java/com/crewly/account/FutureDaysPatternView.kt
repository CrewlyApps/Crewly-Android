package com.crewly.account

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
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
      input_first_days_on.setTextValue(futureDaysPattern?.firstNumberOfDaysOn ?: 0)
      input_first_days_off.setTextValue(futureDaysPattern?.firstNumberOfDaysOff ?: 0)
      input_second_days_on.setTextValue(futureDaysPattern?.secondNumberOfDaysOn ?: 0)
      input_second_days_off.setTextValue(futureDaysPattern?.secondNumberOfDaysOff ?: 0)
    }

  private var newFutureDaysPattern: FutureDaysPattern? = null
  private val disposables = CompositeDisposable()

  init {
    setBackgroundColor(context.getColorCompat(R.color.white))
    isClickable = true
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
        newFutureDaysPattern = newFutureDaysPattern?.copy(
          firstNumberOfDaysOn = it.toString().toIntOrNull() ?: 0
        )
      }
  }

  private fun observeFirstDaysOffInputChanges() {
    disposables + input_first_days_off
      .textChanges()
      .skipInitialValue()
      .subscribe {
        newFutureDaysPattern = newFutureDaysPattern?.copy(
          firstNumberOfDaysOff = it.toString().toIntOrNull() ?: 0
        )
      }
  }

  private fun observeSecondDaysOnInputChanges() {
    disposables + input_second_days_on
      .textChanges()
      .skipInitialValue()
      .subscribe {
        newFutureDaysPattern = newFutureDaysPattern?.copy(
          secondNumberOfDaysOn = it.toString().toIntOrNull() ?: 0
        )
      }
  }

  private fun observeSecondDaysOffInputChanges() {
    disposables + input_second_days_off
      .textChanges()
      .skipInitialValue()
      .subscribe {
        newFutureDaysPattern = newFutureDaysPattern?.copy(
          secondNumberOfDaysOff = it.toString().toIntOrNull() ?: 0
        )
      }
  }

  private fun TextView.setTextValue(
    value: Int
  ) {
    text = if (value == 0) "" else "$value"
  }
}