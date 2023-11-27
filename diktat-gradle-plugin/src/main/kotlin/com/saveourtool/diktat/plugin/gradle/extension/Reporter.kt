package com.saveourtool.diktat.plugin.gradle.extension

import com.saveourtool.diktat.api.DiktatReporterCreationArguments
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import java.nio.file.Path

/**
 * A base interface for reporter
 */
interface Reporter {
    /**
     * Location for output
     */
    @get:OutputFile
    val output: RegularFileProperty

    /**
     * @param sourceRootDir
     * @return [DiktatReporterCreationArguments] to create this reporter
     */
    fun toCreationArguments(sourceRootDir: Path): DiktatReporterCreationArguments
}
