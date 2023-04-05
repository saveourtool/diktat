@Suppress("DSL_SCOPE_VIOLATION", "RUN_IN_SCRIPT")  // https://github.com/gradle/gradle/issues/22797
plugins {
    id("org.cqfn.diktat.buildutils.kotlin-jvm-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
    id("org.cqfn.diktat.buildutils.publishing-signing-default-configuration")
    alias(libs.plugins.kotlin.plugin.serialization)
}

project.description = "This module builds diktat-cli to run diktat as CLI using ktlint"

dependencies {
    implementation(projects.diktatKtlintEngine)
    implementation(projects.diktatRules)
    implementation(libs.kotlinx.cli)
    implementation(libs.log4j2.core)
}
