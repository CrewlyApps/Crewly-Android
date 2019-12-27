package com.crewly.account

import com.crewly.app.CrewlyEncryptedPreferences
import com.crewly.app.CrewlyPreferences
import com.crewly.persistence.CrewlyDatabase
import com.crewly.persistence.account.Account
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Derek on 06/05/2019
 */
class AccountRepository @Inject constructor(
  private val crewlyDatabase: CrewlyDatabase,
  private val crewlyPreferences: CrewlyPreferences,
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

  fun saveCurrentCrewCode(
    crewCode: String
  ): Completable =
    Completable.fromCallable {
      crewlyPreferences.saveCurrentAccount(
        crewCode = crewCode
      )
    }

  fun getCurrencyCrewCode(): Single<String> =
    Single.fromCallable {
      crewlyPreferences.getCurrentAccount()
    }

  fun clearCurrentCrewCode(): Completable =
    Completable.fromCallable {
      crewlyPreferences.clearAccount()
    }

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

  fun observeAccount(
    crewCode: String
  ): Flowable<Account> =
    crewlyDatabase.accountDao()
      .observeAccount(
        crewCode = crewCode
      )
      .map { accounts -> if (accounts.isNotEmpty()) accounts[0] else Account() }

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

  fun clearPassword(
    crewCode: String
  ): Completable =
    Completable.fromCallable {
      crewlyEncryptedPreferences.clearPassword(
        crewCode = crewCode
      )
    }
}