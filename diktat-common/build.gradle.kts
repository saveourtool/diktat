plugins {
    id("com.saveourtool.save.buildutils.kotlin-jvm-configuration")
//    id("com.saveourtool.save.buildutils.code-quality-convention")
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlinx.serialization.json.jvm)
    implementation(libs.kaml)
    implementation(libs.apache.commons.cli)
    implementation(libs.kotlin.logging)
    // ktlint-core is needed only for `initKtLintKLogger` method
    implementation(libs.ktlint.core)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
}
