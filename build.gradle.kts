buildscript {

  repositories {
    google()
    jcenter()
    maven("https://maven.fabric.io/public")
  }

  dependencies {
    classpath(Android.gradleBuildTools)
    classpath(Kotlin.gradlePlugin)
    classpath(Android.googleServices)
    classpath(Fabric.gradle)
  }
}

allprojects {
  repositories {
    google()
    jcenter()
    maven("https://maven.google.com/")
  }
}
