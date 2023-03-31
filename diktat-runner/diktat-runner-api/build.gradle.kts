@Suppress("DSL_SCOPE_VIOLATION", "RUN_IN_SCRIPT")  // https://github.com/gradle/gradle/issues/22797
plugins {
    id("org.cqfn.diktat.buildutils.kotlin-jvm-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
    id("org.cqfn.diktat.buildutils.publishing-signing-default-configuration")
    alias(libs.plugins.kotlin.plugin.serialization)
}

project.description = "This module builds diktat-runner-api"

dependencies {
    implementation(projects.diktatRules)
}
