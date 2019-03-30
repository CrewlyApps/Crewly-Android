package com.crewly.network

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Derek on 24/07/2018
 */
@Module
class NetworkModule {

  @Singleton
  @Provides
  fun provideMoshi(): Moshi =
    Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()
}