rootProject.name = "diktat"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.12.4"
}

includeBuild("gradle/plugins")
include("diktat-common")
include("diktat-gradle-plugin")
include("diktat-maven-plugin")
include("diktat-rules")
include("diktat-ruleset")
include("diktat-test-framework")
include("diktat-dev-ksp")

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
