import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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
    implementation(projects.diktatKtlintEngine)
    implementation(projects.diktatRules)
    implementation(libs.kotlinx.cli)
    implementation(libs.log4j2.core)
    implementation(libs.log4j2.slf4j)
}

val addLicenseTask = tasks.register<Copy>("addLicense") {
    val sourceLicenseFile = rootProject.file("LICENSE")
    val outputDir = File("$buildDir/generated/src")
    val targetDir = outputDir.resolve("META-INF/diktat")

    inputs.file(sourceLicenseFile)
    outputs.dir(outputDir)

    copy {
        from(rootProject.path)
        include(sourceLicenseFile.name)
        into(targetDir)
    }
}

kotlin.sourceSets.getByName("main") {
    kotlin.srcDir(
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
}

// disable default jar
tasks.named("jar") {
    enabled = false
}
