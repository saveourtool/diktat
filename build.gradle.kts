plugins {
    kotlin("jvm") version Versions.Kotlin apply false
    kotlin("plugin.serialization") version Versions.Kotlin apply false
}

allprojects {
    repositories {
        jcenter()
    }
    configureDiktat()
}
