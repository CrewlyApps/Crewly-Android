/**
 * Created by Derek on 06/04/2019
 */

object Android {
  private const val androidCoreVersion = "1.1.0"
  private const val appCompatVersion = "1.1.0"
  private const val constraintLayoutVersion = "1.1.3"
  private const val lifecycleVersion = "2.1.0"
  private const val recyclerViewVersion = "1.1.0"
  private const val materialVersion = "1.0.0"
  private const val gradleBuildToolsVersion = "3.5.2"
  private const val googleServicesVersion = "4.3.3"
  private const val roomVersion = "2.2.3"
  private const val securityVersion = "1.0.0-alpha02"

  const val core = "androidx.core:core-ktx:$androidCoreVersion"
  const val appCompat = "androidx.appcompat:appcompat:$appCompatVersion"
  const val constraintLayout = "androidx.constraintlayout:constraintlayout:$constraintLayoutVersion"
  const val lifecycle = "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion"
  const val lifecycleExtensions = "androidx.lifecycle:lifecycle-extensions:$lifecycleVersion"
  const val recyclerView = "androidx.recyclerview:recyclerview:$recyclerViewVersion"
  const val material = "com.google.android.material:material:$materialVersion"
  const val gradleBuildTools = "com.android.tools.build:gradle:$gradleBuildToolsVersion"
  const val googleServices = "com.google.gms:google-services:$googleServicesVersion"
  const val room = "androidx.room:room-runtime:$roomVersion"
  const val roomRx = "androidx.room:room-rxjava2:$roomVersion"
  const val roomCompiler = "androidx.room:room-compiler:$roomVersion"
  const val security = "androidx.security:security-crypto:$securityVersion"
}

object DI {
  private const val daggerVersion = "2.25.3"

  const val dagger = "com.google.dagger:dagger:$daggerVersion"
  const val daggerSupport = "com.google.dagger:dagger-android-support:$daggerVersion"
  const val daggerCompiler = "com.google.dagger:dagger-compiler:$daggerVersion"
  const val daggerProcessor = "com.google.dagger:dagger-android-processor:$daggerVersion"
}

object Firebase {
  private const val firebaseCoreVersion = "17.2.1"
  private const val analyticsVersion = "17.4.3"
  private const val crashlyticsGradleVersion = "2.2.0"
  private const val crashlyticsVersion = "17.1.0"

  const val core = "com.google.firebase:firebase-core:$firebaseCoreVersion"
  const val analytics = "com.google.firebase:firebase-analytics:$analyticsVersion"
  const val crashlyticsGradle = "com.google.firebase:firebase-crashlytics-gradle:$crashlyticsGradleVersion"
  const val crashlytics = "com.google.firebase:firebase-crashlytics:$crashlyticsVersion"
}

object Flipper {
  private const val flipperVersion = "0.30.1"
  private const val soLoaderVersion = "0.5.1"

  const val flipper = "com.facebook.flipper:flipper:$flipperVersion"
  const val flipperNoOp = "com.facebook.flipper:flipper-noop:$flipper"
  const val soLoader = "com.facebook.soloader:soloader:$soLoaderVersion"
}

object Image {
  private const val photoViewVersion = "2.1.3"

  const val photoView = "com.github.chrisbanes:PhotoView:$photoViewVersion"
}

object Kotlin {
  private const val kotlinVersion = "1.3.61"

  const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
  const val standard = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion"
}

object Networking {
  private const val retrofitVersion = "2.6.3"
  private const val okHttpVersion = "3.14.4"
  private const val moshiVersion = "1.9.2"

  const val retrofit = "com.squareup.retrofit2:retrofit:$retrofitVersion"
  const val retrofitMoshiConverter = "com.squareup.retrofit2:converter-moshi:$retrofitVersion"
  const val retrofitRxJavaAdapter = "com.squareup.retrofit2:adapter-rxjava2:$retrofitVersion"
  const val okHttp = "com.squareup.okhttp3:okhttp:$okHttpVersion"
  const val okHttpLogging = "com.squareup.okhttp3:logging-interceptor:$okHttpVersion"

  const val moshi = "com.squareup.moshi:moshi:$moshiVersion"
  const val moshiKotlin = "com.squareup.moshi:moshi-kotlin:$moshiVersion"
}

object Rx {
  private const val rxAndroidVersion = "2.1.1"
  private const val rxJavaVersion = "2.2.16"
  private const val rxBindingVersion = "3.1.0"

  const val android = "io.reactivex.rxjava2:rxandroid:$rxAndroidVersion"
  const val java = "io.reactivex.rxjava2:rxjava:$rxJavaVersion"
  const val binding = "com.jakewharton.rxbinding3:rxbinding:$rxBindingVersion"
}

object Testing {
  private const val junitVersion = "4.12"
  private const val mockitoVersion = "2.18.3"
  private const val testRunnerVersion = "1.1.0"
  private const val espressoVersion = "3.1.0"

  const val junit = "junit:junit:$junitVersion"
  const val mockito = "org.mockito:mockito-core:$mockitoVersion"
  const val testRunner = "androidx.test:runner:$testRunnerVersion"
  const val espresso = "androidx.test.espresso:espresso-core:$espressoVersion"
}

object Utils {
  private const val jodaTimeVersion = "2.10.3"
  private const val timberVersion = "4.7.1"

  const val jodaTime = "net.danlew:android.joda:$jodaTimeVersion"
  const val timber = "com.jakewharton.timber:timber:$timberVersion"
}
