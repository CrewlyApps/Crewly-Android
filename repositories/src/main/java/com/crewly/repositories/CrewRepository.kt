package com.crewly.repositories

import com.crewly.models.Company
import com.crewly.models.Rank
import com.crewly.models.crew.Crew
import com.crewly.persistence.CrewlyDatabase
import com.crewly.persistence.crew.DbCrew
import io.reactivex.Completable
import io.reactivex.Single
import org.joda.time.DateTime
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
      .map { dbCrew ->
        dbCrew.map { it.toCrew() }
      }

  fun insertOrUpdateCrew(
    crew: List<Crew>
  ): Completable =
    crewlyDatabase
      .crewDao()
      .insertOrUpdateCrew(
        crew = crew.map { it.toDbCrew() }
      )

  fun saveCrew(
    crew: List<DbCrew>
  ): Completable =
    crewlyDatabase.crewDao()
      .insertOrUpdateCrew(crew)

  private fun Crew.toDbCrew(): DbCrew =
    DbCrew(
      id = id,
      name = name,
      companyId = company.id,
      base = base,
      rank = rank.getValue(),
      isPilot = isPilot,
      joinedCompanyAt = joinedCompanyAt.millis,
      lastSeenAt = lastSeenAt.millis,
      showCrew = showCrew
    )

  private fun DbCrew.toCrew(): Crew =
    Crew(
      id = id,
      name = name,
      company = Company.fromId(companyId),
      base = base,
      rank = Rank.fromRank(rank),
      isPilot = isPilot,
      joinedCompanyAt = DateTime(joinedCompanyAt),
      lastSeenAt = DateTime(lastSeenAt),
      showCrew = showCrew
    )
}