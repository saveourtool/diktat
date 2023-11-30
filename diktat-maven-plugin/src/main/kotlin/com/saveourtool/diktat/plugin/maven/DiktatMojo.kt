/**
 * MOJOs for goals of diktat plugin
 */

package com.saveourtool.diktat.plugin.maven

import com.saveourtool.diktat.DiktatRunner
import com.saveourtool.diktat.DiktatRunnerArguments

import org.apache.maven.plugins.annotations.Mojo

/**
 * Main [Mojo] that call diktat's rules on [inputs] files
 */
@Mojo(name = "check")
@Suppress("unused")
class DiktatCheckMojo : DiktatBaseMojo() {
    override fun runAction(
        runner: DiktatRunner,
        args: DiktatRunnerArguments,
    ): Int = runner.checkAll(args)
}

/**
 * Main [Mojo] that call diktat's rules on [inputs] files
 * and fixes discovered errors
 */
@Mojo(name = "fix")
@Suppress("unused")
class DiktatFixMojo : DiktatBaseMojo() {
    override fun runAction(
        runner: DiktatRunner,
        args: DiktatRunnerArguments,
    ): Int = runner.fixAll(args) { updatedFile ->
        log.info("Original and formatted content differ, writing to ${updatedFile.fileName}...")
    }
}
