/**
 * MOJOs for goals of diktat plugin
 */

package org.cqfn.diktat.plugin.maven

import org.cqfn.diktat.DiktatProcessCommand
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProviderV2

import org.apache.maven.plugins.annotations.Mojo
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

/**
 * Main [Mojo] that call [DiktatRuleSetProviderV2]'s rules on [inputs] files
 */
@Mojo(name = "check")
@Suppress("unused")
class DiktatCheckMojo : DiktatBaseMojo() {
    /**
     * @param command instance of [DiktatProcessCommand] used in analysis
     */
    override fun runAction(command: DiktatProcessCommand) {
        command.check()
    }
}

/**
 * Main [Mojo] that call [DiktatRuleSetProviderV2]'s rules on [inputs] files
 * and fixes discovered errors
 */
@Mojo(name = "fix")
@Suppress("unused")
class DiktatFixMojo : DiktatBaseMojo() {
    /**
     * @param command instance of [DiktatProcessCommand] used in analysis
     */
    override fun runAction(command: DiktatProcessCommand) {
        val fileName = command.file.absolutePathString()
        val fileContent = command.fileContent
        val formattedText = command.fix()
        if (fileContent != formattedText) {
            log.info("Original and formatted content differ, writing to $fileName...")
            command.file.writeText(formattedText, Charsets.UTF_8)
        }
    }
}
