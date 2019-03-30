package com.crewly.utils

import android.content.Context
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import io.reactivex.Single
import java.nio.charset.Charset

/**
 * Created by Derek on 04/06/2018
 */

fun Context.readAssetsFile(fileName: String): Single<String> =
  Single.fromCallable {
    val input = assets.open(fileName)
    val size = input.available()
    val buffer = ByteArray(size)
    input.read(buffer)
    input.close()

    buffer.toString(Charset.forName("UTF-8"))
  }

@ColorInt
fun Context.getColorCompat(@ColorRes color: Int): Int = ContextCompat.getColor(this, color)