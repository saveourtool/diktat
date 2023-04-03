import de.benediktritter.maven.plugin.development.task.GenerateHelpMojoSourcesTask
import de.benediktritter.maven.plugin.development.task.GenerateMavenPluginDescriptorTask

plugins {
    id("org.cqfn.diktat.buildutils.kotlin-jvm-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
    id("org.cqfn.diktat.buildutils.publishing-configuration")
    id("de.benediktritter.maven-plugin-development") version "0.4.1"
    `maven-publish`
}

dependencies {
    implementation(libs.maven.plugin.api)
    compileOnly(libs.maven.plugin.annotations)
    compileOnly(libs.maven.core)

    implementation(libs.kotlin.stdlib.jdk8)
    implementation(projects.diktatRules)
    implementation(projects.diktatKtlintEngine)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.vintage.engine)
    testImplementation(libs.junit.jupiter.extension.itf)
    testImplementation(libs.maven.plugin.testing.harness)
    // to use org.apache.maven.repository.RepositorySystem in newer maven versions and maybe other classes
    testImplementation(libs.maven.compat)
    testImplementation(libs.assertj.core)
    testImplementation(libs.plexus.cipher)
}

tasks.withType<GenerateMavenPluginDescriptorTask> {
    notCompatibleWithConfigurationCache("https://github.com/britter/maven-plugin-development/issues/8")
}

tasks.withType<GenerateHelpMojoSourcesTask> {
    notCompatibleWithConfigurationCache("https://github.com/britter/maven-plugin-development/issues/8")
}

mavenPlugin {
    goalPrefix.set("diktat")
}

publishing {
    publications {
        create<MavenPublication>("mavenPlugin") {
            from(components["java"])
        }
    }
}
