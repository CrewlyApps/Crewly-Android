package com.crewly.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.crewly.R

/**
 * Created by Derek on 22/06/2018
 * TextView that supports using SVGs with compound drawables for pre-Lollipop devices.
 */
class CompoundDrawableTextView @JvmOverloads constructor(
  context: Context,
  attributes: AttributeSet? = null,
  defStyle: Int = 0
):
  AppCompatTextView(context, attributes, defStyle) {

  init {
    setUpView(attributes)
  }

  private fun setUpView(attributes: AttributeSet?) {
    attributes?.let {
      val typedArray = context.obtainStyledAttributes(attributes, R.styleable.CompoundDrawableTextView)
      val startDrawable: Drawable?
      val endDrawable: Drawable?
      val topDrawable: Drawable?
      val bottomDrawable: Drawable?

      startDrawable = typedArray.getDrawable(R.styleable.CompoundDrawableTextView_drawableStartCompat)
      endDrawable = typedArray.getDrawable(R.styleable.CompoundDrawableTextView_drawableEndCompat)
      topDrawable = typedArray.getDrawable(R.styleable.CompoundDrawableTextView_drawableTopCompat)
      bottomDrawable = typedArray.getDrawable(R.styleable.CompoundDrawableTextView_drawableBottomCompat)

      setCompoundDrawablesWithIntrinsicBounds(startDrawable, topDrawable, endDrawable, bottomDrawable)
      typedArray.recycle()
    }
  }
}