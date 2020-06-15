package com.huawei.rri.fixbot.ruleset.huawei.rules

import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.BLANK_LINE_AFTER_KDOC
import com.huawei.rri.fixbot.ruleset.huawei.utils.countSubStringOccurrences
import com.huawei.rri.fixbot.ruleset.huawei.utils.getFirstChildWithType
import com.huawei.rri.fixbot.ruleset.huawei.utils.leaveOnlyOneNewLine
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import config.rules.RulesConfig
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Formatting visitor for Kdoc:
 * 1) removing all blank lines between Kdoc and the code it's declaring
 */
class KdocFormatting : Rule("kdoc-formatting") {

    private lateinit var confiRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {

        confiRules = params.rulesConfigList!!
        isFixMode = autoCorrect
        emitWarn = emit

        val declarationTypes = setOf(CLASS, FUN, PROPERTY)

        if (declarationTypes.contains(node.elementType)) {
            val kdoc = node.getFirstChildWithType(ElementType.KDOC)
            val nodeAfterKdoc = kdoc?.treeNext
            val name = node.getFirstChildWithType(ElementType.IDENTIFIER)
            if (nodeAfterKdoc?.elementType == ElementType.WHITE_SPACE && nodeAfterKdoc.text.countSubStringOccurrences("\n") > 1) {
                BLANK_LINE_AFTER_KDOC.warnAndFix(confiRules, emitWarn, isFixMode, name!!.text, nodeAfterKdoc.startOffset) {
                    nodeAfterKdoc.leaveOnlyOneNewLine()
                }
            }
        }
    }
}
