package com.crewly.persistence.account

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by Derek on 16/06/2018
 */
@Dao
interface AccountDao {

  @Query("SELECT * FROM accounts")
  fun observeAllAccounts(): Flowable<List<DbAccount>>

  @Query("SELECT * FROM accounts WHERE crewCode IS :crewCode")
  fun observeAccount(crewCode: String): Flowable<List<DbAccount>>

  @Query("SELECT * FROM accounts WHERE crewCode IS :crewCode")
  fun fetchAccount(crewCode: String): Single<List<DbAccount>>

  @Query("SELECT * FROM accounts WHERE crewCode IN (:crewCodes)")
  fun fetchAccounts(crewCodes: List<String>): Single<List<DbAccount>>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertAccount(account: DbAccount): Completable

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertOrReplaceAccount(account: DbAccount): Completable

  @Update
  fun updateAccount(account: DbAccount): Completable
}