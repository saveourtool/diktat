@Suppress("DSL_SCOPE_VIOLATION", "RUN_IN_SCRIPT")  // https://github.com/gradle/gradle/issues/22797
plugins {
    id("com.saveourtool.diktat.buildutils.kotlin-jvm-configuration")
    id("com.saveourtool.diktat.buildutils.code-quality-convention")
    id("com.saveourtool.diktat.buildutils.publishing-default-configuration")
}

project.description = "This module contains runner for diktat"

dependencies {
    api(projects.diktatApi)
    implementation(projects.diktatKtlintEngine)
    implementation(projects.diktatRules)
}
