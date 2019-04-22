package com.crewly.views

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import org.joda.time.DateTime

/**
 * Created by Derek on 23/06/2018
 */
class DatePickerDialog: DialogFragment(), DatePickerDialog.OnDateSetListener {

  companion object {

    private const val INITIAL_DATE_KEY = "InitialDate"
    private const val MAX_SELECTION_DATE_KEY = "MaxSelectionDate"

    fun getInstance(
      initialDate: Long,
      maxSelectionDate: Long = -1
    ): com.crewly.views.DatePickerDialog =
      DatePickerDialog().apply {
        arguments = Bundle().apply {
          putLong(INITIAL_DATE_KEY, initialDate)
          putLong(MAX_SELECTION_DATE_KEY, maxSelectionDate)
        }
      }
  }

  var dateSelectedAction: ((selectedTime: DateTime) -> Unit)? = null

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val initialTime = DateTime(arguments?.getLong(INITIAL_DATE_KEY) ?: System.currentTimeMillis())
    val maxTime = arguments?.getLong(MAX_SELECTION_DATE_KEY, -1) ?: Long.MAX_VALUE

    val dialog = DatePickerDialog(requireContext(), this, initialTime.year,
      initialTime.monthOfYear - 1, initialTime.dayOfMonth)
    dialog.datePicker.maxDate = if (maxTime != -1L) maxTime else Long.MAX_VALUE
    return dialog
  }

  override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
    dateSelectedAction?.invoke(DateTime(year, month + 1, day, 0, 0))
  }
}