package com.crewly.duty

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import org.joda.time.DateTime
import org.joda.time.Period

/**
 * Created by Derek on 14/06/2018
 */
@Entity(tableName = "sectors",
        primaryKeys = ["flight_id", "departure_time", "departure_airport", "arrival_airport"],
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

    fun getFlightDuration(): Period = Period(departureTime, arrivalTime)

    /**
     * Check whether [sector] is a return flight for this sector.
     */
    fun isReturnFlight(sector: Sector): Boolean =
            departureAirport == sector.arrivalAirport &&
                    arrivalAirport == sector.departureAirport &&
                    flightId.toInt() == sector.flightId.toInt() + 1
}