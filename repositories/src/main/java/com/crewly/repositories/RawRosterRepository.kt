package com.crewly.repositories

import com.crewly.models.file.FileData
import com.crewly.persistence.CrewlyDatabase
import com.crewly.persistence.FileWriter
import com.crewly.persistence.roster.DbRawRoster
import io.reactivex.Completable
import javax.inject.Inject

class RawRosterRepository @Inject constructor(
  private val crewlyDatabase: CrewlyDatabase,
  private val fileWriter: FileWriter
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
      fileWriter.writeFile(
        data = rosterData
      )
    )
}