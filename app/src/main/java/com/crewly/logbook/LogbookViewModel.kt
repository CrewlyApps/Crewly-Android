package com.crewly.logbook

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.crewly.account.Account
import com.crewly.account.AccountManager
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by Derek on 26/08/2018
 */
class LogbookViewModel @Inject constructor(app: Application,
                                           private val accountManager: AccountManager):
        AndroidViewModel(app) {

    private val disposables = CompositeDisposable()

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

    fun observeAccount(): Flowable<Account> =
            accountManager.observeCurrentAccount()
}