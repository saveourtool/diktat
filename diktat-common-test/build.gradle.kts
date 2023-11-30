plugins {
    id("com.saveourtool.diktat.buildutils.kotlin-jvm-configuration")
    id("com.saveourtool.diktat.buildutils.code-quality-convention")
    id("com.saveourtool.diktat.buildutils.publishing-default-configuration")
}

project.description = "Diktat common for tests"

dependencies {
    implementation(libs.kotlin.logging)
    implementation(libs.junit.jupiter.api)
    implementation(libs.assertj.core)
}
