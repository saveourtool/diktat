plugins {
    id("org.cqfn.diktat.buildutils.kotlin-jvm-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
    id("org.cqfn.diktat.buildutils.publishing-signing-default-configuration")
}

project.description = "This module builds diktat-runner implementation using ktlint as CLI"

dependencies {
    api(projects.diktatRunner.diktatRunnerApi)
    implementation(projects.diktatRules)
    implementation(projects.diktatRunner.diktatRunnerKtlintEngine)
    implementation(libs.kotlinx.cli)
    implementation(libs.log4j2.core)
    implementation(libs.ktlint.reporter.baseline)
    implementation(libs.ktlint.reporter.checkstyle)
    implementation(libs.ktlint.reporter.html)
    implementation(libs.ktlint.reporter.json)
    implementation(libs.ktlint.reporter.plain)
    implementation(libs.ktlint.reporter.sarif)
}
