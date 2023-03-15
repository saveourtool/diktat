@Suppress("DSL_SCOPE_VIOLATION", "RUN_IN_SCRIPT")  // https://github.com/gradle/gradle/issues/22797
plugins {
    id("com.saveourtool.save.buildutils.kotlin-jvm-configuration")
//    id("com.saveourtool.save.buildutils.code-quality-convention")
    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlinx.serialization.json.jvm)
    api(libs.kaml)
    implementation(libs.apache.commons.cli)
    implementation(libs.kotlin.logging)
    // ktlint-core is needed only for `initKtLintKLogger` method
    implementation(libs.ktlint.core)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
}
