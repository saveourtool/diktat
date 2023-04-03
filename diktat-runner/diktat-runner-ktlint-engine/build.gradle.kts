plugins {
    id("org.cqfn.diktat.buildutils.kotlin-jvm-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
    id("org.cqfn.diktat.buildutils.publishing-signing-default-configuration")
}

project.description = "This module builds diktat-runner implementation using ktlint"

dependencies {
    api(projects.diktatRunner.diktatRunnerApi)
    implementation(projects.diktatCommon)
    implementation(libs.ktlint.core)
    implementation(libs.ktlint.reporter.baseline)
    implementation(libs.ktlint.reporter.checkstyle)
    implementation(libs.ktlint.reporter.html)
    implementation(libs.ktlint.reporter.json)
    implementation(libs.ktlint.reporter.plain)
    implementation(libs.ktlint.reporter.sarif)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.platform.suite)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito)
}
