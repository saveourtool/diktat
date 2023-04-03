plugins {
    id("org.cqfn.diktat.buildutils.kotlin-jvm-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
    id("org.cqfn.diktat.buildutils.publishing-signing-default-configuration")
    id("com.google.devtools.ksp") version "1.8.10-1.0.9"
    idea
}

project.description = "The main diktat ruleset"

dependencies {
    api(projects.diktatCommon)
    api(projects.diktatRunner.diktatRunnerApi)
    implementation(projects.diktatRunner.diktatRunnerKtlintEngine)
    testImplementation(projects.diktatTestFramework)
    api(libs.ktlint.core)
    implementation(libs.kotlin.stdlib.jdk8)
    // guava is used for string case utils
    implementation(libs.guava)
    implementation(libs.kotlin.logging)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.platform.suite)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito)
    // is used for simplifying boolean expressions
    implementation(libs.jbool.expressions)

    // generating
    compileOnly(projects.diktatDevKsp)
    ksp(projects.diktatDevKsp)
    testImplementation(libs.kotlin.reflect)
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

idea {
    module {
        // Not using += due to https://github.com/gradle/gradle/issues/8749
        sourceDirs = sourceDirs + file("build/generated/ksp/main/kotlin")  // or tasks["kspKotlin"].destination
        testSourceDirs = testSourceDirs + file("build/generated/ksp/test/kotlin")
        generatedSourceDirs = generatedSourceDirs + file("build/generated/ksp/main/kotlin") + file("build/generated/ksp/test/kotlin")
    }
}
