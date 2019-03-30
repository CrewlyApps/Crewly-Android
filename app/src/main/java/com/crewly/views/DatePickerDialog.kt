package com.crewly.views

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.DatePicker
import org.joda.time.DateTime

/**
 * Created by Derek on 23/06/2018
 */
class DatePickerDialog: DialogFragment(), DatePickerDialog.OnDateSetListener {

  var dateSelectedAction: ((selectedTime: DateTime) -> Unit)? = null

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val currentTime = DateTime()
    val dialog = DatePickerDialog(context, this, currentTime.year,
      currentTime.monthOfYear, currentTime.dayOfMonth)
    dialog.datePicker.maxDate = currentTime.millis
    return dialog
  }

  override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
    dateSelectedAction?.invoke(DateTime(year, month + 1, day, 0, 0))
  }
}