package com.crewly.network

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ApiInterceptor @Inject constructor() : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val headers = Headers.Builder().run {
      add("", "")
      build()
    }

    val request = chain.request().newBuilder().run {
      headers(headers)
      build()
    }

    return chain.proceed(request)
  }
}