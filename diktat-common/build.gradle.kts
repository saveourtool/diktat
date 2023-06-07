@Suppress("DSL_SCOPE_VIOLATION", "RUN_IN_SCRIPT")  // https://github.com/gradle/gradle/issues/22797
plugins {
    id("com.saveourtool.diktat.buildutils.kotlin-jvm-configuration")
    id("com.saveourtool.diktat.buildutils.code-quality-convention")
    id("com.saveourtool.diktat.buildutils.publishing-signing-default-configuration")
    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    api(libs.kotlinx.serialization.json)
    api(libs.kaml)
    implementation(libs.apache.commons.cli)
    implementation(libs.kotlin.logging)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
}
