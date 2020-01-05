package com.crewly.roster.raw

import androidx.lifecycle.ViewModel
import com.crewly.account.AccountManager
import com.crewly.logging.LoggingManager
import com.crewly.models.roster.RawRoster
import com.crewly.repositories.RawRosterRepository
import com.crewly.utils.plus
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class RawRosterViewModel @Inject constructor(
  private val accountManager: AccountManager,
  private val loggingManager: LoggingManager,
  private val rawRosterRepository: RawRosterRepository
) : ViewModel() {

  private val disposables = CompositeDisposable()

  private val rawRoster = BehaviorSubject.create<RawRoster>()
  private val showLoading = BehaviorSubject.create<Boolean>()

  init {
    fetchRawRoster()
  }

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }

  fun observeRawRoster(): Observable<RawRoster> = rawRoster.hide()
  fun observeShowLoading(): Observable<Boolean> = showLoading.hide()

  private fun fetchRawRoster() {
    disposables + rawRosterRepository
      .getRawRoster(
        ownerId = accountManager.getCurrentAccount().crewCode
      )
      .subscribeOn(Schedulers.io())
      .doOnSubscribe { showLoading.onNext(true) }
      .doOnEvent { _, _ -> showLoading.onNext(false) }
      .subscribe({ rawRoster ->
        this.rawRoster.onNext(rawRoster)
      }, { error ->
        loggingManager.logError(error)
      })
  }
}