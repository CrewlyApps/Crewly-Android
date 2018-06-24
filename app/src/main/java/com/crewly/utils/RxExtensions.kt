package com.crewly.utils

import android.view.View
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Derek on 27/05/2018
 */

operator fun CompositeDisposable.plus(disposable: Disposable) { add(disposable) }

/**
 * Maps an Observable to the [view] supplied.
 */
fun Observable<*>.mapAsView(view: View): Observable<View> { return this.map { view } }