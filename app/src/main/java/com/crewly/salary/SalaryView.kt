package com.crewly.salary

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import com.crewly.R
import com.crewly.models.Salary
import com.crewly.utils.*
import com.crewly.views.EnterExitRightView
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.salary_view.view.*

/**
 * Created by Derek on 06/08/2018
 * Allows users to update their salary settings.
 */
class SalaryView: ConstraintLayout, EnterExitRightView {

  constructor(context: Context): super(context)
  constructor(context: Context, attributes: AttributeSet?): super(context, attributes)
  constructor(context: Context, attributes: AttributeSet?, defStyle: Int = 0): super(context, attributes, defStyle)

  override val view: View = this
  var hideAction: ((salary: Salary?) -> Unit)? = null
  private val disposables = CompositeDisposable()

  var salary: Salary? = null
    set (value) {
      field = value
      value?.let {
        setSalaryInput(input_base_salary, value.perMonthBase)
        setSalaryInput(input_per_flight_time, value.perFlightHour)
        setSalaryInput(input_asby, value.perAsbyHour)
        setSalaryInput(input_hsby, value.perHsbyHour)
        setSalaryInput(input_per_flight_time_oob, value.perFlightHourOob)
      }
    }

  init {
    setBackgroundColor(context.getColorCompat(R.color.white))
    isClickable = true
    inflate(R.layout.salary_view, attachToRoot = true)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    observeCloseImage()
    observeClearButton()
    observeBaseSalaryInput()
    observePerFlightHourInput()
    observeAsbyInput()
    observeHsbyInput()
    observePerFlightHourOobInput()
  }

  override fun onDetachedFromWindow() {
    disposables.clear()
    super.onDetachedFromWindow()
  }

  override fun hideView() {
    hideAction?.invoke(salary)
    focusedChild.hideKeyboard()
    super.hideView()
  }

  private fun setSalaryInput(input: EditText, value: Float) {
    if (value > 0f) {
      input.setText(value.toString().removeZeroDecimals())
    }
  }

  private fun observeCloseImage() {
    disposables + image_close
      .throttleClicks()
      .subscribe {
        hideView()
      }
  }

  private fun observeClearButton() {
    disposables + button_clear
      .throttleClicks()
      .subscribe {
        input_base_salary.setText("")
        input_per_flight_time.setText("")
        input_asby.setText("")
        input_hsby.setText("")
        input_per_flight_time_oob.setText("")
      }
  }

  private fun observeBaseSalaryInput() {
    disposables + input_base_salary
      .textChanges()
      .skipInitialValue()
      .subscribe { input -> salary?.perMonthBase = input.toString().toFloatSafe() }
  }

  private fun observePerFlightHourInput() {
    disposables + input_per_flight_time
      .textChanges()
      .skipInitialValue()
      .subscribe { input -> salary?.perFlightHour = input.toString().toFloatSafe() }
  }

  private fun observeAsbyInput() {
    disposables + input_asby
      .textChanges()
      .skipInitialValue()
      .subscribe { input -> salary?.perAsbyHour = input.toString().toFloatSafe() }
  }

  private fun observeHsbyInput() {
    disposables + input_hsby
      .textChanges()
      .skipInitialValue()
      .subscribe { input -> salary?.perHsbyHour = input.toString().toFloatSafe() }
  }

  private fun observePerFlightHourOobInput() {
    disposables + input_per_flight_time_oob
      .textChanges()
      .skipInitialValue()
      .subscribe { input -> salary?.perFlightHourOob = input.toString().toFloatSafe() }
  }

  private fun String.toFloatSafe(): Float {
    if (this.isEmpty()) return 0f
    return this.toFloatOrNull() ?: 0f
  }
}