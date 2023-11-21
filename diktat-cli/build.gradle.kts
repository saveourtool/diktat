import com.saveourtool.diktat.buildutils.configurePublications
import com.github.jengelman.gradle.plugins.shadow.ShadowExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.incremental.createDirectory

@Suppress("DSL_SCOPE_VIOLATION", "RUN_IN_SCRIPT")  // https://github.com/gradle/gradle/issues/22797
plugins {
    id("com.saveourtool.diktat.buildutils.kotlin-jvm-configuration")
    id("com.saveourtool.diktat.buildutils.code-quality-convention")
    id("com.saveourtool.diktat.buildutils.publishing-configuration")
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.shadow)
}

project.description = "This module builds diktat-cli to run diktat as CLI using ktlint"

dependencies {
    implementation(projects.diktatRunner)
    implementation(libs.kotlinx.cli)
    implementation(libs.kotlin.logging)
    implementation(libs.slf4j.api)
    implementation(libs.log4j2.core)
    implementation(libs.log4j2.slf4j2)

    testImplementation(projects.diktatCommonTest)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.platform.suite)
    testImplementation(libs.assertj.core)
}

val addLicenseTask: TaskProvider<Task> = tasks.register("addLicense") {
    val licenseFile = rootProject.file("LICENSE")
    val outputDir = layout.buildDirectory
        .dir("generated/src")
        .get()
        .asFile

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
        attributes["Main-Class"] = "com.saveourtool.diktat.DiktatMainKt"
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
    test {
        dependsOn(shadowJar)
    }
}

publishing {
    publications {
        // it creates a publication for shadowJar
        create<MavenPublication>("shadow") {
            // https://github.com/johnrengelman/shadow/issues/417#issuecomment-830668442
            project.extensions.configure<ShadowExtension> {
                component(this@create)
            }
        }
    }
}
configurePublications()
