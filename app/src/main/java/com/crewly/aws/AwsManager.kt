package com.crewly.aws

import android.app.Application
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.crewly.app.RxModule
import com.crewly.logging.LoggingFlow
import com.crewly.logging.LoggingManager
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by Derek on 04/05/2019
 */
@Singleton
class AwsManager @Inject constructor(
  private val app: Application,
  private val loggingManager: LoggingManager,
  @Named(RxModule.IO_THREAD) private val ioThread: Scheduler
) {

  private val dynamoDbMapper = BehaviorSubject.create<DynamoDBMapper>()
  private var fetchDynamoDbMapperSubscription: Disposable? = null

  fun init() {
    AWSMobileClient.getInstance().initialize(app, object: Callback<UserStateDetails> {
      override fun onResult(result: UserStateDetails?) {
        fetchDynamoDbMapperIfNeeded()

        loggingManager.logMessage(
          loggingFlow = LoggingFlow.AWS,
          message = result?.userState?.name.toString()
        )
      }

      override fun onError(exc: Exception?) {
        loggingManager.logError(
          exception = exc ?: Exception("AwsManager init error")
        )
      }
    })
  }

  fun getDynamoDbMapper(): Single<DynamoDBMapper> {
    fetchDynamoDbMapperIfNeeded()

    return dynamoDbMapper
      .take(1)
      .singleOrError()
  }

  private fun fetchDynamoDbMapperIfNeeded() {
    if (!dynamoDbMapper.hasValue() && fetchDynamoDbMapperSubscription == null) {
      fetchDynamoDbMapperSubscription = Single.fromCallable {
        val credentials = CognitoCachingCredentialsProvider(
          app,
          "eu-west-1:b4c4af9a-d8fc-4646-bab0-9c86b2784ffe",
          Regions.EU_WEST_1
        )

        DynamoDBMapper.builder()
          .dynamoDBClient(
            AmazonDynamoDBClient(credentials).apply {
              setRegion(Region.getRegion(Regions.EU_WEST_1))
            }
          )
          .build()
      }
        .subscribeOn(ioThread)
        .doOnEvent { _, _ -> fetchDynamoDbMapperSubscription = null  }
        .subscribe({ dynamoDbMapper ->
          this.dynamoDbMapper.onNext(dynamoDbMapper)
        }, { error ->
          loggingManager.logError(
            throwable = error
          )
        })
    }
  }
}