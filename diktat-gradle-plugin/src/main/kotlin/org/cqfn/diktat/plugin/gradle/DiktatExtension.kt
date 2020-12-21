package org.cqfn.diktat.plugin.gradle

import com.pinterest.ktlint.core.Reporter
import org.gradle.api.file.FileCollection

/**
 * An extension to configure diktat in build.gradle(.kts) file
 */
open class DiktatExtension {
    /**
     * Flag that indicates whether to turn debug logging on
     */
    var debug = false

    /**
     * Path to diktat yml config file. Can be either absolute or relative to project's root directory.
     * Private until gradle supports kotlin 1.4 and we can pass this value to DiktatRuleSetProvider
     */
    internal var diktatConfigFile: String = "diktat-analysis.yml"

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
