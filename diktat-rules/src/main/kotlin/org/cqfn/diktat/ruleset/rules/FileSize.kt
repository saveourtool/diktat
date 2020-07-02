package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.*
import org.cqfn.diktat.ruleset.utils.splitPathToDirs
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.slf4j.LoggerFactory

class FileSize : Rule("file-size") {

    companion object {
        var MAX_SIZE = 2000L
        var IGNOR_FOLDER = listOf<String>()
        private val log = LoggerFactory.getLogger(FileSize::class.java)
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

        parseConfig(configRules.getRuleConfig(FILE_SIZE_LARGER)?.configuration ?: mapOf())

        val realPackageName = calculateRealPackageName(fileName)

        if (!realPackageName.containsAll(IGNOR_FOLDER)) {
            if (node.elementType == ElementType.FILE) {
                checkFileSize(node)
            }
        }
    }

    private fun calculateRealPackageName(fileName: String?): List<String> {
        val filePathParts = fileName?.splitPathToDirs()
        return if (filePathParts == null) {
            log.error("Not able to determine a path to a scanned file or src directory cannot be found in it's path." +
                " Will not be able to determine correct package name. It can happen due to missing <src> directory in the path")
            listOf()
        } else {
            filePathParts
        }
    }

    private fun parseConfig(config: Map<String, String>){
        if (!config.isEmpty()){
            val ignorFolders =  config.get("ignorFolders")
            val maxSize = config.get("maxSize")
            if (ignorFolders != null){
                val listIgnorFolders = ignorFolders.trim().split(",")
                IGNOR_FOLDER = listIgnorFolders
            }
            if (maxSize?.toLongOrNull() != null){
                MAX_SIZE = maxSize.toLong()
            }
        }
    }

    private fun checkFileSize(node: ASTNode){
        val size = node.text.split("\n".toRegex()).size
        if (size > MAX_SIZE){
            FILE_SIZE_LARGER.warn(configRules, emitWarn, isFixMode, "$size" ,node.startOffset)
        }
    }
}
