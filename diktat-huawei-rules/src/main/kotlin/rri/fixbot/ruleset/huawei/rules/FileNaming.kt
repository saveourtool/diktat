package rri.fixbot.ruleset.huawei.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import config.rules.RulesConfig
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import rri.fixbot.ruleset.huawei.constants.Warnings.*
import rri.fixbot.ruleset.huawei.utils.getAllChildrenWithType
import rri.fixbot.ruleset.huawei.utils.getFirstChildWithType
import rri.fixbot.ruleset.huawei.utils.isPascalCase
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
        val VALID_EXTENSIONS = listOf(".kt")
    }

    private var confiRules: List<RulesConfig>? = emptyList()

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        params: KtLint.Params,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        confiRules = params.rulesConfigList!!

        if (node.elementType == FILE) {
            checkFileNaming(params, autoCorrect, emit)
            checkClassNameMatchesWithFile(node, params, autoCorrect, emit)
        }
    }

    private fun checkFileNaming(params: KtLint.Params,
                                autoCorrect: Boolean,
                                emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        if (params.fileName != null) {
            val (name, extension) = getFileParts(params)
            if (!name.isPascalCase() || !VALID_EXTENSIONS.contains(extension)) {
                emit(0,
                    "${FILE_NAME_INCORRECT.warnText} $name$extension",
                    false
                )

                if (autoCorrect) {
                    // FixMe: we can add an autocorrect here in future, but is there any purpose to change file or class name?
                }
            }
        }
    }

    private fun checkClassNameMatchesWithFile(fileLevelNode: ASTNode,
                                              params: KtLint.Params,
                                              autoCorrect: Boolean,
                                              emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        if (params.fileName != null) {
            val (fileName, extension) = getFileParts(params)
            val classes = fileLevelNode.getAllChildrenWithType(CLASS)
            if (classes.size == 1) {
                val className = classes[0].getFirstChildWithType(IDENTIFIER)!!.text
                if (className != fileName) {
                    emit(0,
                        "${FILE_NAME_MATCH_CLASS.warnText} $fileName$extension vs $className",
                        false
                    )

                    if (autoCorrect) {
                        // FixMe: we can add an autocorrect here in future, but is there any purpose to change file name?
                    }
                }
            }
        }
    }

    private fun getFileParts(params: KtLint.Params): Pair<String, String> {
        val file = File(params.fileName!!)
        val fileNameWithoutSuffix = file.name.replace(Regex("\\..*"), "")
        val fileNameSuffix = file.name.replace(fileNameWithoutSuffix, "")

        return Pair(fileNameWithoutSuffix, fileNameSuffix)
    }
}
