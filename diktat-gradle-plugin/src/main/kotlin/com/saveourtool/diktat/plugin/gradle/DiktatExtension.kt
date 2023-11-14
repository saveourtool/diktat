package com.saveourtool.diktat.plugin.gradle

import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.util.PatternFilterable

/**
 * An extension to configure diktat in build.gradle(.kts) file
 */
abstract class DiktatExtension {
    /**
     * @return boolean flag to support `ignoreFailures` property of [VerificationTask].
     */
    abstract fun getIgnoreFailures(): Property<Boolean>

    /**
     * @return Property that will be used if you need to publish the report to GitHub
     */
    abstract val githubActions: Property<Boolean> // = false

    /**
     * Type of the reporter to use
     */
    abstract fun getReporter(): Property<String> // = ""

    /**
     * Destination for reporter. If empty, will write to stdout.
     */
    abstract fun getOutput(): Property<String> // = ""

    /**
     * @return Baseline file, containing a list of errors that will be ignored.
     * If this file doesn't exist, it will be created on the first invocation.
     */
    abstract fun getBaseline(): Property<String> // null

    /**
     * Path to diktat yml config file. Can be either absolute or relative to project's root directory.
     * Default value: `diktat-analysis.yml` in rootDir.
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val diktatConfigFile: RegularFileProperty

    @Nested
    abstract fun getInputs(): PatternFilterable

    fun inputs(action: Action<in PatternFilterable>) = action.execute(getInputs())
}
