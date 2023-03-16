@Suppress("DSL_SCOPE_VIOLATION", "RUN_IN_SCRIPT")  // https://github.com/gradle/gradle/issues/22797
plugins {
    id("org.cqfn.diktat.buildutils.versioning-configuration")
    id("org.cqfn.diktat.buildutils.git-hook-configuration")
    id("org.cqfn.diktat.buildutils.code-quality-convention")
    id("org.cqfn.diktat.buildutils.publishing-configuration")
    alias(libs.plugins.talaiot.base)
    java
}

talaiot {
    metrics {
        // disabling due to problems with OSHI on some platforms
        performanceMetrics = false
        environmentMetrics = false
    }
    publishers {
        timelinePublisher = true
    }
}

project.description = "diKTat kotlin formatter and fixer"
