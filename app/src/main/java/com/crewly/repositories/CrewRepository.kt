package com.crewly.repositories

import com.crewly.db.CrewlyDatabase
import com.crewly.db.crew.Crew
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Derek on 18/05/2019
 */
class CrewRepository @Inject constructor(
  private val crewlyDatabase: CrewlyDatabase
) {

  fun getCrew(
    ids: List<String>
  ): Single<List<Crew>> =
    crewlyDatabase
      .crewDao()
      .fetchCrew(
        ids = ids
      )

  fun insertOrUpdateCrew(
    crew: List<Crew>
  ): Completable =
    crewlyDatabase
      .crewDao()
      .insertOrUpdateCrew(
        crew = crew
      )
}