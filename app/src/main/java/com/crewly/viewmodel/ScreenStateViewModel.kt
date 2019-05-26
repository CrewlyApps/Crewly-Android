package com.crewly.viewmodel

import com.crewly.models.ScreenState
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by Derek on 10/06/2018
 * A view model that can hold the state of the screen, typically in response to a network operation.
 */
interface ScreenStateViewModel {

  val screenState: BehaviorSubject<ScreenState>

  fun observeScreenState(): Observable<ScreenState> = screenState.hide()

  fun updateScreenState(screenState: ScreenState) {
    this.screenState.onNext(screenState)
  }
}