package com.crewly.network

import com.crewly.network.roster.RosterApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Created by Derek on 24/07/2018
 */
@Module
class NetworkModule {

  @Singleton
  @Provides
  fun provideRosterApi(
    retrofit: Retrofit
  ): RosterApi =
    retrofit.create(RosterApi::class.java)

  @Singleton
  @Provides
  fun provideRetrofit(
    okHttpClient: OkHttpClient,
    moshiConverterFactory: MoshiConverterFactory
  ): Retrofit =
    Retrofit.Builder().run {
      client(okHttpClient)
      baseUrl("${ApiPaths.BASE_API_PATH}${ApiPaths.PROD_API_PATH}")
      addConverterFactory(moshiConverterFactory)
      addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      build()
    }

  @Singleton
  @Provides
  fun provideMoshi(): Moshi =
    Moshi.Builder()
      .add(NullToEmptyStringAdapter)
      .add(KotlinJsonAdapterFactory())
      .build()

  @Singleton
  @Provides
  fun provideMoshiConverterFactory(
    moshi: Moshi
  ): MoshiConverterFactory =
    MoshiConverterFactory.create(moshi)

  @Singleton
  @Provides
  fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
    HttpLoggingInterceptor().apply {
      level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

  @Singleton
  @Provides
  fun provideOkHttpClient(
    apiInterceptor: ApiInterceptor,
    httpLoggingInterceptor: HttpLoggingInterceptor
  ): OkHttpClient =
    OkHttpClient.Builder().run {
      connectTimeout(60, TimeUnit.SECONDS)
      readTimeout(60, TimeUnit.SECONDS)
      writeTimeout(60, TimeUnit.SECONDS)
      addInterceptor(apiInterceptor)
      addInterceptor(httpLoggingInterceptor)
      build()
    }
}