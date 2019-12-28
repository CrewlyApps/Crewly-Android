plugins {
  id("com.android.application")
  id("io.fabric")
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
      isMinifyEnabled = true
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
  implementation(Firebase.core)

  // Networking
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

  implementation(Fabric.crashlytics)
  implementation(Utils.jodaTime)
  implementation(Aws.dbMapper)
  implementation(Aws.mobileClient)

  // Unit Testing
  testImplementation(Testing.junit)
  testImplementation(Testing.mockito)
  androidTestImplementation(Testing.testRunner)
  androidTestImplementation(Testing.espresso)
}

apply(mapOf("plugin" to "com.google.gms.google-services"))
