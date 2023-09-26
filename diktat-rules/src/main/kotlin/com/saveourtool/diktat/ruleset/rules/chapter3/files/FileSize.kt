package com.saveourtool.diktat.ruleset.rules.chapter3.files

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.FILE_IS_TOO_LONG
import com.saveourtool.diktat.ruleset.rules.DiktatRule

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

/**
 * Rule that checks number of lines in a file
 */
class FileSize(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(FILE_IS_TOO_LONG)
) {
    private val configuration by lazy {
        FileSizeConfiguration(
            this.configRules.getRuleConfig(FILE_IS_TOO_LONG)?.configuration ?: emptyMap()
        )
    }

    override fun logic(node: ASTNode) {
        if (node.elementType == KtFileElementType.INSTANCE) {
            checkFileSize(node, configuration.maxSize)
        }
    }

    private fun checkFileSize(node: ASTNode, maxSize: Long) {
        val size = node
            .text
            .split("\n")
            .size
        if (size > maxSize) {
            FILE_IS_TOO_LONG.warn(configRules, emitWarn, size.toString(), node.startOffset, node)
        }
    }

    /**
     * [RuleConfiguration] for maximun number of lines in a file
     */
    class FileSizeConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Maximum allowed number of lines in a file
         */
        val maxSize = config["maxSize"]?.toLongOrNull() ?: MAX_SIZE
    }

    companion object {
        const val MAX_SIZE = 2000L
        const val NAME_ID = "file-size"
    }
}
