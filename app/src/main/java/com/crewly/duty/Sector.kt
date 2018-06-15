package com.crewly.duty

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import org.joda.time.DateTime

/**
 * Created by Derek on 14/06/2018
 */
@Entity(tableName = "sectors",
        primaryKeys = ["flight_id", "departure_time"],
        indices = [(Index("departure_time"))])
data class Sector(@ColumnInfo(name = "flight_id")
                  var flightId: String = "",

                  @ColumnInfo(name = "arrival_airport")
                  var arrivalAirport: String = "",

                  @ColumnInfo(name = "departure_airport")
                  var departureAirport: String = "",

                  @ColumnInfo(name = "arrival_time")
                  var arrivalTime: DateTime = DateTime(),

                  @ColumnInfo(name = "departure_time")
                  var departureTime: DateTime = DateTime()) {

    @Ignore constructor(): this("")
}