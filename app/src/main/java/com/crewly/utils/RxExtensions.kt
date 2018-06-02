package com.crewly.utils

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Derek on 27/05/2018
 */

operator fun CompositeDisposable.plus(disposable: Disposable) { add(disposable) }