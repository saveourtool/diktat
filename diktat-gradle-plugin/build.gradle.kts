import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.4.10"
}

repositories {
    flatDir {
        // to use snapshot diktat without necessary installing
        dirs("../diktat-rules/target")
    }
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))

    implementation("com.pinterest.ktlint:ktlint-core:0.39.0") {
        exclude("com.pinterest.ktlint", "ktlint-ruleset-standard")
    }
    implementation("com.pinterest.ktlint:ktlint-reporter-plain:0.39.0")
    implementation("org.cqfn.diktat:diktat-rules:$version")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        // fixme: kotlin 1.3 is required for gradle <6.8
        languageVersion = "1.3"
        apiVersion = "1.3"
    }
}

gradlePlugin {
    plugins {
        create("diktatPlugin") {
            id = "org.cqfn.diktat.diktat-gradle-plugin"
            implementationClass = "org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin"
        }
    }
}

java {
    withSourcesJar()
}
