package com.crewly.salary

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.crewly.R
import com.crewly.utils.*
import com.crewly.views.EnterExitRightView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.salary_view.view.*

/**
 * Created by Derek on 06/08/2018
 * Allows users to update their salary settings.
 */
class SalaryView @JvmOverloads constructor(context: Context,
                                           attributes: AttributeSet? = null,
                                           defStyle: Int = 0):
        ConstraintLayout(context, attributes, defStyle), EnterExitRightView {

    override val view: View = this
    private val disposables = CompositeDisposable()

    init {
        setBackgroundColor(context.getColorCompat(R.color.white))
        isClickable = true
        inflate(R.layout.salary_view, attachToRoot = true)
        setUpPadding()

        observeCloseImage()
    }

    override fun onDetachedFromWindow() {
        disposables.dispose()
        super.onDetachedFromWindow()
    }

    private fun setUpPadding() {
        val horizontalPadding = context.resources.getDimensionPixelOffset(R.dimen.salary_horizontal_padding)
        smartPadding(leftPadding = horizontalPadding, rightPadding = horizontalPadding, topPadding = horizontalPadding)
    }

    private fun observeCloseImage() {
        disposables + image_close
                .throttleClicks()
                .subscribe {
                    hideView()
                }
    }
}