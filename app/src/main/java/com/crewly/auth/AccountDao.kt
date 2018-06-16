package com.crewly.auth

import android.arch.persistence.room.*
import io.reactivex.Flowable

/**
 * Created by Derek on 16/06/2018
 */
@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE crew_code IS :crewCode")
    fun fetchAccount(crewCode: String): Flowable<Account>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAccount(account: Account)

    @Update
    fun updateAccount(account: Account)
}