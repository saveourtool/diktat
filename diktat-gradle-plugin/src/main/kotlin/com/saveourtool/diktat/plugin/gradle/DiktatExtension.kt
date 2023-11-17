package com.saveourtool.diktat.plugin.gradle

import com.saveourtool.diktat.plugin.gradle.extension.Reporter
import com.saveourtool.diktat.plugin.gradle.extension.ReportersDsl
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import java.io.File
import javax.inject.Inject

/**
 * An extension to configure diktat in build.gradle(.kts) file
 *
 * @param patternSet
 * @param reporters
 */
open class DiktatExtension @Inject constructor(
    private val objectFactory: ObjectFactory,
    private val patternSet: PatternSet,
    reporters: List<Reporter>,
) {
    private val reportersDsl: ReportersDsl = objectFactory.newInstance(ReportersDsl::class.java, reporters)

    /**
     * Boolean flag to support `ignoreFailures` property of [VerificationTask].
     */
    var ignoreFailures: Boolean = false

    /**
     * Flag that indicates whether to turn debug logging on
     */
    var debug = false

    /**
     * Baseline file, containing a list of errors that will be ignored.
     * If this file doesn't exist, it will be created on the first invocation.
     */
    var baseline: String? = null

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
        action(patternSet)
    }

    /**
     * Configure reporters
     *
     * @param action configuration lambda for [ReportersDsl]
     */
    fun reporters(action: Action<ReportersDsl>): Unit = action.execute(reportersDsl)
}
