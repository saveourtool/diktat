plugins {
    id("com.saveourtool.diktat.buildutils.kotlin-jvm-configuration")
    id("com.saveourtool.diktat.buildutils.code-quality-convention")
    id("com.saveourtool.diktat.buildutils.publishing-default-configuration")
}

project.description = "Diktat coommon for tests"

dependencies {
    api(projects.diktatCommon)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.apache.commons.cli)
    implementation(libs.apache.commons.io)
    implementation(libs.kotlin.logging)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlin.multiplatform.diff)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
}
