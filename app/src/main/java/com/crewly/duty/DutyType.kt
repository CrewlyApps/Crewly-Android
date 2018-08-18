package com.crewly.duty

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import org.joda.time.DateTime

/**
 * Created by Derek on 30/05/2018
 * Represents a duty a in user's roster.
 * @param crewCode The id of the user this duty belongs to
 */
@Entity(tableName = "duties")
data class DutyType(@PrimaryKey(autoGenerate = true)
                    var id: Long = 0,

                    @ColumnInfo(name = "crew_code")
                    var crewCode: String = "",

                    var type: String = NONE,
                    var date: DateTime = DateTime(),
                    var location: String = "",

                    @ColumnInfo(name = "special_event_type")
                    var specialEventType: String = "") {

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

    @Ignore constructor(): this(0)
}