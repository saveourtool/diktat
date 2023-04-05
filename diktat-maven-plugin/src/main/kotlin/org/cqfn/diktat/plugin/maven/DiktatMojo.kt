/**
 * MOJOs for goals of diktat plugin
 */

package org.cqfn.diktat.plugin.maven

import org.cqfn.diktat.DiktatRunner
import org.cqfn.diktat.DiktatRunnerArguments
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider

import org.apache.maven.plugins.annotations.Mojo

/**
 * Main [Mojo] that call [DiktatRuleSetProvider]'s rules on [inputs] files
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
 * Main [Mojo] that call [DiktatRuleSetProvider]'s rules on [inputs] files
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
