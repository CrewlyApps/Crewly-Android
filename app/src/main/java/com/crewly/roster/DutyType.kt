package com.crewly.roster

import org.joda.time.DateTime

/**
 * Created by Derek on 30/05/2018
 */
sealed class DutyType(var date: DateTime,
                      var location: String) {

    class HSBY(date: DateTime = DateTime(),
               location: String = ""): DutyType(date, location)

    class ASBY(date: DateTime = DateTime(),
               location: String = ""): DutyType(date, location)

    class Duty(date: DateTime = DateTime(),
               location: String = ""): DutyType(date, location)

    class Sick(date: DateTime = DateTime(),
               location: String = ""): DutyType(date, location)

    class Off(date: DateTime = DateTime(),
              location: String = ""): DutyType(date, location)

    class BankHoliday(date: DateTime = DateTime(),
                      location: String = ""): DutyType(date, location)

    class AnnualLeave(date: DateTime = DateTime(),
                      location: String = ""): DutyType(date, location)

    class UnpaidLeave(date: DateTime = DateTime(),
                      location: String = ""): DutyType(date, location)

    class NotAvailable(date: DateTime = DateTime(),
                       location: String = ""): DutyType(date, location)

    class ParentalLeave(date: DateTime = DateTime(),
                        location: String = ""): DutyType(date, location)

    data class Sector(var flightId: String = "",
                      var arrivalAirport: String = "",
                      var departureAirport: String = "",
                      var arrivalTime: DateTime = DateTime(),
                      var departureTime: DateTime = DateTime()):
            DutyType(departureTime, departureAirport)

    class SpecialEvent(date: DateTime = DateTime(),
                       location: String = "",
                       val description: String = ""): DutyType(date, location)
}