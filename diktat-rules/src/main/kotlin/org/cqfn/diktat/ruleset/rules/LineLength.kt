package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_LIST
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TEXT
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.LONG_LINE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import java.io.IOException
import java.net.URL

@Suppress("ForbiddenComment")
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
                configRules.getRuleConfig(LONG_LINE)?.configuration ?: mapOf())
        if (node.elementType == FILE) {
            node.getChildren(null).forEach {
                if (it.elementType != PACKAGE_DIRECTIVE || it.elementType != IMPORT_LIST)
                    checkLength(it, configuration)
            }
        }
    }

    private fun checkLength(node: ASTNode, configuration: LineLengthConfiguration) {
        var offset = 0
        node.text.lines().forEach {
            if (it.length > configuration.lineLength) {
                val newNode = node.psi.findElementAt(offset + it.length - 1)!!.node
                if (newNode.elementType == KDOC_TEXT)
                    checkKDoc(newNode, configuration)
                else
                    LONG_LINE.warnAndFix(configRules, emitWarn, isFixMode,
                            "max line length ${configuration.lineLength}, but was ${it.length}",
                            offset.plus(node.startOffset)) {}
            }
            offset += it.length + 1
        }
    }

    // fixme json method
    private fun checkKDoc(node: ASTNode, configuration: LineLengthConfiguration) {
        if (node.text.length > configuration.lineLength) {
            try {
                URL(node.text).toURI()
            } catch (e: IOException) {
                LONG_LINE.warnAndFix(configRules, emitWarn, isFixMode,
                        "max line length ${configuration.lineLength}, but was ${node.text.length}",
                        node.startOffset) {}
            }
        }
    }

    class LineLengthConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val lineLength = config["lineLength"]?.toLongOrNull() ?: MAX_LENGTH
    }
}
