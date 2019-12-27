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

  implementation(project(":utils"))

  implementation(Kotlin.standard)

  implementation(Utils.jodaTime)
}
