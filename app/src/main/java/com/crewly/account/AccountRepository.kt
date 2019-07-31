package com.crewly.account

import com.crewly.app.CrewlyEncryptedPreferences
import com.crewly.db.CrewlyDatabase
import com.crewly.db.account.Account
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Derek on 06/05/2019
 */
class AccountRepository @Inject constructor(
  private val crewlyDatabase: CrewlyDatabase,
  private val crewlyEncryptedPreferences: CrewlyEncryptedPreferences
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

  fun getAccounts(
    ids: List<String>
  ): Single<List<Account>> =
    crewlyDatabase
      .accountDao()
      .fetchAccounts(
        crewCodes = ids
      )

  fun savePassword(
    crewCode: String,
    password: String
  ): Completable =
    Completable.fromCallable {
      crewlyEncryptedPreferences.savePassword(
        crewCode = crewCode,
        password = password
      )
    }

  fun getPassword(
    crewCode: String
  ): Single<String> =
    Single.fromCallable {
      crewlyEncryptedPreferences.getPassword(
        crewCode = crewCode
      )
    }
}