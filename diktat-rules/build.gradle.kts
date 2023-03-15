plugins {
    id("com.saveourtool.save.buildutils.kotlin-jvm-configuration")
//    id("com.saveourtool.save.buildutils.code-quality-convention")
    id("com.google.devtools.ksp") version "1.8.0-1.0.8"
}

dependencies {
    api(projects.diktatCommon)
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
    implementation(projects.diktatDevKsp)
    ksp(projects.diktatDevKsp)
}

ksp {
    arg("enumName", "org.cqfn.diktat.ruleset.constants.Warnings")
}
