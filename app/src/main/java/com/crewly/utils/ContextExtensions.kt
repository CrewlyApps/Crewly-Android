package com.crewly.utils

import android.content.Context
import io.reactivex.Observable
import java.nio.charset.Charset

/**
 * Created by Derek on 04/06/2018
 */

fun Context.readAssetsFile(fileName: String): Observable<String> {
    return Observable.create { subscriber ->
        try {
            val input = assets.open(fileName)
            val size = input.available()
            val buffer = ByteArray(size)
            input.read(buffer)
            input.close()

            val output = buffer.toString(Charset.forName("UTF-8"))
            subscriber.onNext(output)
            subscriber.onComplete()

        } catch (exc: Exception) { subscriber.onError(exc) }
    }
}