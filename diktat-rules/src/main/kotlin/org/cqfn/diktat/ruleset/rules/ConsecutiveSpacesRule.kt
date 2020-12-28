package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.TOO_MANY_CONSECUTIVE_SPACES

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.ENUM_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import com.pinterest.ktlint.core.ast.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafElement

/**
 * This visitor covers recommendation 3.8 of Huawei code style. It covers following recommendations:
 * 1) No spaces should be inserted for horizontal alignment
 * 2) If saveInitialFormattingForEnums is true then white spaces in enums will not be affected
 *
 */
class ConsecutiveSpacesRule(private val configRules: List<RulesConfig>) : Rule("too-many-spaces") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        val configuration = TooManySpacesRuleConfiguration(
            configRules.getRuleConfig(TOO_MANY_CONSECUTIVE_SPACES)?.configuration ?: emptyMap())

        if (node.elementType == WHITE_SPACE) {
            checkWhiteSpace(node, configuration)
        }
    }

    private fun checkWhiteSpace(node: ASTNode, configuration: TooManySpacesRuleConfiguration) {
        if (configuration.enumInitialFormatting) {
            checkWhiteSpaceEnum(node, configuration)
        } else {
            squeezeSpacesToOne(node, configuration)
        }
    }

    private fun checkWhiteSpaceEnum(node: ASTNode, configuration: TooManySpacesRuleConfiguration) {
        val isInEnum = isWhitespaceInEnum(node)

        if (!isInEnum) {
            squeezeSpacesToOne(node, configuration)
        }
    }

    private fun isWhitespaceInEnum(node: ASTNode): Boolean = node.parent(ENUM_ENTRY) != null

    private fun squeezeSpacesToOne(node: ASTNode, configuration: TooManySpacesRuleConfiguration) {
        val spaces = node.textLength
        if (spaces > configuration.numberOfSpaces && !node.isWhiteSpaceWithNewline() &&
                !node.hasEolComment()) {
            TOO_MANY_CONSECUTIVE_SPACES.warnAndFix(configRules, emitWarn, isFixMode,
                "found: $spaces. need to be: ${configuration.numberOfSpaces}", node.startOffset, node) {
                node.squeezeSpaces()
            }
        }
    }

    private fun ASTNode.hasEolComment(): Boolean = this.treeNext.elementType == EOL_COMMENT

    private fun ASTNode.squeezeSpaces() = (this as LeafElement).replaceWithText(" ")

    /**
     * [RuleConfiguration] for consecutive spaces
     */
    class TooManySpacesRuleConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Maximum allowed number of consecutive spaces (not counting indentation)
         */
        val numberOfSpaces = config["max_spaces"]?.toIntOrNull() ?: MAX_SPACES

        /**
         * Whether formatting for enums should be kept without checking
         */
        val enumInitialFormatting = config["saveInitialFormattingForEnums"]?.toBoolean() ?: false
    }

    companion object {
        private const val MAX_SPACES = 1
    }
}
