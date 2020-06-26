package org.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import org.diktat.common.config.rules.RulesConfig
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.diktat.ruleset.constants.Warnings.*
import org.diktat.ruleset.utils.getAllChildrenWithType
import org.diktat.ruleset.utils.getFirstChildWithType
import org.diktat.ruleset.utils.isPascalCase
import java.io.File

/**
 * This visitor covers rule 1.2 of Huawei code style. It covers following rules related to a file naming:
 * 1) File must have ".kt" extension
 * 2) File must be in camel case
 * 3) File name must start with capital letter (PascalCase)
 *
 * Aggressive: In case file contains only one class on upper level - it should be named with the same name
 */
class FileNaming : Rule("file-naming") {

    companion object {
        // FixMe: should be moved to properties
        val VALID_EXTENSIONS = listOf(".kt", ".kts")
    }

    private lateinit var confiRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var fileName: String? = null
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        confiRules = params.rulesConfigList!!
        fileName = params.fileName
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == FILE) {
            checkFileNaming()
            checkClassNameMatchesWithFile(node)
        }
    }

    private fun checkFileNaming() {
        if (fileName != null) {
            val (name, extension) = getFileParts()
            if (!name.isPascalCase() || !VALID_EXTENSIONS.contains(extension)) {
                FILE_NAME_INCORRECT.warnAndFix(confiRules, emitWarn, isFixMode, "$name$extension", 0) {
                    // FixMe: we can add an autocorrect here in future, but is there any purpose to change file or class name?
                }
            }
        }
    }

    private fun checkClassNameMatchesWithFile(fileLevelNode: ASTNode) {
        if (fileName != null) {
            val (fileNameWithoutSuffix, fileNameSuffix) = getFileParts()
            val classes = fileLevelNode.getAllChildrenWithType(CLASS)
            if (classes.size == 1) {
                val className = classes[0].getFirstChildWithType(IDENTIFIER)!!.text
                if (className != fileNameWithoutSuffix) {
                    FILE_NAME_MATCH_CLASS.warnAndFix(confiRules, emitWarn, isFixMode, "$fileNameWithoutSuffix$fileNameSuffix vs $className", 0) {
                        // FixMe: we can add an autocorrect here in future, but is there any purpose to change file name?
                    }
                }
            }
        }
    }

    private fun getFileParts(): Pair<String, String> {
        val file = File(fileName!!)
        val fileNameWithoutSuffix = file.name.replace(Regex("\\..*"), "")
        val fileNameSuffix = file.name.replace(fileNameWithoutSuffix, "")
        return Pair(fileNameWithoutSuffix, fileNameSuffix)
    }
}
