package com.crewly.utils

import android.view.View
import android.view.ViewTreeObserver
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Created by Derek on 10/06/2018
 */

/**
 * Set the visibility of a view between [View.VISIBLE] and [View.GONE]
 */
fun View?.visible(visible: Boolean) {
    if (visible) {
        this?.visibility = View.VISIBLE
    } else {
        this?.visibility = View.GONE
    }
}

/**
 * To set even padding on all sides just pass in the padding value for [leftPadding].
 */
fun View?.evenPadding(leftPadding: Int = 0,
                      topPadding: Int = leftPadding,
                      rightPadding: Int = leftPadding,
                      bottomPadding: Int = leftPadding) {
    this?.setPadding(leftPadding, topPadding, rightPadding, bottomPadding)
}

/**
 * All padding values default to their current values so you can set only the ones you need to change.
 */
fun View?.smartPadding(leftPadding: Int = this?.paddingLeft ?: 0,
            topPadding: Int = this?.paddingTop ?: 0,
            rightPadding: Int = this?.paddingRight ?: 0,
            bottomPadding: Int = this?.paddingBottom ?: 0) {
    this?.setPadding(leftPadding, topPadding, rightPadding, bottomPadding)
}

/**
 * Listen to the next view layout event for a view. Once the view has been fully laid out and
 * receives this event, [action] will be called. The view will stop listening to any
 * subsequent events.
 */
inline fun View.listenToViewLayout(crossinline action: () -> Unit) {
    this.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            action.invoke()
            this@listenToViewLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}

/**
 * Throttle successive clicks to a view within specified [throttleTime] of each other
 */
fun View.throttleClicks(throttleTime: Long = 500): Observable<Unit> {
    return this
            .clicks()
            .throttleFirst(throttleTime, TimeUnit.MILLISECONDS)
}