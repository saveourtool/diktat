import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("org.cqfn.diktat.buildutils.kotlin-jvm-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
    id("org.cqfn.diktat.buildutils.publishing-signing-default-configuration")
}

project.description = "This module builds diktat-api implementation using ktlint"

dependencies {
    api(projects.diktatApi)
    implementation(projects.diktatCommon)
    // a temporary solution to avoid a lot of changes in diktat-rules
    api(libs.ktlint.core)
    implementation(libs.ktlint.reporter.baseline)
    implementation(libs.ktlint.reporter.checkstyle)
    implementation(libs.ktlint.reporter.html)
    implementation(libs.ktlint.reporter.json)
    implementation(libs.ktlint.reporter.plain)
    implementation(libs.ktlint.reporter.sarif)

    testImplementation(libs.log4j2.slf4j2)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.platform.suite)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito)
}

val ktlintVersion: String = the<LibrariesForLibs>()
    .versions
    .ktlint
    .get()

val generateKtlintVersionFile by tasks.registering {
    val outputDir = File("$buildDir/generated/src")
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
