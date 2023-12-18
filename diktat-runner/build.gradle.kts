import com.saveourtool.diktat.buildutils.configurePublications
import com.saveourtool.diktat.buildutils.configurePublishing

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
    implementation(projects.diktatKtlintEngine)
    implementation(projects.diktatRules)
}

tasks.shadowJar {
    archiveClassifier.set("shadow")
    duplicatesStrategy = DuplicatesStrategy.FAIL
}

// https://github.com/gradle/gradle/issues/10384#issuecomment-1279708395
val shadowElement: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
    }
    outgoing.artifact(tasks.shadowJar)
}
components.named<AdhocComponentWithVariants>("java").configure {
    addVariantsFromConfiguration(shadowElement) {}
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
configurePublications()
configurePublishing()
