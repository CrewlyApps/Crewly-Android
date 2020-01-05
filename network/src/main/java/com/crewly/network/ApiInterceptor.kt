package com.crewly.network

import android.content.Context
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ApiInterceptor @Inject constructor(
  private val context: Context
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val headers = Headers.Builder().run {
      add("x-api-key", context.getString(R.string.api_key))
      build()
    }

    val request = chain.request().newBuilder().run {
      headers(headers)
      build()
    }

    return chain.proceed(request)
  }
}