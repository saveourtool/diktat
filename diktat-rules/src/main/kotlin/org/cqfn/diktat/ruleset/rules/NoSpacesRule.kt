package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.ENUM_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import com.pinterest.ktlint.core.ast.parent
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.cqfn.diktat.ruleset.constants.Warnings.TOO_MANY_SPACES
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafElement

class NoSpacesRule : Rule("no-spaces") {

    companion object {
        private const val MAX_SPACES = 1
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

        val configuration = NoSpacesRuleConfiguration(
                configRules.getRuleConfig(TOO_MANY_SPACES)?.configuration ?: mapOf())

        if (node.elementType == WHITE_SPACE) {
            checkWhiteSpace(node, configuration)
        }

    }


    private fun checkWhiteSpace(node: ASTNode, configuration: NoSpacesRuleConfiguration) {

        if (configuration.enumInitialFormatting) {
            checkWhiteSpaceEnum(node, configuration)
        } else {
            squeezeSpacesToOne(node, configuration)
        }

    }

    private fun checkWhiteSpaceEnum(node: ASTNode, configuration: NoSpacesRuleConfiguration) {
        val isInEnum = isWhitespaceInEnum(node)

        if (isInEnum) {
            squeezeSpacesToOne(node, configuration)
        }
    }

    private fun isWhitespaceInEnum(node: ASTNode): Boolean {
        node.parent({it.elementType == CLASS_BODY})
                ?.parent({it.elementType == CLASS})
                ?.findChildByType(MODIFIER_LIST)
                ?.findChildByType(ENUM_KEYWORD)
                ?: return true

        return false
    }

    private fun squeezeSpacesToOne(node: ASTNode, configuration: NoSpacesRuleConfiguration) {

        val spaces = node.textLength
        if (spaces > configuration.numberOfSpaces && !node.isWhiteSpaceWithNewline()) {
            TOO_MANY_SPACES.warnAndFix(configRules, emitWarn, isFixMode,
                    "found: $spaces. need to be: ${configuration.numberOfSpaces}", node.startOffset) {
                node.squeezeSpaces()
            }
        }
    }


    private fun ASTNode.squeezeSpaces() {
        (this as LeafElement).replaceWithText(" ")
    }


    class NoSpacesRuleConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val numberOfSpaces = config["max_spaces"]?.toIntOrNull() ?: MAX_SPACES
        val enumInitialFormatting = config["saveInitialFormattingForEnums"]?.toBoolean() ?: false
    }

}
