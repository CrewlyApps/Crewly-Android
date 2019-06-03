package com.crewly.logbook

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.account.AccountManager
import com.crewly.app.RxModule
import com.crewly.db.account.Account
import com.crewly.duty.ryanair.RyanairDutyIcon
import com.crewly.duty.ryanair.RyanairDutyType
import com.crewly.models.DateTimePeriod
import com.crewly.models.duty.FullDuty
import com.crewly.models.roster.RosterPeriod
import com.crewly.roster.RosterRepository
import com.crewly.utils.plus
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 26/08/2018
 */
class LogbookViewModel @Inject constructor(
  app: Application,
  private val accountManager: AccountManager,
  private val rosterRepository: RosterRepository,
  @Named(RxModule.IO_THREAD) private val ioThread: Scheduler
):
  AndroidViewModel(app) {

  private val dateTimePeriod = BehaviorSubject.createDefault(
    DateTimePeriod(
      startDateTime = DateTime().minusWeeks(1),
      endDateTime = DateTime()
    )
  )

  private val rosterDates = BehaviorSubject.create<List<RosterPeriod.RosterDate>>()
  private val startDateSelectionEvent = PublishSubject.create<Long>()
  private val endDateSelectionEvent = PublishSubject.create<Long>()

  private val disposables = CompositeDisposable()

  init {
    dateTimePeriod.value?.let {
      fetchRosterDatesBetween(it)
    }
  }

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }

  fun observeAccount(): Flowable<Account> = accountManager.observeCurrentAccount()
  fun observeRosterDates(): Observable<List<RosterPeriod.RosterDate>> = rosterDates.hide()
  fun observeDateTimePeriod(): Observable<DateTimePeriod> = dateTimePeriod.hide()
  fun observeStartDateSelectionEvents(): Observable<Long> = startDateSelectionEvent.hide()
  fun observeEndDateSelectionEvents(): Observable<Long> = endDateSelectionEvent.hide()

  fun startStartDateSelection() {
    dateTimePeriod.value?.let {
      startDateSelectionEvent.onNext(it.startDateTime.millis)
    }
  }

  fun startEndDateSelection() {
    dateTimePeriod.value?.let {
      endDateSelectionEvent.onNext(it.endDateTime.millis)
    }
  }

  fun startDateSelected(startDate: DateTime) {
    dateTimePeriod.value?.let {
      val newDateTimePeriod = it.copy(startDateTime = startDate)
      fetchRosterDatesBetween(newDateTimePeriod)
      dateTimePeriod.onNext(newDateTimePeriod)
    }
  }

  fun endDateSelected(endDate: DateTime) {
    dateTimePeriod.value?.let {
      val newDateTimePeriod = it.copy(endDateTime = endDate)
      fetchRosterDatesBetween(newDateTimePeriod)
      dateTimePeriod.onNext(newDateTimePeriod)
    }
  }

  private fun fetchRosterDatesBetween(dateTimePeriod: DateTimePeriod) {
    disposables + rosterRepository
      .fetchRosterDays(dateTimePeriod)
      .subscribeOn(ioThread)
      .map { rosterDates ->
        rosterDates.map { rosterDate ->
          rosterDate.copy(
            fullDuties = rosterDate.duties.map { duty ->
              FullDuty(
                duty = duty,
                dutyType = RyanairDutyType(
                  name = duty.type
                ),
                dutyIcon = RyanairDutyIcon(
                  dutyName = duty.type
                )
              )
            }
          )
        }
      }
      .subscribe { rosterDates ->
        this.rosterDates.onNext(rosterDates)
      }
  }
}