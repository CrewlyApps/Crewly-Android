package com.crewly.roster.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.account.AccountManager
import com.crewly.persistence.crew.Crew
import com.crewly.persistence.duty.Duty
import com.crewly.persistence.sector.Sector
import com.crewly.duty.ryanair.RyanairDutyIcon
import com.crewly.duty.ryanair.RyanairDutyType
import com.crewly.logging.LoggingManager
import com.crewly.models.Company
import com.crewly.models.Flight
import com.crewly.models.duty.FullDuty
import com.crewly.models.roster.RosterPeriod
import com.crewly.repositories.CrewRepository
import com.crewly.roster.RosterRepository
import com.crewly.roster.ryanair.RyanAirRosterHelper
import com.crewly.utils.plus
import dagger.Lazy
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
  private val rosterRepository: RosterRepository,
  private val ryanAirRosterHelper: Lazy<RyanAirRosterHelper>
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
        RosterPeriod.RosterDate(
          date = date,
          sectors = sectors.toMutableList(),
          fullDuties = duties.map { duty -> duty.toFullDuty() }
        )
      })
      .subscribeOn(Schedulers.io())
      .doOnNext { rosterDate ->
        rosterDate.fullDuties.forEach { fullDuty ->
          ryanAirRosterHelper.get().populateDescription(fullDuty.duty)
        }

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

  private fun Duty.toFullDuty(): FullDuty =
    when (company) {
      Company.Ryanair -> FullDuty(
        duty = this,
        dutyType = RyanairDutyType(
          name = type
        ),
        dutyIcon = RyanairDutyIcon(
          dutyName = type
        )
      )

      else -> throw Exception("Company ${company.id} not supported")
    }
}