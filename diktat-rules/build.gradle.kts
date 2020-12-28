plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":diktat-common"))
    testImplementation(project(":diktat-test-framework"))
}

configureJunit()
