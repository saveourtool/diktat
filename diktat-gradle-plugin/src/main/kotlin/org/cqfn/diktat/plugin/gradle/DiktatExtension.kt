package org.cqfn.diktat.plugin.gradle

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import java.io.File

/**
 * An extension to configure diktat in build.gradle(.kts) file
 */
open class DiktatExtension(
    private val patternSet: PatternSet
) {
    /**
     * Boolean flag to support `ignoreFailures` property of [VerificationTask].
     */
    var ignoreFailures: Boolean = false

    /**
     * Flag that indicates whether to turn debug logging on
     */
    var debug = false

    /**
     * Type of the reporter to use
     */
    var reporterType: String = "plain"

    /**
     * Type of output
     * Default: System.out
     */
    var output: String = ""

    /**
     * Paths that will be excluded from diktat run
     */
    @Deprecated("Configuration via inputs/excludes is unsupported, use inputs(Action)")
    var excludes: FileCollection? = null

    /**
     * Paths that will be scanned for .kt(s) files
     */
    @Deprecated("Configuration via inputs/excludes is unsupported, use inputs(Action)")
    var inputs: FileCollection? = null

    /**
     * Path to diktat yml config file. Can be either absolute or relative to project's root directory.
     * Default value: `diktat-analysis.yml` in rootDir.
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    lateinit var diktatConfigFile: File

    /**
     * Configure input files for diktat task
     *
     * @param action configuration lambda for `PatternFilterable`
     */
    fun inputs(action: PatternFilterable.() -> Unit) {
        action.invoke(patternSet)
    }
}
