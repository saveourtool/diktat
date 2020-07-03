package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.*
import org.cqfn.diktat.ruleset.utils.getIdentifierName
import org.cqfn.diktat.ruleset.utils.prettyPrint
import org.cqfn.diktat.ruleset.utils.splitPathToDirs
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.math.max

class FileSize : Rule("file-size") {

    companion object {
        private val log = LoggerFactory.getLogger(FileSize::class.java)
        const val MAX_SIZE = 2000L
        private val IGNORE_FOLDER = listOf<String>()
        const val IGNORE_FOLDERS_SEPARATOR = ","
        const val SRC_PATH = "src"
    }

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var fileName: String? = null
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.rulesConfigList!!
        fileName = params.fileName
        emitWarn = emit
        isFixMode = autoCorrect
        if (node.elementType == ElementType.FILE) {
            val configuration = FileSizeConfiguration(configRules.getRuleConfig(FILE_IS_TOO_LONG)?.configuration
                ?: mapOf())
            val maxSize = if (configuration.maxSize != null && configuration.maxSize!!.toLongOrNull() != null) configuration.maxSize!!.toLong() else MAX_SIZE
            val ignoreFolders = if (configuration.ignoreFolders != null) configuration.ignoreFolders!!.split(IGNORE_FOLDERS_SEPARATOR) else IGNORE_FOLDER

            val realFilePath = calculateFilePath(fileName)

            if (!realFilePath.contains(SRC_PATH)) {
                log.error("src directory not found in file path")
            } else {
                if (ignoreFolders.isEmpty()) {
                    checkFileSize(node, maxSize)
                } else {
                    ignoreFolders.forEach {
                        if (!realFilePath.containsAll(it.split("/"))) {
                            checkFileSize(node, maxSize)
                        }
                    }
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

    private fun checkFileSize(node: ASTNode, maxSize: Long){
        val size = node.text.split("\n").size
        if (size > maxSize){
            FILE_IS_TOO_LONG.warn(configRules, emitWarn, isFixMode, "$size" ,node.startOffset)
        }
    }

    class FileSizeConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val maxSize = if(config.containsKey("maxSize")) config.get("maxSize") else null
        val ignoreFolders = if(config.containsKey("ignoreFolders")) config.get("maxSize") else null
    }
}
