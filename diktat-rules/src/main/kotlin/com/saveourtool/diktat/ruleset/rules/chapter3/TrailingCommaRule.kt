package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getCommonConfiguration
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.TRAILING_COMMA
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.isPartOfComment
import com.saveourtool.diktat.ruleset.utils.isWhiteSpaceWithNewline

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlin.KtNodeTypes.COLLECTION_LITERAL_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.DESTRUCTURING_DECLARATION
import org.jetbrains.kotlin.KtNodeTypes.DESTRUCTURING_DECLARATION_ENTRY
import org.jetbrains.kotlin.KtNodeTypes.INDICES
import org.jetbrains.kotlin.KtNodeTypes.REFERENCE_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.STRING_TEMPLATE
import org.jetbrains.kotlin.KtNodeTypes.TYPE_ARGUMENT_LIST
import org.jetbrains.kotlin.KtNodeTypes.TYPE_PARAMETER
import org.jetbrains.kotlin.KtNodeTypes.TYPE_PARAMETER_LIST
import org.jetbrains.kotlin.KtNodeTypes.TYPE_PROJECTION
import org.jetbrains.kotlin.KtNodeTypes.VALUE_ARGUMENT
import org.jetbrains.kotlin.KtNodeTypes.VALUE_ARGUMENT_LIST
import org.jetbrains.kotlin.KtNodeTypes.VALUE_PARAMETER
import org.jetbrains.kotlin.KtNodeTypes.VALUE_PARAMETER_LIST
import org.jetbrains.kotlin.KtNodeTypes.WHEN_CONDITION_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.WHEN_CONDITION_IN_RANGE
import org.jetbrains.kotlin.KtNodeTypes.WHEN_CONDITION_IS_PATTERN
import org.jetbrains.kotlin.KtNodeTypes.WHEN_ENTRY
import org.jetbrains.kotlin.com.intellij.lang.ASTFactory
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.KDOC
import org.jetbrains.kotlin.lexer.KtTokens.BLOCK_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.COMMA
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * [1] Enumerations (In another rule)
 * [2] Value arguments
 * [3] Class properties and parameters
 * [4] Function value parameters
 * [5] Parameters with optional type (including setters)
 * [6] Indexing suffix
 * [7] Lambda parameters
 * [8] when entry
 * [9] Collection literals (in annotations)Type arguments
 * [10] Type arguments
 * [11] Type parameters
 * [12] Destructuring declarations
 */
@Suppress("TOO_LONG_FUNCTION")
class TrailingCommaRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(TRAILING_COMMA)
) {
    private val commonConfig = configRules.getCommonConfiguration()
    private val trailingConfig = this.configRules.getRuleConfig(TRAILING_COMMA)?.configuration ?: emptyMap()
    private val configuration by lazy {
        if (trailingConfig.isEmpty()) {
            log.warn {
                "You have enabled TRAILING_COMMA, but rule will remain inactive until you explicitly set" +
                        " configuration options. See [available-rules.md] for possible configuration options."
            }
        }
        TrailingCommaConfiguration(trailingConfig)
    }

    override fun logic(node: ASTNode) {
        if (commonConfig.kotlinVersion >= ktVersion) {
            val (type, config) = when (node.elementType) {
                VALUE_ARGUMENT_LIST -> Pair(VALUE_ARGUMENT, configuration.getParam("valueArgument"))
                VALUE_PARAMETER_LIST -> Pair(VALUE_PARAMETER, configuration.getParam("valueParameter"))
                INDICES -> Pair(REFERENCE_EXPRESSION, configuration.getParam("referenceExpression"))
                WHEN_ENTRY -> {
                    val lastChildType = node
                        .children()
                        .toList()
                        .findLast { it.elementType in whenChildrenTypes }
                        ?.elementType
                    Pair(lastChildType, configuration.getParam("whenConditions"))
                }
                COLLECTION_LITERAL_EXPRESSION -> Pair(STRING_TEMPLATE, configuration.getParam("collectionLiteral"))
                TYPE_ARGUMENT_LIST -> Pair(TYPE_PROJECTION, configuration.getParam("typeArgument"))
                TYPE_PARAMETER_LIST -> Pair(TYPE_PARAMETER, configuration.getParam("typeParameter"))
                DESTRUCTURING_DECLARATION -> Pair(
                    DESTRUCTURING_DECLARATION_ENTRY,
                    configuration.getParam("destructuringDeclaration")
                )
                else -> return
            }
            val astNode = node
                .children()
                .toList()
                .lastOrNull { it.elementType == type }
            astNode?.checkTrailingComma(config)
        }
    }

    private fun ASTNode.checkTrailingComma(config: Boolean) {
        val noCommaInSiblings = siblings(true).toSet()
            .let { siblings ->
                siblings.none { it.elementType == COMMA } && siblings.any { it.isWhiteSpaceWithNewline() || it.isPartOfComment() }
            }
        val noCommaInChildren = children().none { it.elementType == COMMA }
        val shouldFix = noCommaInSiblings && noCommaInChildren

        if (shouldFix && config) {
            // we should write type of node in warning, to make it easier for user to find the parameter
            TRAILING_COMMA.warnAndFix(configRules, emitWarn, isFixMode, "after ${this.elementType}: ${this.text}", this.startOffset, this) {
                val parent = this.treeParent

                // In case, when we got VALUE_PARAMETER, it may contain comments, which follows the actual parameter and all of them are actually in the same node
                // Ex: `class A(val a: Int, val b: Int  // comment)`
                // `val b: Int  // comment` --> the whole expression is VALUE_PARAMETER
                // So, in this case we must insert comma before the comment, in other cases we will insert it after current node
                val comments = listOf(EOL_COMMENT, BLOCK_COMMENT, KDOC)
                val firstCommentNodeOrNull = if (this.elementType == VALUE_PARAMETER) this.children().firstOrNull { it.elementType in comments } else null
                firstCommentNodeOrNull?.let {
                    this.addChild(ASTFactory.leaf(COMMA, ","), it)
                }
                    ?: parent.addChild(ASTFactory.leaf(COMMA, ","), this.treeNext)
            }
        }
    }

    /**
     * Configuration for trailing comma
     */
    class TrailingCommaConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * @param name parameters name
         * @return param based on its name
         */
        fun getParam(name: String) = config[name]?.toBoolean() ?: false
    }

    companion object {
        private val log = KotlinLogging.logger {}
        const val NAME_ID = "trailing-comma"
        val ktVersion = KotlinVersion(1, 4)
        val whenChildrenTypes = listOf(WHEN_CONDITION_EXPRESSION, WHEN_CONDITION_IS_PATTERN, WHEN_CONDITION_IN_RANGE)
    }
}
