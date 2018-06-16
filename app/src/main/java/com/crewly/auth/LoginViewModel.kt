package com.crewly.auth

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.crewly.ScreenState
import com.crewly.app.CrewlyDatabase
import com.crewly.app.CrewlyPreferences
import com.crewly.app.RxModule
import com.crewly.utils.plus
import com.crewly.viewmodel.ScreenStateViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Derek on 10/06/2018
 */
class LoginViewModel @Inject constructor(private val app: Application,
                                         private val crewlyPreferences: CrewlyPreferences,
                                         private val crewlyDatabase: CrewlyDatabase,
                                         @Named(RxModule.IO_THREAD) private val ioThread: Scheduler):
        AndroidViewModel(app), ScreenStateViewModel {

    private val disposables = CompositeDisposable()

    override val screenState = BehaviorSubject.create<ScreenState>()

    var account: Account? = null
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
                validUserName && validPassword -> {
                    fetchAccount()
                    screenState.onNext(ScreenState.Loading(ScreenState.Loading.LOGGING_IN))
                }

                !validUserName && !validPassword -> screenState.onNext(ScreenState.Error("Please enter a username and password"))
                !validUserName -> screenState.onNext(ScreenState.Error("Please enter a username"))
                !validPassword -> screenState.onNext(ScreenState.Error("Please enter a password"))
            }
        }
    }

    fun createAccount(): Completable {
        account = Account(userName)
        return Completable.fromAction { crewlyDatabase.accountDao().insertAccount(account!!) }
    }

    fun saveAccount() {
        disposables + Completable
                .fromAction {
                    crewlyDatabase.accountDao().updateAccount(account!!)
                    crewlyPreferences.saveCurrentAccount(userName)
                }
                .subscribeOn(ioThread)
                .subscribe()
    }

    private fun fetchAccount() {
        disposables + crewlyDatabase.accountDao().fetchAccount(userName)
                .subscribeOn(ioThread)
                .take(1)
                .subscribe( {account -> this.account = account }, { error -> error.printStackTrace() })
    }
}