package org.cqfn.diktat.ruleset.rules.files

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_IS_TOO_LONG
import org.cqfn.diktat.ruleset.utils.splitPathToDirs
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.slf4j.LoggerFactory

class FileSize(private val configRules: List<RulesConfig>) : Rule("file-size") {
    companion object {
        private val log = LoggerFactory.getLogger(FileSize::class.java)
        const val MAX_SIZE = 2000L
        private val IGNORE_FOLDER = emptyList<String>()
        const val IGNORE_FOLDERS_SEPARATOR = ","
        const val SRC_PATH = "src"
    }

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var fileName: String? = null
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect
        if (node.elementType == ElementType.FILE) {
            fileName = node.getUserData(KtLint.FILE_PATH_USER_DATA_KEY)!!
            val configuration = FileSizeConfiguration(
                this.configRules.getRuleConfig(FILE_IS_TOO_LONG)?.configuration ?: mapOf()
            )
            val ignoreFolders = configuration.ignoreFolders

            val realFilePath = calculateFilePath(fileName)

            if (!realFilePath.contains(SRC_PATH)) {
                log.error("$SRC_PATH directory is not found in file path")
            } else {
                if (ignoreFolders.none { realFilePath.containsAll(it.splitPathToDirs()) }) {
                    checkFileSize(node, configuration.maxSize)
                }
            }
        }
    }

    private fun calculateFilePath(fileName: String?): List<String> {
        val filePathParts = fileName?.splitPathToDirs()
        return if (filePathParts == null) {
            log.error("Could not find absolute path to file")
            listOf()
        } else {
            filePathParts
        }
    }

    private fun checkFileSize(node: ASTNode, maxSize: Long) {
        val size = node
                .text
                .split("\n")
                .size
        if (size > maxSize) {
            FILE_IS_TOO_LONG.warn(configRules, emitWarn, isFixMode, "$size", node.startOffset, node)
        }
    }

    class FileSizeConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val maxSize = config["maxSize"]?.toLongOrNull() ?: MAX_SIZE
        val ignoreFolders = config["ignoreFolders"]?.replace("\\s+".toRegex(), "")?.split(IGNORE_FOLDERS_SEPARATOR) ?: IGNORE_FOLDER
    }
}
