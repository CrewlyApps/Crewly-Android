package com.crewly.persistence.crew

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crewly.persistence.crew.Crew
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by Derek on 18/05/2019
 */
@Dao
interface CrewDao {

  @Query("SELECT * FROM crew WHERE id IN (:ids)")
  fun fetchCrew(ids: List<String>): Single<List<Crew>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertOrUpdateCrew(crew: List<Crew>): Completable
}