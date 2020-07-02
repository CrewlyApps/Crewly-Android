package com.crewly.repositories

import com.crewly.models.Company
import com.crewly.models.account.Account
import com.crewly.persistence.preferences.CrewlyEncryptedPreferences
import com.crewly.persistence.preferences.CrewlyPreferences
import com.crewly.persistence.CrewlyDatabase
import com.crewly.persistence.account.DbAccount
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.joda.time.DateTime
import javax.inject.Inject

/**
 * Created by Derek on 06/05/2019
 */
class AccountRepository @Inject constructor(
  private val crewlyDatabase: CrewlyDatabase,
  private val crewlyPreferences: CrewlyPreferences,
  private val crewlyEncryptedPreferences: CrewlyEncryptedPreferences
) {

  fun createOrReplaceAccount(
    account: Account
  ): Completable =
    crewlyDatabase.accountDao()
      .insertOrReplaceAccount(account.toDbAccount())

  fun updateAccount(
    account: Account
  ): Completable =
    crewlyDatabase.accountDao()
      .updateAccount(account.toDbAccount())

  fun saveCurrentCrewCode(
    crewCode: String
  ): Completable =
    Completable.fromCallable {
      crewlyPreferences.saveCurrentAccount(
        crewCode = crewCode
      )
    }

  fun getCurrentCrewCode(): Single<String> =
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
      .map { accounts -> if (accounts.isNotEmpty()) accounts[0].toAccount() else Account(id) }

  fun getCurrentAccount(): Single<Account> =
    getCurrentCrewCode()
      .flatMap { crewCode ->
        getAccount(crewCode)
      }

  fun getAccounts(
    ids: List<String>
  ): Single<List<Account>> =
    crewlyDatabase
      .accountDao()
      .fetchAccounts(
        crewCodes = ids
      )
      .map { dbAccounts ->
        dbAccounts.map { dbAccount ->
          dbAccount.toAccount()
        }
      }

  fun observeAccount(
    crewCode: String
  ): Flowable<Account> =
    crewlyDatabase.accountDao()
      .observeAccount(
        crewCode = crewCode
      )
      .map { accounts -> if (accounts.isNotEmpty()) accounts[0].toAccount() else Account() }

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

  private fun Account.toDbAccount(): DbAccount =
    DbAccount(
      crewCode = crewCode,
      name = name,
      companyId = company.id,
      crewType = crewType,
      base = base,
      joinedCompanyAt = joinedCompanyAt.millis,
      updateFlightsRealTimeEnabled = updateFlightsRealTimeEnabled,
      salary = salary,
      futureDaysPattern = futureDaysPattern
    )

  private fun DbAccount.toAccount(): Account =
    Account(
      crewCode = crewCode,
      name = name,
      company = Company.fromId(companyId),
      crewType = crewType,
      base = base,
      joinedCompanyAt = DateTime(joinedCompanyAt),
      updateFlightsRealTimeEnabled = updateFlightsRealTimeEnabled,
      salary = salary,
      futureDaysPattern = futureDaysPattern
    )
}