import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.saveourtool.diktat.buildutils.kotlin-jvm-configuration")
    id("com.saveourtool.diktat.buildutils.code-quality-convention")
    id("com.saveourtool.diktat.buildutils.publishing-default-configuration")
}

project.description = "This module builds diktat-api implementation using ktlint"

dependencies {
    api(projects.diktatApi)
    implementation(libs.ktlint.cli.reporter.core)
    implementation(libs.ktlint.rule.engine)
    implementation(libs.ktlint.rule.engine.core)
    implementation(libs.ktlint.cli.reporter.baseline)
    implementation(libs.ktlint.cli.reporter.checkstyle)
    implementation(libs.ktlint.cli.reporter.html)
    implementation(libs.ktlint.cli.reporter.json)
    implementation(libs.ktlint.cli.reporter.plain)
    implementation(libs.ktlint.cli.reporter.sarif)

    testImplementation(libs.log4j2.slf4j2)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.platform.suite)
    testImplementation(libs.assertj.core)
}

val ktlintVersion: String = the<LibrariesForLibs>()
    .versions
    .ktlint
    .get()

val generateKtlintVersionFile by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/src").get().asFile
    val versionsFile = outputDir.resolve("generated/KtLintVersion.kt")

    inputs.property("ktlint version", ktlintVersion)
    outputs.dir(outputDir)

    doFirst {
        versionsFile.parentFile.mkdirs()
        versionsFile.writeText(
            """
            package generated

            const val KTLINT_VERSION = "$ktlintVersion"

            """.trimIndent()
        )
    }
}

kotlin.sourceSets.getByName("main") {
    kotlin.srcDir(
        generateKtlintVersionFile.map {
            it.outputs.files.singleFile
        }
    )
}
