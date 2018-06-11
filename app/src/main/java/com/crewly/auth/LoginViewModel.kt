package com.crewly.auth

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.crewly.ScreenState
import com.crewly.viewmodel.ScreenStateViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Created by Derek on 10/06/2018
 */
class LoginViewModel @Inject constructor(private val app: Application):
        AndroidViewModel(app), ScreenStateViewModel {

    private val disposables = CompositeDisposable()

    override val screenState = BehaviorSubject.create<ScreenState>()

    var userName: String = ""
    var password: String = ""

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

    fun processUserNameChanges(input: Observable<String>): Observable<String> {
        return input.doOnNext { userName -> this.userName = userName.trim() }
    }

    fun processPasswordChanges(input: Observable<String>): Observable<String> {
        return input.doOnNext { password -> this.password = password }
    }

    fun processLoginButtonClicks(clicks: Observable<Unit>): Observable<Unit> {
        return clicks.doOnNext {
            val validUserName = userName.isNotBlank()
            val validPassword = password.isNotBlank()

            when {
                validUserName && validPassword -> screenState.onNext(ScreenState.Loading(ScreenState.Loading.LOGGING_IN))
                !validUserName && !validPassword -> screenState.onNext(ScreenState.Error("Please enter a username and password"))
                !validUserName -> screenState.onNext(ScreenState.Error("Please enter a username"))
                !validPassword -> screenState.onNext(ScreenState.Error("Please enter a password"))
            }
        }
    }
}