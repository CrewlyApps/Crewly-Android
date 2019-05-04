package com.crewly.aws

import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Derek on 28/04/2019
 */
class AwsRepository @Inject constructor(
  private val awsManager: AwsManager
) {

  fun getUser(
    userId: String,
    companyId: Int
  ): Single<AwsUser> =
    awsManager
      .getDynamoDbMapper()
      .map { mapper -> mapper.load(AwsUser::class.java, userId, companyId) }

  fun createOrUpdateUser(
    user: AwsUser
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .doOnSuccess { mapper -> mapper.save(user) }
      .ignoreElement()

  fun deleteUser(
    userId: String,
    companyId: Int
  ): Completable =
    awsManager
      .getDynamoDbMapper()
      .doOnSuccess { mapper ->
        mapper.delete(
          AwsUser().apply {
            id = userId
            this.companyId = companyId
          }
        )}
      .ignoreElement()
}