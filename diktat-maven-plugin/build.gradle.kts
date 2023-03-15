plugins {
    id("com.saveourtool.save.buildutils.kotlin-jvm-configuration")
//    id("com.saveourtool.save.buildutils.code-quality-convention")
    id("de.benediktritter.maven-plugin-development") version "0.4.1"
}

dependencies {
    implementation(libs.maven.plugin.api)
    compileOnly(libs.maven.plugin.annotations)
    implementation(libs.maven.core)

    implementation(libs.kotlin.stdlib.jdk8)
    implementation(projects.diktatRules)
    implementation(libs.ktlint.core)
    implementation(libs.ktlint.reporter.plain)
    implementation(libs.ktlint.reporter.sarif)
    implementation(libs.ktlint.reporter.json)
    implementation(libs.ktlint.reporter.html)
    implementation(libs.ktlint.reporter.baseline)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.extension.itf)
    testImplementation(libs.maven.plugin.testing.harness)
    // to use org.apache.maven.repository.RepositorySystem in newer maven versions and maybe other classes
    testImplementation(libs.maven.compat)
    testImplementation(libs.assertj.core)
    testImplementation(libs.plexus.cipher)
}

mavenPlugin {
    helpMojoPackage.set("org.cqfn.diktat.plugin.maven")
}
