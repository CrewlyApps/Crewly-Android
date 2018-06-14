package com.crewly.duty

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.joda.time.DateTime

/**
 * Created by Derek on 30/05/2018
 */
@Entity(tableName = "duties")
class DutyType(@PrimaryKey(autoGenerate = true) var id: Long = 0,
                      var type: String = NONE,
                      var date: DateTime = DateTime(),
                      var location: String = "",
                      var description: String = "") {

    companion object {
        const val NONE = "none"
        const val HSBY = "hsby"
        const val ASBY = "asby"
        const val SICK = "sick"
        const val OFF = "off"
        const val BANK_HOLIDAY = "bank holiday"
        const val ANNUAL_LEAVE = "annual leave"
        const val UNPAID_LEAVE = "unpaid leave"
        const val NOT_AVAILABLE = "not available"
        const val PARENTAL_LEAVE = "parental leave"
        const val SPECIAL_EVENT = "special event"
    }
}