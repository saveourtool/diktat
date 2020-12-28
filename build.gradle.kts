plugins {
    kotlin("jvm") version Versions.Kotlin apply false
    id("org.cqfn.diktat.diktat-gradle-plugin") version Versions.Diktat
}

allprojects {
    repositories {
        jcenter()
    }
}

// diktat config is TODO
diktat {
    inputs = files("**/src/main/kotlin/**/*.kt",
        "**/src/test/kotlin/**/*.kt",
        "buildSrc/**/*.kt")
    excludes = files("**/resources/**/*.kt", "**/target/**/*.kt")
}