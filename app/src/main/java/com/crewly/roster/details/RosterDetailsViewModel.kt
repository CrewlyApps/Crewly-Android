package com.crewly.roster.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.account.AccountManager
import com.crewly.duty.sector.SectorViewData
import com.crewly.logging.LoggingManager
import com.crewly.models.crew.Crew
import com.crewly.models.duty.Duty
import com.crewly.models.roster.RosterPeriod
import com.crewly.models.sector.Sector
import com.crewly.repositories.*
import com.crewly.utils.TimeDisplay
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
  private val crewRepository: CrewRepository,
  private val dutiesRepository: DutiesRepository,
  private val sectorsRepository: SectorsRepository,
  private val timeDisplay: TimeDisplay
):
  AndroidViewModel(application) {

  private val rosterDate = BehaviorSubject.create<RosterPeriod.RosterDate>()
  private val flights = BehaviorSubject.create<List<SectorViewData>>()
  private val crew = BehaviorSubject.create<List<Crew>>()

  private val disposables = CompositeDisposable()

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }

  fun observeRosterDate(): Observable<RosterPeriod.RosterDate> = rosterDate.hide()
  fun observeFlights(): Observable<List<SectorViewData>> = flights.hide()
  fun observeCrew(): Observable<List<Crew>> = crew.hide()

  fun fetchRosterDate(
    date: DateTime
  ) {
    disposables + Flowable.combineLatest(
      dutiesRepository.observeDutiesForDay(
        ownerId = accountManager.getCurrentAccount().crewCode,
        date = date
      ),
      sectorsRepository.observeSectorsForDay(
        ownerId = accountManager.getCurrentAccount().crewCode,
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

        flights.onNext(rosterDate.sectors.map { sector ->
          SectorViewData(
            sector = sector,
            arrivalTimeZulu = timeDisplay.buildDisplayTime(
              format = TimeDisplay.Format.ZULU_HOUR,
              time = sector.arrivalTime
            ),
            arrivalTimeLocal = timeDisplay.buildDisplayTime(
              format = TimeDisplay.Format.LOCAL_HOUR,
              time = sector.arrivalTime,
              timeZoneId = sector.arrivalAirport.timezone
            ),
            departureTimeZulu = timeDisplay.buildDisplayTime(
              format = TimeDisplay.Format.ZULU_HOUR,
              time = sector.departureTime
            ),
            departureTimeLocal = timeDisplay.buildDisplayTime(
              format = TimeDisplay.Format.LOCAL_HOUR,
              time = sector.departureTime,
              timeZoneId = sector.departureAirport.timezone
            )
          )
        })
      }
      .map { rosterDate -> rosterDate.sectors }
      .filter { sectors ->
        val hasSectors = sectors.isNotEmpty()
        if (!hasSectors) crew.onNext(listOf())
        hasSectors
      }
      .flatMap { flights ->
        crewRepository
          .getCrew(
            ids = flights.first().crew.toList()
          )
          .toFlowable()
      }
      .subscribe({ crew -> this.crew.onNext(crew) },
        { error -> loggingManager.logError(error) })
  }
}