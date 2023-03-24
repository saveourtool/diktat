plugins {
    id("org.cqfn.diktat.buildutils.kotlin-jvm-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
}

project.description = "Test framework for diktat"

dependencies {
    api(projects.diktatCommon)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.apache.commons.cli)
    implementation(libs.apache.commons.io)
    implementation(libs.kotlin.logging)
    implementation(libs.kotlin.multiplatform.diff)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
}