rootProject.name = "diktat"

dependencyResolutionManagement {
    repositories {
        file("$rootDir/build/diktat-snapshot")
            .takeIf { it.exists() }
            ?.run {
                maven {
                    url = this@run.toURI()
                }
            }
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            content {
                includeGroup("com.saveourtool.diktat")
            }
        }
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        file("$rootDir/build/diktat-snapshot")
            .takeIf { it.exists() }
            ?.run {
                maven {
                    url = this@run.toURI()
                }
            }
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            content {
                includeGroup("com.saveourtool.diktat")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.16.1"
    // starting from Gradle 8, it's needed to configure a repo from which to take Java for a toolchain
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

includeBuild("gradle/plugins")
include("diktat-api")
include("diktat-common-test")
include("diktat-ktlint-engine")
include("diktat-gradle-plugin")
include("diktat-maven-plugin")
include("diktat-rules")
include("diktat-ruleset")
include("diktat-dev-ksp")
include("diktat-cli")
include("diktat-runner")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

gradleEnterprise {
    @Suppress("AVOID_NULL_CHECKS")
    if (System.getenv("CI") != null) {
        buildScan {
            publishAlways()
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
    }
}
