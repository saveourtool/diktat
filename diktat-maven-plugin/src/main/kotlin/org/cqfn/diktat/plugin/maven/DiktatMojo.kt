/**
 * MOJOs for goals of diktat plugin
 */

package org.cqfn.diktat.plugin.maven

import org.cqfn.diktat.DiktatProcessor
import org.cqfn.diktat.api.DiktatProcessorListener
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider

import org.apache.maven.plugins.annotations.Mojo
import java.nio.file.Path

/**
 * Main [Mojo] that call [DiktatRuleSetProvider]'s rules on [inputs] files
 */
@Mojo(name = "check")
@Suppress("unused")
class DiktatCheckMojo : DiktatBaseMojo() {
    override fun runAction(processor: DiktatProcessor, listener: DiktatProcessorListener, files: Sequence<Path>, formattedContentConsumer: (Path, String) -> Unit) {
        processor.checkAll(listener, files)
    }
}

/**
 * Main [Mojo] that call [DiktatRuleSetProvider]'s rules on [inputs] files
 * and fixes discovered errors
 */
@Mojo(name = "fix")
@Suppress("unused")
class DiktatFixMojo : DiktatBaseMojo() {
    override fun runAction(processor: DiktatProcessor, listener: DiktatProcessorListener, files: Sequence<Path>, formattedContentConsumer: (Path, String) -> Unit) {
        processor.fixAll(listener, files, formattedContentConsumer)
    }
}
