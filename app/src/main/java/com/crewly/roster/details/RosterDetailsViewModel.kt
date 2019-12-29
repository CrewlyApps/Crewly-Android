package com.crewly.roster.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.account.AccountManager
import com.crewly.logging.LoggingManager
import com.crewly.models.Company
import com.crewly.models.Flight
import com.crewly.models.crew.Crew
import com.crewly.models.duty.Duty
import com.crewly.models.roster.RosterPeriod
import com.crewly.models.sector.Sector
import com.crewly.repositories.AirportsRepository
import com.crewly.repositories.CrewRepository
import com.crewly.roster.RosterRepository
import com.crewly.utils.plus
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
  private val airportsRepository: AirportsRepository,
  private val crewRepository: CrewRepository,
  private val rosterRepository: RosterRepository
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
      rosterRepository.fetchDutiesForDay(
        crewCode = accountManager.getCurrentAccount().crewCode,
        date = date
      ),
      rosterRepository.fetchSectorsForDay(
        crewCode = accountManager.getCurrentAccount().crewCode,
        date = date
      ),
      BiFunction<List<Duty>, List<Sector>, RosterPeriod.RosterDate> { duties, sectors ->
        RosterPeriod.RosterDate(
          date = date,
          sectors = sectors.toMutableList(),
          duties = duties
        )
      })
      .subscribeOn(Schedulers.io())
      .doOnNext { rosterDate ->
        this.rosterDate.onNext(rosterDate)
      }
      .map { rosterDate -> rosterDate.sectors }
      .filter { sectors ->
        val hasSectors = sectors.isNotEmpty()
        if (!hasSectors) crew.onNext(listOf())
        hasSectors
      }
      .map { sectors ->
        Flight(
          departureSector = sectors.first(),
          arrivalSector = sectors.last()
        )
      }
      .flatMap { flight ->
        airportsRepository
          .fetchDepartureAirportForSector(flight.departureSector)
          .map { airport ->
            flight.departureAirport = airport
            flight
          }
          .toFlowable()
      }
      .flatMap { flight ->
        airportsRepository
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
        if (accountManager.getCurrentAccount().showCrew) {
          crewRepository
            .getCrew(
              ids = flight.departureSector.crew.toList()
            )
            .toFlowable()
        } else {
          Flowable.just(listOf())
        }
      }
      .subscribe({ crew -> this.crew.onNext(crew) },
        { error -> loggingManager.logError(error) })
  }
}