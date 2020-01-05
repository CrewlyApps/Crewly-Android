package com.crewly.roster.raw

import androidx.lifecycle.ViewModel
import com.crewly.repositories.RawRosterRepository
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class RawRosterViewModel @Inject constructor(
  private val rawRosterRepository: RawRosterRepository
) : ViewModel() {

  private val disposables = CompositeDisposable()

  override fun onCleared() {
    disposables.dispose()
    super.onCleared()
  }
}