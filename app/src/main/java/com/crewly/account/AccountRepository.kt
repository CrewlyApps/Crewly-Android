package com.crewly.account

import com.crewly.app.CrewlyDatabase
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Derek on 06/05/2019
 */
class AccountRepository @Inject constructor(
  private val crewlyDatabase: CrewlyDatabase
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

  fun getAccount(
    id: String
  ): Single<Account> =
    crewlyDatabase.accountDao()
      .fetchAccount(id)
      .map { accounts -> if (accounts.isNotEmpty()) accounts[0] else Account(id) }
}