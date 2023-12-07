package com.saveourtool.diktat.ruleset.config

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * class returns the list of common configurations that we have read from a configuration map
 *
 * @param configuration map of common configuration
 */
data class CommonConfiguration(private val configuration: Map<String, String>?) {
    /**
     * List of directory names which will be used to detect test sources
     */
    val testAnchors: List<String> by lazy {
        val testDirs = (configuration ?: emptyMap()).getOrDefault("testDirs", "test").split(',').map { it.trim() }
        if (testDirs.any { !it.lowercase(Locale.getDefault()).endsWith("test") }) {
            log.error { "test directory names should end with `test`" }
        }
        testDirs
    }

    /**
     * Start of package name, which shoould be common, e.g. org.example.myproject
     */
    val domainName: String? by lazy {
        configuration?.get("domainName")
    }

    /**
     * Get disable chapters from configuration
     */
    val disabledChapters: String? by lazy {
        configuration?.get("disabledChapters")
    }

    /**
     * Get version of kotlin from configuration
     */
    val kotlinVersion: KotlinVersion by lazy {
        configuration?.get("kotlinVersion")?.kotlinVersion() ?: run {
            if (visitorCounter.incrementAndGet() == 1) {
                log.error { "Kotlin version not specified in the configuration file. Will be using ${KotlinVersion.CURRENT} version" }
            }
            KotlinVersion.CURRENT
        }
    }

    /**
     * Get source directories from configuration
     */
    val srcDirectories: List<String> by lazy {
        configuration?.get("srcDirectories")?.split(",")?.map { it.trim() } ?: listOf("main")
    }

    companion object {
        internal val log: KLogger = KotlinLogging.logger {}

        /**
         * Counter that helps not to raise multiple warnings about kotlin version
         */
        var visitorCounter = AtomicInteger(0)
    }
}

/**
 * Parse string into KotlinVersion
 *
 * @return KotlinVersion from configuration
 */
internal fun String.kotlinVersion(): KotlinVersion {
    require(this.contains("^(\\d+\\.)(\\d+)\\.?(\\d+)?$".toRegex())) {
        "Kotlin version format is incorrect"
    }
    val versions = this.split(".").map { it.toInt() }
    return if (versions.size == 2) {
        KotlinVersion(versions[0], versions[1])
    } else {
        KotlinVersion(versions[0], versions[1], versions[2])
    }
}
