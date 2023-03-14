import com.saveourtool.save.buildutils.*

@Suppress("DSL_SCOPE_VIOLATION", "RUN_IN_SCRIPT")  // https://github.com/gradle/gradle/issues/22797
plugins {
    id("com.saveourtool.save.buildutils.versioning-configuration")
//    id("com.saveourtool.save.buildutils.code-quality-convention")
    id("com.saveourtool.save.buildutils.publishing-configuration")
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

allprojects {
    configurations.all {
        // if SNAPSHOT dependencies are used, refresh them periodically
        resolutionStrategy.cacheDynamicVersionsFor(10, TimeUnit.MINUTES)
        resolutionStrategy.cacheChangingModulesFor(10, TimeUnit.MINUTES)
    }
}
