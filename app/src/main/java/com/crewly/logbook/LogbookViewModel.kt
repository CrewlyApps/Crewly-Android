package com.crewly.logbook

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.crewly.account.AccountManager
import com.crewly.logging.Logger
import com.crewly.models.DateTimePeriod
import com.crewly.models.account.Account
import com.crewly.models.roster.RosterPeriod
import com.crewly.repositories.RosterRepository
import com.crewly.utils.plus
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import javax.inject.Inject

/**
 * Created by Derek on 26/08/2018
 */
class LogbookViewModel @Inject constructor(
  app: Application,
  private val accountManager: AccountManager,
  private val rosterRepository: RosterRepository
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

  fun observeAccount(): Observable<Account> = accountManager.observeCurrentAccount()
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

  private fun fetchRosterDatesBetween(
    dateTimePeriod: DateTimePeriod
  ) {
    disposables + rosterRepository.getRosterDays(
      crewCode = accountManager.getCurrentAccount().crewCode,
      dateTimePeriod = dateTimePeriod
    )
      .map { it.sortedByDescending { rosterDate -> rosterDate.date.millis } }
      .subscribeOn(Schedulers.io())
      .subscribe({ rosterDates ->
        this.rosterDates.onNext(rosterDates)
      }) { error -> Logger.logError(error) }
  }
}