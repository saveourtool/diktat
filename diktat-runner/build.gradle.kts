import com.saveourtool.diktat.buildutils.configurePublications
import com.saveourtool.diktat.buildutils.configurePublishing
import com.github.jengelman.gradle.plugins.shadow.ShadowExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

@Suppress("DSL_SCOPE_VIOLATION", "RUN_IN_SCRIPT")  // https://github.com/gradle/gradle/issues/22797
plugins {
    id("com.saveourtool.diktat.buildutils.kotlin-jvm-configuration")
    id("com.saveourtool.diktat.buildutils.code-quality-convention")
    id("com.saveourtool.diktat.buildutils.publishing-configuration")
    alias(libs.plugins.shadow)
}

project.description = "This module contains runner for diktat"

dependencies {
    api(projects.diktatApi)
    api(projects.diktatCommon)
    implementation(projects.diktatKtlintEngine)
    implementation(projects.diktatRules)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("shadow")
    duplicatesStrategy = DuplicatesStrategy.FAIL
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
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
configurePublishing()
