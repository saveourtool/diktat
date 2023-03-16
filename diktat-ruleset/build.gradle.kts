import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("org.cqfn.diktat.buildutils.kotlin-jvm-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    `maven-publish`
}

project.description = "This module builds jar that can be used to run diktat using ktlint -R via command line"

dependencies {
    api(projects.diktatRules) {
        // Kotlin runtime & libraries will be provided by ktlint executable
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("org.jetbrains.kotlin", "kotlin-compiler-embeddable")
        // A new module, better to exclude
        exclude("org.cqfn.diktat", "diktat-dev-ksp")
    }
    testImplementation(projects.diktatTestFramework)
    testImplementation(libs.kotlin.stdlib.common)
    testImplementation(libs.kotlin.stdlib.jdk7)
    testImplementation(libs.kotlin.stdlib.jdk8)
    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.compiler.embeddable)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.platform.suite)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("diktat")
    archiveClassifier.set("")
}

publishing {
    publications {
        create<MavenPublication>("shadowJar").apply {
            project.shadow.component(this)
        }
    }
}
