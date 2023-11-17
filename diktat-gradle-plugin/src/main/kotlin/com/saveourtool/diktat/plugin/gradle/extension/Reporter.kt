package com.saveourtool.diktat.plugin.gradle.extension

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

/**
 * A base interface for reporter
 */
interface Reporter {
    /**
     * Location for output
     */
    val output: RegularFileProperty
}
