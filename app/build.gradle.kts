plugins {
  id("com.android.application")
  id("com.google.firebase.crashlytics")
  id("com.google.gms.google-services") apply false
  id("kotlin-android")
  id("kotlin-android-extensions")
  id("kotlin-kapt")
}

android {
  compileSdkVersion(Build.compileVersion)

  defaultConfig {
    minSdkVersion(Build.minVersion)
    targetSdkVersion(Build.targetVersion)

    applicationId = Build.appId
    versionCode = Build.versionCode
    versionName = Build.versionName

    multiDexEnabled = true
    vectorDrawables.useSupportLibrary = true
    resConfig("en")

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      //storeFile = file("")
      storePassword = ""
      keyAlias = ""
      keyPassword = ""
    }
  }

  buildTypes {

    getByName("release") {
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("release")
      proguardFile(getDefaultProguardFile("proguard-android.txt"))
      proguardFile("proguard-rules.pro")
    }

    getByName("debug") {
      isMinifyEnabled = false
    }
  }

  packagingOptions {
    exclude("META-INF/rxjava.properties")
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_1_8.toString()
  }
}

dependencies {

  implementation(project(":repositories"))
  implementation(project(":persistence"))
  implementation(project(":network"))
  implementation(project(":models"))
  implementation(project(":utils"))

  // Android
  implementation(Android.core)
  implementation(Android.appCompat)
  implementation(Android.constraintLayout)
  implementation(Android.lifecycle)
  implementation(Android.lifecycleExtensions)
  implementation(Android.recyclerView)
  implementation(Android.material)
  implementation(Android.room)
  implementation(Android.roomRx)
  implementation(Android.security)
  kapt(Android.roomCompiler)

  // Firebase
  implementation(Firebase.analytics)
  implementation(Firebase.core)
  implementation(Firebase.crashlytics)

  // Networking
  implementation(Networking.retrofit)
  implementation(Networking.retrofitRxJavaAdapter)
  implementation(Networking.retrofitMoshiConverter)
  implementation(Networking.okHttpLogging)
  implementation(Networking.moshi)
  implementation(Networking.moshiKotlin)

  implementation(Kotlin.standard)

  implementation(DI.dagger)
  implementation(DI.daggerSupport)
  kapt(DI.daggerCompiler)
  kapt(DI.daggerProcessor)

  implementation(Rx.android)
  implementation(Rx.java)
  implementation(Rx.binding)

  implementation(Utils.jodaTime)
  implementation(Utils.timber)

  debugImplementation(Flipper.flipper)
  debugImplementation(Flipper.soLoader)
  releaseImplementation(Flipper.flipper)

  implementation(Image.GLIDE)
  kapt(Image.GLIDE_COMPILER)
  implementation(Image.photoView)

  // Unit Testing
  testImplementation(Testing.junit)
  testImplementation(Testing.mockito)
  androidTestImplementation(Testing.testRunner)
  androidTestImplementation(Testing.espresso)
}

apply(mapOf("plugin" to "com.google.gms.google-services"))
