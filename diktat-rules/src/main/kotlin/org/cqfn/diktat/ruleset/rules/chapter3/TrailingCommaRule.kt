package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getCommonConfiguration
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.TRAILING_COMMA
import org.cqfn.diktat.ruleset.rules.DiktatRule

import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.COLLECTION_LITERAL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.COMMA
import com.pinterest.ktlint.core.ast.ElementType.DESTRUCTURING_DECLARATION
import com.pinterest.ktlint.core.ast.ElementType.DESTRUCTURING_DECLARATION_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.INDICES
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.core.ast.ElementType.TYPE_ARGUMENT_LIST
import com.pinterest.ktlint.core.ast.ElementType.TYPE_PARAMETER
import com.pinterest.ktlint.core.ast.ElementType.TYPE_PARAMETER_LIST
import com.pinterest.ktlint.core.ast.ElementType.TYPE_PROJECTION
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHEN_CONDITION_IN_RANGE
import com.pinterest.ktlint.core.ast.ElementType.WHEN_CONDITION_IS_PATTERN
import com.pinterest.ktlint.core.ast.ElementType.WHEN_CONDITION_WITH_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.WHEN_ENTRY
import com.pinterest.ktlint.core.ast.children
import com.pinterest.ktlint.core.ast.isPartOfComment
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.slf4j.LoggerFactory

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
    nameId,
    configRules,
    listOf(TRAILING_COMMA)
) {
    private val commonConfig = configRules.getCommonConfiguration()
    private val trailingConfig = this.configRules.getRuleConfig(TRAILING_COMMA)?.configuration ?: emptyMap()
    private val configuration by lazy {
        if (trailingConfig.isEmpty()) {
            log.warn("You have enabled TRAILING_COMMA, but rule will remain inactive until you explicitly set" +
                    " configuration options. See [available-rules.md] for possible configuration options.")
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
        val shouldFix = this.siblings(true).toList().run {
            !this.map { it.elementType }.contains(COMMA) && this.find { it.isWhiteSpaceWithNewline() || it.isPartOfComment() } != null
        }
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
                    this.addChild(LeafPsiElement(COMMA, ","), firstCommentNodeOrNull)
                }
                    ?: parent.addChild(LeafPsiElement(COMMA, ","), this.treeNext)
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
        val nameId = "abh-trailing-comma"
        private val log = LoggerFactory.getLogger(TrailingCommaRule::class.java)
        val ktVersion = KotlinVersion(1, 4)
        val whenChildrenTypes = listOf(WHEN_CONDITION_WITH_EXPRESSION, WHEN_CONDITION_IS_PATTERN, WHEN_CONDITION_IN_RANGE)
    }
}
