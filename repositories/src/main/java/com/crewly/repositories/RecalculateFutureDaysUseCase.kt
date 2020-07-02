package com.crewly.repositories

import com.crewly.models.account.CrewType
import com.crewly.models.roster.future.FutureDay
import com.crewly.persistence.duty.DbDuty
import io.reactivex.Completable
import javax.inject.Inject

class RecalculateFutureDaysUseCase @Inject constructor(
  accountRepository: AccountRepository,
  private val dutiesRepository: DutiesRepository,
  private val rosterRepository: RosterRepository
) {

  private val futureDaysCalculator = RosterFutureDaysCalculator(
    accountRepository = accountRepository
  )

  fun recalculateFutureDays(
    username: String,
    companyId: Int,
    crewType: CrewType
  ): Completable =
    rosterRepository.readDutiesPriorToRoster(
      username = username,
      rosterDays = emptyList()
    )
      .flatMap { eventTypesByDate ->
        futureDaysCalculator.generateFutureRosterDays(
          crewType = crewType,
          eventTypesByDate = eventTypesByDate
        )
      }
      .map { futureDays ->
        futureDays.map {
          it.toDbDuty(
            ownerId = username,
            companyId = companyId
          )
        }
      }
      .flatMapCompletable { dbDuties ->
        //TODO - save + retrieve last roster time
        val rosterStartTime = 0L
        if (rosterStartTime > 0) {
          dutiesRepository.deleteDutiesFrom(
            ownerId = username,
            time = rosterStartTime
          )
            .andThen(
              dutiesRepository.saveDuties(
                duties = dbDuties
              )
            )
        } else {
          Completable.complete()
        }
      }

  private fun FutureDay.toDbDuty(
    ownerId: String,
    companyId: Int
  ): DbDuty =
    DbDuty(
      ownerId = ownerId,
      companyId = companyId,
      type = type.name,
      code = type.code,
      startTime = date.millis,
      endTime = date.millis,
      from = "",
      to = "",
      phoneNumber = ""
    )
}