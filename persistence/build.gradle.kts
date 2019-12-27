plugins {
  id("com.android.library")
  id("kotlin-android")
  id("kotlin-android-extensions")
  id("kotlin-kapt")
}

android {
  compileSdkVersion(Build.compileVersion)


  defaultConfig {
    minSdkVersion(Build.minVersion)
    targetSdkVersion(Build.targetVersion)

    multiDexEnabled = true
    vectorDrawables.useSupportLibrary = true
    resConfig("en")

    kapt {
      arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
      }
    }
  }
}

dependencies {

  implementation(project(":models"))
  implementation(project(":utils"))

  implementation(Android.security)

  implementation(Kotlin.standard)

  implementation(DI.dagger)
  implementation(DI.daggerSupport)
  kapt(DI.daggerCompiler)
  kapt(DI.daggerProcessor)

  implementation(Android.room)
  implementation(Android.roomRx)
  kapt(Android.roomCompiler)

  implementation(Networking.moshi)
  implementation(Networking.moshiKotlin)

  implementation(Utils.jodaTime)
}
