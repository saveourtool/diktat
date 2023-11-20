package com.saveourtool.diktat.plugin.gradle.extension

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import javax.inject.Inject

/**
 * A base interface for reporter
 */
abstract class Reporter {
    /**
     * Location for output
     */
    @get:OutputFile
    abstract val output: RegularFileProperty
}
