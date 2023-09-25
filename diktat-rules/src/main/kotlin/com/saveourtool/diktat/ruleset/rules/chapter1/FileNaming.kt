package com.saveourtool.diktat.ruleset.rules.chapter1

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.FILE_NAME_INCORRECT
import com.saveourtool.diktat.ruleset.constants.Warnings.FILE_NAME_MATCH_CLASS
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.getAllChildrenWithType
import com.saveourtool.diktat.ruleset.utils.getFilePath
import com.saveourtool.diktat.ruleset.utils.getFirstChildWithType
import com.saveourtool.diktat.ruleset.utils.isPascalCase
import com.saveourtool.diktat.util.isKotlinScript

import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

import java.io.File

/**
 * This visitor covers rule 1.2 of Huawei code style. It covers following rules related to a file naming:
 * 1) File must have ".kt" extension
 * 2) File must be in camel case
 * 3) File name must start with capital letter (PascalCase)
 *
 * Aggressive: In case file contains only one class on upper level - it should be named with the same name
 */
@Suppress("ForbiddenComment")
class FileNaming(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(FILE_NAME_INCORRECT, FILE_NAME_MATCH_CLASS)
) {
    private lateinit var filePath: String

    override fun logic(node: ASTNode) {
        if (node.elementType == KtFileElementType.INSTANCE) {
            filePath = node.getFilePath()
            if (!filePath.isKotlinScript()) {
                checkFileNaming(node)
                checkClassNameMatchesWithFile(node)
            }
        }
    }

    private fun checkFileNaming(node: ASTNode) {
        val (name, extension) = getFileParts(filePath)
        if (!name.isPascalCase() || !validExtensions.contains(extension)) {
            // FixMe: we can add an autocorrect here in future, but is there any purpose to change file or class name?
            FILE_NAME_INCORRECT.warn(configRules, emitWarn, "$name$extension", 0, node)
        }
    }

    @Suppress("UnsafeCallOnNullableType", "ControlFlowWithEmptyBody")
    private fun checkClassNameMatchesWithFile(fileLevelNode: ASTNode) {
        val (fileNameWithoutSuffix, fileNameSuffix) = getFileParts(filePath)
        val classes = fileLevelNode.getAllChildrenWithType(CLASS)
        if (classes.size == 1) {
            val className = classes[0].getFirstChildWithType(IDENTIFIER)!!.text
            if (className != fileNameWithoutSuffix) {
                // FixMe: we can add an autocorrect here in future, but is there any purpose to change file name?
                FILE_NAME_MATCH_CLASS.warn(configRules, emitWarn, "$fileNameWithoutSuffix$fileNameSuffix vs $className", 0, fileLevelNode)
            }
        } else {
            // FixMe: need to check that if there are several classes - at least one of them should match
        }
    }

    private fun getFileParts(fileName: String): Pair<String, String> {
        val file = File(fileName)
        val fileNameWithoutSuffix = file.name.replace(Regex("\\..*"), "")
        val fileNameSuffix = file.name.replace(fileNameWithoutSuffix, "")
        return Pair(fileNameWithoutSuffix, fileNameSuffix)
    }

    companion object {
        // FixMe: should be moved to properties
        const val NAME_ID = "file-naming"
        val validExtensions = listOf(".kt", ".kts")
    }
}
