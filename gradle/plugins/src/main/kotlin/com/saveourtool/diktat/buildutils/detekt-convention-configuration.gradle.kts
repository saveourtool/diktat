package com.saveourtool.diktat.buildutils

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    config.setFrom(rootProject.files("detekt-config.yml"))
    basePath = rootDir.canonicalPath
    buildUponDefaultConfig = true
}

@Suppress("RUN_IN_SCRIPT")
if (path == rootProject.path) {
    tasks.register("detektAll") {
        allprojects {
            this@register.dependsOn(tasks.withType<Detekt>())
        }
    }

    tasks.register("mergeDetektReports", ReportMergeTask::class) {
        output.set(layout.buildDirectory.file("detekt-sarif-reports/detekt-merged.sarif").get().asFile)
    }
}

@Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
val reportMerge: TaskProvider<ReportMergeTask> = rootProject.tasks.named<ReportMergeTask>("mergeDetektReports") {
    input.from(
        tasks.withType<Detekt>().map { it.sarifReportFile }
    )
    shouldRunAfter(tasks.withType<Detekt>())
}
tasks.withType<Detekt>().configureEach {
    reports.sarif.required.set(true)
    finalizedBy(reportMerge)
}
