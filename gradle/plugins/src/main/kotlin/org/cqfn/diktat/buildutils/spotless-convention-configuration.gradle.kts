package org.cqfn.diktat.buildutils

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.diffplug.spotless")
}

@Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
val libs = the<LibrariesForLibs>()
val diktatVersion: String = libs.versions.diktat.asProvider().get()
spotless {
//    kotlin {
//        diktat(diktatVersion).configFile(rootProject.file("diktat-analysis.yml"))
//        target("src/**/*.kt")
//        targetExclude("src/test/**/*.kt", "src/*Test/**/*.kt")
//        if (path == rootProject.path) {
//            target("gradle/plugins/src/**/*.kt")
//        }
//    }
//    kotlinGradle {
//        ktlint()
//        diktat(diktatVersion).configFile(rootProject.file("diktat-analysis.yml"))
//
//        // using `Project#path` here, because it must be unique in gradle's project hierarchy
//        if (path == rootProject.path) {
//            target("$rootDir/*.kts", "$rootDir/gradle/plugins/**/*.kts")
//            targetExclude(
//                "$rootDir/build/**/*.kts",
//                "$rootDir/gradle/plugins/build/**/*.kts",
//            )
//        } else {
//            target("**/*.kts")
//            targetExclude(
//                "build/**/*.kts",
//                "src/test/**/*.kts",
//            )
//        }
//    }
}
