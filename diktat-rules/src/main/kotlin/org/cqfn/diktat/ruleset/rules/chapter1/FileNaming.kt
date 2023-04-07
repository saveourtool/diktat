package org.cqfn.diktat.ruleset.rules.chapter1

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_NAME_INCORRECT
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_NAME_MATCH_CLASS
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getFilePath
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.isPascalCase
import org.cqfn.diktat.util.isKotlinScript

import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes.FILE
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
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
            FILE_NAME_INCORRECT.warnAndFix(configRules, emitWarn, isFixMode, "$name$extension", 0, node) {
                // FixMe: we can add an autocorrect here in future, but is there any purpose to change file or class name?
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType", "ControlFlowWithEmptyBody")
    private fun checkClassNameMatchesWithFile(fileLevelNode: ASTNode) {
        val (fileNameWithoutSuffix, fileNameSuffix) = getFileParts(filePath)
        val classes = fileLevelNode.getAllChildrenWithType(CLASS)
        if (classes.size == 1) {
            val className = classes[0].getFirstChildWithType(IDENTIFIER)!!.text
            if (className != fileNameWithoutSuffix) {
                FILE_NAME_MATCH_CLASS.warnAndFix(configRules, emitWarn, isFixMode, "$fileNameWithoutSuffix$fileNameSuffix vs $className", 0, fileLevelNode) {
                    // FixMe: we can add an autocorrect here in future, but is there any purpose to change file name?
                }
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
