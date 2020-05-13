package rri.fixbot.ruleset.huawei

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.FILE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import rri.fixbot.ruleset.huawei.constants.Warnings.*
import rri.fixbot.ruleset.huawei.huawei.utils.*
import java.io.File

/**
 * This visitor covers rule 1.2 of Huawei code style. It covers following rules related to a file naming:
 * 1) File must have ".kt" extension
 * 2) File must be in camel case
 * 3) File name must start with capital letter (PascalCase)
 */
class FileNaming : Rule("file-naming") {
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        params: KtLint.Params,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        if (node.elementType == FILE) {
            checkFileNaming(params, autoCorrect, emit)
        }
    }

    private fun checkFileNaming(params: KtLint.Params,
                                autoCorrect: Boolean,
                                emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        if (params.fileName != null) {
            val file = File(params.fileName!!)
            val fileName = file.name
            if (!fileName.isUpperCamelCase() || !fileName.endsWith(".kt")) {
                emit(0,
                    "${FILE_NAME_INCORRECT.text} $fileName",
                    false
                )

                if (autoCorrect) {
                    // FixMe: we can add an autocorrect here in future, but is there any purpose?
                }
            }
        }
    }
}
