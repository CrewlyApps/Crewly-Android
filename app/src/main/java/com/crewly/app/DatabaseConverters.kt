package com.crewly.app

import android.arch.persistence.room.TypeConverter
import org.joda.time.DateTime

/**
 * Created by Derek on 13/06/2018
 */
class DatabaseConverters {

    @TypeConverter
    fun fromTimeMillis(time: Long): DateTime = DateTime(time)

    @TypeConverter
    fun toTimeMillis(date: DateTime): Long = date.millis
}