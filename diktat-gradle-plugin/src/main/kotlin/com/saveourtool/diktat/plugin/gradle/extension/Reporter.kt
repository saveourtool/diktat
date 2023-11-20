package com.saveourtool.diktat.plugin.gradle.extension

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile

/**
 * A base interface for reporter
 */
interface Reporter {
    /**
     * Location for output
     */
    @get:OutputFile
    val output: RegularFileProperty
}
