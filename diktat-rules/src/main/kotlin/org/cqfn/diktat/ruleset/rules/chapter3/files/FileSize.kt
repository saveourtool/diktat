package org.cqfn.diktat.ruleset.rules.chapter3.files

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_IS_TOO_LONG
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.SRC_DIRECTORY_NAME
import org.cqfn.diktat.ruleset.utils.getFilePath
import org.cqfn.diktat.ruleset.utils.isGradleScript
import org.cqfn.diktat.ruleset.utils.splitPathToDirs

import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.slf4j.LoggerFactory

/**
 * Rule that checks number of lines in a file
 */
class FileSize(configRules: List<RulesConfig>) : DiktatRule(
    "file-size",
    configRules,
    listOf(FILE_IS_TOO_LONG)) {
    private val configuration by lazy {
        FileSizeConfiguration(
            this.configRules.getRuleConfig(FILE_IS_TOO_LONG)?.configuration ?: emptyMap()
        )
    }

    override fun logic(node: ASTNode) {
        if (node.elementType == ElementType.FILE) {
            checkFileSize(node, configuration.maxSize)
        }
    }

    private fun checkFileSize(node: ASTNode, maxSize: Long) {
        val size = node
            .text
            .split("\n")
            .size
        if (size > maxSize) {
            FILE_IS_TOO_LONG.warn(configRules, emitWarn, isFixMode, size.toString(), node.startOffset, node)
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
    }
}
