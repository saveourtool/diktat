plugins {
    id("com.saveourtool.save.buildutils.kotlin-jvm-configuration")
//    id("com.saveourtool.save.buildutils.code-quality-convention")
}

dependencies {
    implementation(projects.diktatCommon)
    testImplementation(projects.diktatTestFramework)
    implementation(libs.ktlint.core)
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
}
