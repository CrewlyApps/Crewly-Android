package com.crewly.account

import android.annotation.SuppressLint
import com.crewly.app.CrewlyDatabase
import com.crewly.app.RxModule
import com.crewly.aws.AwsRepository
import com.crewly.logging.LoggingManager
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by Derek on 06/05/2019
 */
@Singleton
@SuppressLint("CheckResult")
class AccountRepository @Inject constructor(
  private val crewlyDatabase: CrewlyDatabase,
  private val awsRepository: AwsRepository,
  private val loggingManager: LoggingManager,
  @Named(RxModule.IO_THREAD) private val ioThread: Scheduler
) {

  fun createAccount(
    account: Account
  ): Completable =
    crewlyDatabase.accountDao()
      .insertAccount(account)

  fun updateAccount(
    account: Account
  ): Completable =
    crewlyDatabase.accountDao()
      .updateAccount(account)
      .doOnSubscribe { updateAwsAccount(account) }

  fun getAccount(
    id: String
  ): Single<Account> =
    crewlyDatabase.accountDao()
      .fetchAccount(id)
      .map { accounts -> if (accounts.isNotEmpty()) accounts[0] else Account(id) }

  private fun updateAwsAccount(
    account: Account
  ) {
    awsRepository
      .createOrUpdateUser(account)
      .subscribeOn(ioThread)
      .subscribe({}, { error ->
        loggingManager.logError(error)
      })
  }
}