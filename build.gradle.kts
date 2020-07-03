buildscript {

  repositories {
    google()
    jcenter()
  }

  dependencies {
    classpath(Android.gradleBuildTools)
    classpath(Kotlin.gradlePlugin)
    classpath(Android.googleServices)
    classpath(Firebase.crashlyticsGradle)
  }
}

allprojects {
  repositories {
    google()
    jcenter()
    maven("https://maven.google.com/")
    maven("https://jitpack.io")
  }
}
