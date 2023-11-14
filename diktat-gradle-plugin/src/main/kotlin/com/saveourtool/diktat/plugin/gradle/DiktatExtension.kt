package com.saveourtool.diktat.plugin.gradle

import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.VerificationTask
import org.gradle.api.tasks.util.PatternFilterable

/**
 * An extension to configure diktat in build.gradle(.kts) file
 */
abstract class DiktatExtension {
    /**
     * Boolean flag to support `ignoreFailures` property of [VerificationTask].
     */
    abstract val ignoreFailures: Property<Boolean>

    /**
     * Property that will be used if you need to publish the report to GitHub
     */
    abstract val githubActions: Property<Boolean> // = false

    /**
     * Type of the reporter to use
     */
    abstract val reporter: Property<String>

    /**
     * Destination for reporter. If empty, will write to stdout.
     */
    abstract val output: RegularFileProperty

    /**
     * Baseline file, containing a list of errors that will be ignored.
     * If this file doesn't exist, it will be created on the first invocation.
     */
    abstract val baseline: RegularFileProperty

    /**
     * Path to diktat yml config file. Can be either absolute or relative to project's root directory.
     * Default value: `diktat-analysis.yml` in rootDir.
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val diktatConfigFile: RegularFileProperty

    /**
     * @return [PatternFilterable] to configure input files for diktat task
     */
    @Nested
    abstract fun getInputs(): PatternFilterable

    /**
     * Configure input files for diktat task
     *
     * @param action configuration lambda for [PatternFilterable]
     */
    fun inputs(action: Action<in PatternFilterable>) = action.execute(getInputs())
}
