package com.crewly.utils

import android.view.View
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Created by Derek on 10/06/2018
 */

/**
 * Throttle successive clicks to a view within specified [throttleTime] of each other
 */
fun View.throttleClicks(throttleTime: Long = 500): Observable<Unit> {
    return this
            .clicks()
            .throttleFirst(throttleTime, TimeUnit.MILLISECONDS)
}