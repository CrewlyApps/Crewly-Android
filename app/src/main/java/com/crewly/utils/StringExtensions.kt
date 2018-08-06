package com.crewly.utils

/**
 * Created by Derek on 06/08/2018
 */

fun String.removeZeroDecimals(): String =
        this.removeSuffix(".0").removeSuffix(".00")