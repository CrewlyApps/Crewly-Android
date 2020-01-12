package com.crewly.views

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.widget.TextView
import com.crewly.R
import com.crewly.utils.getColorCompat
import org.joda.time.DateTime

/**
 * Created by Derek on 22/04/2019
 */
class DateHeaderView: TextView {

  enum class FormatStyle { FULL, SHORT }

  constructor(context: Context): super(context)
  constructor(context: Context, attributes: AttributeSet?): super(context, attributes)
  constructor(context: Context, attributes: AttributeSet?, defStyle: Int): super(context, attributes, defStyle)

  var date: DateTime? = null
    private set(value) {
      if (field == value) return
      field = value
      styleDateDisplay()
    }

  var formatStyle: FormatStyle = FormatStyle.FULL
    private set

  init {
    typeface = Typeface.DEFAULT_BOLD
  }

  fun displayDate(
    date: DateTime,
    formatStyle: FormatStyle
  ) {
    this.formatStyle = formatStyle
    this.date = date
  }

  private fun styleDateDisplay() {
    text = when (formatStyle) {
      FormatStyle.FULL -> SpannableStringBuilder().run {
        styleYear().styleMonth().styleDay()
      }

      FormatStyle.SHORT -> SpannableStringBuilder().run {
        styleYear().styleMonth()
      }
    }
  }

  private fun SpannableStringBuilder.styleYear(): SpannableStringBuilder {
    val initialIndex = length
    val dateText = date?.year?.toString()?.toUpperCase()
    append(dateText)
    setSpan(ForegroundColorSpan(context.getColorCompat(R.color.text_black_primary)),
      initialIndex, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return this
  }

  private fun SpannableStringBuilder.styleMonth(): SpannableStringBuilder {
    val initialIndex = length
    val dateText = date?.monthOfYear()?.asText?.toUpperCase()
    append(dateText)
    setSpan(ForegroundColorSpan(context.getColorCompat(R.color.text_transparent_grey_label)),
      initialIndex, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return this
  }

  private fun SpannableStringBuilder.styleDay(): SpannableStringBuilder {
    val initialIndex = length
    val dateText = date?.dayOfMonth?.toString()?.toUpperCase()
    append(dateText)
    setSpan(ForegroundColorSpan(context.getColorCompat(R.color.text_black_primary)),
      initialIndex, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return this
  }
}