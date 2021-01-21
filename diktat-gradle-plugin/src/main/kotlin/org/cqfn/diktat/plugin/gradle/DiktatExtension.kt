package org.cqfn.diktat.plugin.gradle

import com.pinterest.ktlint.core.Reporter
import org.gradle.api.file.FileCollection
import java.io.File

/**
 * An extension to configure diktat in build.gradle(.kts) file
 */
open class DiktatExtension {
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
     * Path to diktat yml config file. Can be either absolute or relative to project's root directory.
     * Default value: `diktat-analysis.yml` in rootDir.
     */
    lateinit var diktatConfigFile: File

    /**
     * Paths that will be excluded from diktat run
     */
    lateinit var excludes: FileCollection

    /**
     * Ktlint's [Reporter] which will be used during run.
     * Private until I find a way to configure it.
     */
    internal lateinit var reporter: Reporter

    /**
     * Paths that will be scanned for .kt(s) files
     */
    lateinit var inputs: FileCollection
}
