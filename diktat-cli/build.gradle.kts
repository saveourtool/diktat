import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.incremental.createDirectory

@Suppress("DSL_SCOPE_VIOLATION", "RUN_IN_SCRIPT")  // https://github.com/gradle/gradle/issues/22797
plugins {
    id("org.cqfn.diktat.buildutils.kotlin-jvm-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
    id("org.cqfn.diktat.buildutils.publishing-signing-default-configuration")
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.shadow)
}

project.description = "This module builds diktat-cli to run diktat as CLI using ktlint"

dependencies {
    implementation(projects.diktatApi)
    implementation(projects.diktatKtlintEngine)
    implementation(projects.diktatRules)
    implementation(libs.kotlinx.cli)
    implementation(libs.log4j2.core)
    implementation(libs.log4j2.slf4j)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.platform.suite)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito)
}

val addLicenseTask = tasks.register("addLicense") {
    val licenseFile = rootProject.file("LICENSE")
    val outputDir = File("$buildDir/generated/src")

    inputs.file(licenseFile)
    outputs.dir(outputDir)

    doLast {
        licenseFile.copyTo(
            outputDir.resolve("META-INF").resolve("diktat")
                .also { it.createDirectory() }
                .resolve(licenseFile.name),
            overwrite = true
        )
    }
}

sourceSets.getByName("main") {
    resources.srcDir(
        addLicenseTask.map {
            it.outputs.files.singleFile
        }
    )
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "org.cqfn.diktat.DiktatMainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.FAIL
}

// disable default jar
tasks.named("jar") {
    enabled = false
}

// it triggers shadowJar with default build
tasks {
    build {
        dependsOn(shadowJar)
    }
}
