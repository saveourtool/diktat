package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_LIST
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.KDOC_SECTION
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TEXT
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.LONG_LINE
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import java.io.IOException
import java.net.URL


class LineLength : Rule("line-length") {

    companion object {
        const val MAX_LENGTH = 120L
    }

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var fileName: String? = null
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        fileName = params.fileName
        emitWarn = emit
        isFixMode = autoCorrect

        val configuration = LineLengthConfiguration(
                configRules.getRuleConfig(LONG_LINE)?.configuration ?: mapOf()
        )
        if (node.elementType == FILE) {
            node.getChildren(null).forEach {
                if (it.elementType != PACKAGE_DIRECTIVE || it.elementType != IMPORT_LIST)
                    checkLength(it, configuration)
            }
        }
    }

    private fun checkLength(node: ASTNode, configuration: LineLengthConfiguration) {
        node.getChildren(null).forEach {
            if (it.elementType == KDOC) {
                checkKDoc(it, configuration)
            } else {
                it.text.split("\n").forEach { textNode ->
                    if (textNode.length > configuration.lineLength)
                        LONG_LINE.warnAndFix(configRules, emitWarn, isFixMode, textNode, it.startOffset.plus(it.treeParent.startOffset)) {}
                }
            }
        }
    }

    /**
     * JSON not supported
     */
    private fun checkKDoc(node: ASTNode, configuration: LineLengthConfiguration) {
        val nodesList = node.findChildByType(KDOC_SECTION)?.findAllNodesWithSpecificType(KDOC_TEXT)
        nodesList.let { nodes ->
            nodes!!.forEach {
                try {
                    URL(it.text).toURI()
                } catch (e: IOException) {
                    if (it.text.length > configuration.lineLength)
                        LONG_LINE.warnAndFix(configRules, emitWarn, isFixMode, it.text, it.treeParent.startOffset) {}
                }
            }
        }
    }

    class LineLengthConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val lineLength = config["lineLength"]?.toLongOrNull() ?: MAX_LENGTH
    }
}
