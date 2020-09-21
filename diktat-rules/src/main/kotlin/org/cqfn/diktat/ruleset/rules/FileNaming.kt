package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_NAME_INCORRECT
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_NAME_MATCH_CLASS
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getFileName
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.isPascalCase
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
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
class FileNaming(private val configRules: List<RulesConfig>) : Rule("file-naming") {

    companion object {
        // FixMe: should be moved to properties
        val VALID_EXTENSIONS = listOf(".kt", ".kts")
    }

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private lateinit var fileName: String
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == FILE) {
            fileName = node.getFileName()
            checkFileNaming(node)
            checkClassNameMatchesWithFile(node)
        }
    }

    private fun checkFileNaming(node: ASTNode) {
        val (name, extension) = getFileParts(fileName)
        if (!name.isPascalCase() || !VALID_EXTENSIONS.contains(extension)) {
            FILE_NAME_INCORRECT.warnAndFix(configRules, emitWarn, isFixMode, "$name$extension", 0, node) {
                // FixMe: we can add an autocorrect here in future, but is there any purpose to change file or class name?
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkClassNameMatchesWithFile(fileLevelNode: ASTNode) {
        val (fileNameWithoutSuffix, fileNameSuffix) = getFileParts(fileName)
        val classes = fileLevelNode.getAllChildrenWithType(CLASS)
        if (classes.size == 1) {
            val className = classes[0].getFirstChildWithType(IDENTIFIER)!!.text
            if (className != fileNameWithoutSuffix) {
                FILE_NAME_MATCH_CLASS.warnAndFix(configRules, emitWarn, isFixMode, "$fileNameWithoutSuffix$fileNameSuffix vs $className", 0, fileLevelNode) {

                    // FixMe: we can add an autocorrect here in future, but is there any purpose to change file name?
                }
            }
        }
    }

    private fun getFileParts(fileName: String): Pair<String, String> {
        val file = File(fileName)
        val fileNameWithoutSuffix = file.name.replace(Regex("\\..*"), "")
        val fileNameSuffix = file.name.replace(fileNameWithoutSuffix, "")
        return Pair(fileNameWithoutSuffix, fileNameSuffix)
    }
}
