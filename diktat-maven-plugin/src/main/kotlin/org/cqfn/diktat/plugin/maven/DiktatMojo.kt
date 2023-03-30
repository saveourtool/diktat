/**
 * MOJOs for goals of diktat plugin
 */

package org.cqfn.diktat.plugin.maven

import org.cqfn.diktat.DiktatProcessCommand
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider

import org.apache.maven.plugins.annotations.Mojo

/**
 * Main [Mojo] that call [DiktatRuleSetProvider]'s rules on [inputs] files
 */
@Mojo(name = "check")
@Suppress("unused")
class DiktatCheckMojo : DiktatBaseMojo() {
    override fun runAction(command: DiktatProcessCommand, formattedContentConsumer: (String) -> Unit) {
        command.check()
    }
}

/**
 * Main [Mojo] that call [DiktatRuleSetProvider]'s rules on [inputs] files
 * and fixes discovered errors
 */
@Mojo(name = "fix")
@Suppress("unused")
class DiktatFixMojo : DiktatBaseMojo() {
    override fun runAction(command: DiktatProcessCommand, formattedContentConsumer: (String) -> Unit) {
        val formattedText = command.fix()
        formattedContentConsumer(formattedText)
    }
}
