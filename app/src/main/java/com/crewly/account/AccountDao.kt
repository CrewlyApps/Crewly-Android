package com.crewly.account

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
  fun fetchAllAccounts(): Flowable<List<Account>>

  @Query("SELECT * FROM accounts WHERE crew_code IS :crewCode")
  fun observeAccount(crewCode: String): Flowable<List<Account>>

  @Query("SELECT * FROM accounts WHERE crew_code IS :crewCode")
  fun fetchAccount(crewCode: String): Single<List<Account>>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertAccount(account: Account): Completable

  @Update
  fun updateAccount(account: Account): Completable
}