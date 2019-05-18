package com.crewly.roster.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.app.RxModule
import com.crewly.db.Crew
import com.crewly.duty.Duty
import com.crewly.duty.Flight
import com.crewly.duty.Sector
import com.crewly.logging.LoggingManager
import com.crewly.repositories.CrewRepository
import com.crewly.roster.RosterPeriod
import com.crewly.roster.RosterRepository
import com.crewly.roster.ryanair.RyanAirRosterHelper
import com.crewly.utils.plus
import dagger.Lazy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 15/07/2018
 */
class RosterDetailsViewModel @Inject constructor(
  application: Application,
  private val loggingManager: LoggingManager,
  private val crewRepository: CrewRepository,
  private val rosterRepository: RosterRepository,
  private val ryanAirRosterHelper: Lazy<RyanAirRosterHelper>,
  @Named(RxModule.IO_THREAD) private val ioThread: Scheduler
):
  AndroidViewModel(application) {

  private val rosterDate = BehaviorSubject.create<RosterPeriod.RosterDate>()
  private val flight = BehaviorSubject.create<Flight>()
  private val crew = BehaviorSubject.create<List<Crew>>()

  private val disposables = CompositeDisposable()

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }

  fun observeRosterDate(): Observable<RosterPeriod.RosterDate> = rosterDate.hide()
  fun observeFlight(): Observable<Flight> = flight.hide()
  fun observeCrew(): Observable<List<Crew>> = crew.hide()

  fun fetchRosterDate(date: DateTime) {
    disposables + Flowable.combineLatest(
      rosterRepository.fetchDutiesForDay(date),
      rosterRepository.fetchSectorsForDay(date),
      BiFunction<List<Duty>, List<Sector>, RosterPeriod.RosterDate> { duties, sectors ->
        RosterPeriod.RosterDate(date, duties.toMutableList(), sectors.toMutableList())
      })
      .subscribeOn(ioThread)
      .doOnNext { rosterDate ->
        rosterDate.duties.forEach {
          ryanAirRosterHelper.get().populateDescription(it)
        }

        this.rosterDate.onNext(rosterDate)
      }
      .map { rosterDate -> rosterDate.sectors }
      .filter { sectors -> sectors.isNotEmpty() }
      .map { sectors -> Flight(
        departureSector = sectors.first(),
        arrivalSector = sectors.last()
      )}
      .flatMap { flight ->
        rosterRepository
          .fetchDepartureAirportForSector(flight.departureSector)
          .map { airport ->
            flight.departureAirport = airport
            flight
          }
          .toFlowable()
      }
      .flatMap { flight ->
        rosterRepository
          .fetchArrivalAirportForSector(flight.arrivalSector)
          .map { airport ->
            flight.arrivalAirport = airport
            flight
          }
          .toFlowable()
      }
      .doOnNext { flight ->
        this.flight.onNext(flight)
      }
      .flatMap { flight ->
        crewRepository
          .getCrew(
            ids = flight.departureSector.crew.toList()
          )
          .toFlowable()
      }
      .subscribe({ crew -> this.crew.onNext(crew) },
        { error -> loggingManager.logError(error) })
  }
}