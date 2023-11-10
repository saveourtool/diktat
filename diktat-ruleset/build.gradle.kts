import com.saveourtool.diktat.buildutils.configurePom
import com.saveourtool.diktat.buildutils.configurePublications
import com.github.jengelman.gradle.plugins.shadow.ShadowExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

@Suppress("DSL_SCOPE_VIOLATION", "RUN_IN_SCRIPT")  // https://github.com/gradle/gradle/issues/22797
plugins {
    id("com.saveourtool.diktat.buildutils.kotlin-jvm-configuration")
    id("com.saveourtool.diktat.buildutils.code-quality-convention")
    id("com.saveourtool.diktat.buildutils.publishing-configuration")
    alias(libs.plugins.shadow)
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
    }
    implementation(projects.diktatKtlintEngine) {
        // Kotlin runtime & libraries will be provided by ktlint executable
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("org.jetbrains.kotlin", "kotlin-compiler-embeddable")
    }
    implementation(libs.ktlint.cli.ruleset.core)
    implementation(libs.ktlint.logger)
    implementation(libs.slf4j.api)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("diktat")
    archiveClassifier.set("")
    // need to relocate serialization from kaml to avoid conflicts with KtLint
    relocate("kotlinx.serialization", "com.saveourtool.kotlinx_serialization")
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
            this.artifactId = "diktat"
            this.pom {
                configurePom(project)
                // need to override name
                name.set("diktat")
            }
        }
    }
}
configurePublications()
