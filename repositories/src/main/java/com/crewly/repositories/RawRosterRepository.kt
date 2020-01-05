package com.crewly.repositories

import com.crewly.models.file.FileData
import com.crewly.models.roster.RawRoster
import com.crewly.persistence.CrewlyDatabase
import com.crewly.persistence.RawRosterFileHelper
import com.crewly.persistence.roster.DbRawRoster
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class RawRosterRepository @Inject constructor(
  private val crewlyDatabase: CrewlyDatabase,
  private val rawRosterFileHelper: RawRosterFileHelper
) {

  fun saveRawRoster(
    rawRoster: DbRawRoster,
    rosterData: FileData
  ): Completable =
    Completable.mergeArray(
      crewlyDatabase.rawRosterDao()
        .insertRawRoster(
          rawRoster = rawRoster
        ),
      rawRosterFileHelper.writeFile(
        data = rosterData
      )
    )

  fun getRawRoster(
    ownerId: String
  ): Single<RawRoster> =
    crewlyDatabase.rawRosterDao()
      .getRawRoster(
        ownerId = ownerId
      )
      .map { it.toRawRoster() }

  private fun DbRawRoster.toRawRoster() =
    RawRoster(
      filePath = filePath
    )
}