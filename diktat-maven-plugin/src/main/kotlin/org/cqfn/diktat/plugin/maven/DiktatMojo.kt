/**
 * MOJOs for goals of diktat plugin
 */

package org.cqfn.diktat.plugin.maven

import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider

import com.pinterest.ktlint.core.KtLint
import org.apache.maven.plugins.annotations.Mojo

import java.io.File

/**
 * Main [Mojo] that call [DiktatRuleSetProvider]'s rules on [inputs] files
 */
@Mojo(name = "check")
@Suppress("unused")
class DiktatCheckMojo : DiktatBaseMojo() {
    /**
     * @param params instance of [KtLint.ExperimentalParams] used in analysis
     */
    override fun runAction(params: KtLint.ExperimentalParams) {
        KtLint.lint(params)
    }
}

/**
 * Main [Mojo] that call [DiktatRuleSetProvider]'s rules on [inputs] files
 * and fixes discovered errors
 */
@Mojo(name = "fix")
@Suppress("unused")
class DiktatFixMojo : DiktatBaseMojo() {
    /**
     * @param params instance of [KtLint.Params] used in analysis
     */
    override fun runAction(params: KtLint.ExperimentalParams) {
        val fileName = params.fileName
        val fileContent = File(fileName).readText(charset("UTF-8"))
        val formattedText = KtLint.format(params)
        if (fileContent != formattedText) {
            log.info("Original and formatted content differ, writing to $fileName...")
            File(fileName).writeText(formattedText, charset("UTF-8"))
        }
    }
}
