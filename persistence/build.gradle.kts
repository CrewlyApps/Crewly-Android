plugins {
  id("com.android.library")
  id("kotlin-android")
  id("kotlin-android-extensions")
}

android {
  compileSdkVersion(Build.compileVersion)


  defaultConfig {
    minSdkVersion(Build.minVersion)
    targetSdkVersion(Build.targetVersion)

    multiDexEnabled = true
    vectorDrawables.useSupportLibrary = true
    resConfig("en")
  }

}

dependencies {

  implementation(project(":models"))
  implementation(project(":utils"))

  implementation(Kotlin.standard)

  implementation(Android.room)
  implementation(Android.roomRx)

  implementation(Networking.moshi)
  implementation(Networking.moshiKotlin)

  implementation(Utils.jodaTime)
}
