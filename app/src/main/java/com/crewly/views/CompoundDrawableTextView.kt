package com.crewly.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
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

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        startDrawable = typedArray.getDrawable(R.styleable.CompoundDrawableTextView_drawableStartCompat)
        endDrawable = typedArray.getDrawable(R.styleable.CompoundDrawableTextView_drawableEndCompat)
        topDrawable = typedArray.getDrawable(R.styleable.CompoundDrawableTextView_drawableTopCompat)
        bottomDrawable = typedArray.getDrawable(R.styleable.CompoundDrawableTextView_drawableBottomCompat)
      } else {
        val startDrawableRes = typedArray.getResourceId(R.styleable.CompoundDrawableTextView_drawableStartCompat, -1)
        val endDrawableRes = typedArray.getResourceId(R.styleable.CompoundDrawableTextView_drawableEndCompat, -1)
        val topDrawableRes = typedArray.getResourceId(R.styleable.CompoundDrawableTextView_drawableTopCompat, -1)
        val bottomDrawableRes = typedArray.getResourceId(R.styleable.CompoundDrawableTextView_drawableBottomCompat, -1)

        startDrawable = if (startDrawableRes != -1) AppCompatResources.getDrawable(context, startDrawableRes) else null
        endDrawable = if (endDrawableRes != -1) AppCompatResources.getDrawable(context, endDrawableRes) else null
        topDrawable = if (topDrawableRes != -1) AppCompatResources.getDrawable(context, topDrawableRes) else null
        bottomDrawable = if (bottomDrawableRes != -1) AppCompatResources.getDrawable(context, bottomDrawableRes) else null
      }

      setCompoundDrawablesWithIntrinsicBounds(startDrawable, topDrawable, endDrawable, bottomDrawable)
      typedArray.recycle()
    }
  }
}