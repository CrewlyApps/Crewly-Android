package com.crewly.roster.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.account.AccountManager
import com.crewly.views.flight.FlightViewData
import com.crewly.logging.LoggingManager
import com.crewly.models.crew.Crew
import com.crewly.models.duty.Duty
import com.crewly.models.roster.RosterPeriod
import com.crewly.models.flight.Flight
import com.crewly.repositories.*
import com.crewly.utils.TimeDisplay
import com.crewly.utils.plus
import com.crewly.utils.isSameTime
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import javax.inject.Inject

/**
 * Created by Derek on 15/07/2018
 */
class RosterDetailsViewModel @Inject constructor(
  application: Application,
  private val accountManager: AccountManager,
  private val loggingManager: LoggingManager,
  private val crewRepository: CrewRepository,
  private val dutiesRepository: DutiesRepository,
  private val flightRepository: FlightRepository,
  private val timeDisplay: TimeDisplay
):
  AndroidViewModel(application) {

  private val summaryData = BehaviorSubject.create<RosterDetailsSummaryViewData>()
  private val events = BehaviorSubject.create<List<EventViewData>>()
  private val flights = BehaviorSubject.create<List<FlightViewData>>()
  private val crew = BehaviorSubject.create<List<Crew>>()

  private val disposables = CompositeDisposable()

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }

  fun observeSummaryData(): Observable<RosterDetailsSummaryViewData> = summaryData.hide()
  fun observeEvents(): Observable<List<EventViewData>> = events.hide()
  fun observeFlights(): Observable<List<FlightViewData>> = flights.hide()
  fun observeCrew(): Observable<List<Crew>> = crew.hide()

  fun fetchRosterDate(
    date: DateTime
  ) {
    disposables + Flowable.combineLatest(
      dutiesRepository.observeDutiesForDay(
        ownerId = accountManager.getCurrentAccount().crewCode,
        date = date
      ),
      flightRepository.observeFlightsForDay(
        ownerId = accountManager.getCurrentAccount().crewCode,
        date = date
      ),
      BiFunction<List<Duty>, List<Flight>, RosterPeriod.RosterDate> { duties, flights ->
        RosterPeriod.RosterDate(
          date = date,
          flights = flights.toMutableList(),
          duties = duties
        )
      })
      .subscribeOn(Schedulers.io())
      .doOnNext { rosterDate ->
        this.summaryData.onNext(
          RosterDetailsSummaryViewData(
            rosterDate = rosterDate,
            checkInTime = buildCheckInTime(rosterDate.duties),
            checkOutTime = buildCheckOutTime(rosterDate.duties)
          )
        )

        events.onNext(
          rosterDate.duties
            .filter { duty ->
              !duty.type.isCheckIn() && !duty.type.isCheckOut()
            }
            .map { duty ->
              val startAndEndTimeSame = duty.startTime.isSameTime(duty.endTime)
              val endTime = if (startAndEndTimeSame) {
                ""
              } else {
                timeDisplay.buildDisplayTime(
                  format = TimeDisplay.Format.LOCAL_HOUR,
                  time = duty.endTime,
                  timeZoneId = duty.to.timezone
                )
              }

              EventViewData(
                duty = duty,
                startTime = timeDisplay.buildDisplayTime(
                  format = TimeDisplay.Format.LOCAL_HOUR,
                  time = duty.startTime,
                  timeZoneId = duty.from.timezone
                ),
                endTime = endTime
              )
          }
        )

        flights.onNext(rosterDate.flights.map { flight ->
          FlightViewData(
            flight = flight,
            arrivalTimeZulu = timeDisplay.buildDisplayTime(
              format = TimeDisplay.Format.ZULU_HOUR,
              time = flight.arrivalTime
            ),
            arrivalTimeLocal = timeDisplay.buildDisplayTime(
              format = TimeDisplay.Format.LOCAL_HOUR,
              time = flight.arrivalTime,
              timeZoneId = flight.arrivalAirport.timezone
            ),
            departureTimeZulu = timeDisplay.buildDisplayTime(
              format = TimeDisplay.Format.ZULU_HOUR,
              time = flight.departureTime
            ),
            departureTimeLocal = timeDisplay.buildDisplayTime(
              format = TimeDisplay.Format.LOCAL_HOUR,
              time = flight.departureTime,
              timeZoneId = flight.departureAirport.timezone
            ),
            duration = timeDisplay.buildDisplayTime(
              format = TimeDisplay.Format.HOUR_WITH_LITERALS,
              time = flight.arrivalTime.minus(flight.departureTime.millis)
            )
          )
        })
      }
      .map { rosterDate -> rosterDate.flights }
      .filter { flights ->
        val hasFlights = flights.isNotEmpty()
        if (!hasFlights) crew.onNext(listOf())
        hasFlights
      }
      .flatMap { flights ->
        crewRepository
          .getCrew(
            ids = flights.first().crew.toList()
          )
          .toFlowable()
      }
      .subscribe({ crew ->
        this.crew.onNext(
          crew.sortedBy { it.rank.priority }
        )
      },
        { error -> loggingManager.logError(error) })
  }

  private fun buildCheckInTime(
    events: List<Duty>
  ): String =
    events.find { event ->
      event.type.isCheckIn()
    }?.run {
      timeDisplay.buildDisplayTime(
        format = TimeDisplay.Format.LOCAL_HOUR,
        time = this.startTime,
        timeZoneId = this.from.timezone
      )
    } ?: ""

  private fun buildCheckOutTime(
    events: List<Duty>
  ): String =
    events.find { event ->
      event.type.isCheckOut()
    }?.run {
      timeDisplay.buildDisplayTime(
        format = TimeDisplay.Format.LOCAL_HOUR,
        time = this.startTime,
        timeZoneId = this.from.timezone
      )
    } ?: ""
}