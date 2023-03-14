plugins {
    id("com.saveourtool.save.buildutils.kotlin-jvm-configuration")
//    id("com.saveourtool.save.buildutils.code-quality-convention")
}

dependencies {
    implementation(projects.diktatCommon)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.apache.commons.cli)
    implementation(libs.apache.commons.io)
    implementation(libs.kotlin.logging)
    implementation(libs.kotlin.multiplatform.diff)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)

    // FIXME: it should come as transitive dependency from projects.diktatCommon
//    implementation(libs.kotlinx.serialization.json)
}
