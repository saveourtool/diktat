package com.saveourtool.diktat.plugin.gradle

import com.saveourtool.diktat.plugin.gradle.extension.Reporters
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import java.io.File
import javax.inject.Inject

/**
 * An extension to configure diktat in build.gradle(.kts) file
 *
 * @param objectFactory
 * @param patternSet
 */
open class DiktatExtension @Inject constructor(
    objectFactory: ObjectFactory,
    private val patternSet: PatternSet,
) {
    /**
     * All reporters
     */
    @get:Internal
    val reporters: Reporters = objectFactory.newInstance(Reporters::class.java)

    /**
     * Boolean flag to support `ignoreFailures` property of [VerificationTask].
     */
    var ignoreFailures: Boolean = false

    /**
     * Flag that indicates whether to turn debug logging on
     */
    var debug = false

    /**
     * Property that will be used if you need to publish the report to GitHub
     */
    var githubActions = false

    /**
     * Baseline file, containing a list of errors that will be ignored.
     * If this file doesn't exist, it will be created on the first invocation.
     */
    var baseline: String? = null

    /**
     * Path to diktat yml config file. Can be either absolute or relative to project's root directory.
     * Default value: `diktat-analysis.yml` in rootDir if it exists or default (empty) configuration
     */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    var diktatConfigFile: File? = null

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
     * @param action configuration lambda for [Reporters]
     */
    fun reporters(action: Action<Reporters>): Unit = action.execute(reporters)
}
