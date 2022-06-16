package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.COMPLEX_BOOLEAN_EXPRESSION
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.findAllNodesWithCondition
import org.cqfn.diktat.ruleset.utils.logicalInfixMethods
import com.bpodgursky.jbool_expressions.And
import com.bpodgursky.jbool_expressions.Expression
import com.bpodgursky.jbool_expressions.NExpression
import com.bpodgursky.jbool_expressions.Or
import com.bpodgursky.jbool_expressions.options.ExprOptions
import com.bpodgursky.jbool_expressions.parsers.ExprParser
import com.bpodgursky.jbool_expressions.parsers.TokenMapper
import com.bpodgursky.jbool_expressions.rules.DeMorgan
import com.bpodgursky.jbool_expressions.rules.Rule
import com.bpodgursky.jbool_expressions.rules.RuleList
import com.bpodgursky.jbool_expressions.rules.RulesHelper
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CONDITION
import com.pinterest.ktlint.core.ast.ElementType.PARENTHESIZED
import com.pinterest.ktlint.core.ast.ElementType.PREFIX_EXPRESSION
import com.pinterest.ktlint.core.ast.isLeaf
import com.pinterest.ktlint.core.ast.isPartOfComment
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Rule that checks if the boolean expression can be simplified.
 */
class BooleanExpressionsRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(COMPLEX_BOOLEAN_EXPRESSION)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == CONDITION) {
            checkBooleanExpression(node)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun checkBooleanExpression(node: ASTNode) {
        // This class is used to assign a variable name for every elementary boolean expression. It is required for jbool to operate.
        val expressionsReplacement = ExpressionsReplacement()
        val correctedExpression = formatBooleanExpressionAsString(node, expressionsReplacement)
        if (expressionsReplacement.isEmpty()) {
            // this happens, if we haven't found any expressions that can be simplified
            return
        }

        // If there are method calls in conditions
        val expr: Expression<String> = try {
            ExprParser.parse(correctedExpression, expressionsReplacement.orderedTokenMapper)
        } catch (exc: RuntimeException) {
            if (exc.message?.startsWith("Unrecognized!") == true) {
                // this comes up if there is an unparsable expression (jbool doesn't have own exception type). For example a.and(b)
                return
            } else {
                throw exc
            }
        }
        val simplifiedExpression = RulesHelper.applySet(expr, allRules(), ExprOptions.noCaching())
        if (expr != simplifiedExpression) {
            COMPLEX_BOOLEAN_EXPRESSION.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                fixBooleanExpression(node, simplifiedExpression, expressionsReplacement)
            }
        }
    }

    /**
     * Converts a complex boolean expression into a string representation, mapping each elementary expression to a letter token.
     * These tokens are collected into [expressionsReplacement].
     * For example:
     * ```
     * (a > 5 && b != 2) -> A & B
     * (a > 5 || false) -> A | false
     * (a > 5 || x.foo()) -> A | B
     * ```
     *
     * @param node
     * @param expressionsReplacement a special class for replacements expression->token
     * @return formatted string representation of expression
     */
    @Suppress("UnsafeCallOnNullableType", "ForbiddenComment")
    internal fun formatBooleanExpressionAsString(node: ASTNode, expressionsReplacement: ExpressionsReplacement): String {
        val (booleanBinaryExpressions, otherBinaryExpressions) = node.collectElementaryExpressions()
        val logicalExpressions = otherBinaryExpressions.filter { otherBinaryExpression ->
            // keeping only boolean expressions, keeping things like `a + b < 6` and excluding `a + b`
            (otherBinaryExpression.psi as KtBinaryExpression).operationReference.text in logicalInfixMethods &&
                // todo: support xor; for now skip all expressions that are nested in xor
                otherBinaryExpression.parents()
                    .takeWhile { it != node }
                    .none { (it.psi as? KtBinaryExpression)?.isXorExpression() ?: false }
        }
        // Boolean expressions like `a > 5 && b < 7` or `x.isEmpty() || (y.isNotEmpty())` we convert to individual parts.
        val elementaryBooleanExpressions = booleanBinaryExpressions
            .map { it.psi as KtBinaryExpression }
            .flatMap { listOf(it.left!!.node, it.right!!.node) }
            .map {
                // remove parentheses around expression, if there are any
                it.removeAllParentheses()
            }
            .filterNot {
                // finally, if parts are binary expressions themselves, they should be present in our lists and we will process them later.
                it.elementType == BINARY_EXPRESSION ||
                    // !(a || b) should be skipped too, `a` and `b` should be present later
                    (it.psi as? KtPrefixExpression)?.lastChild
                        ?.node
                        ?.removeAllParentheses()
                        ?.elementType == BINARY_EXPRESSION ||
                    // `true` and `false` are valid tokens for jBool, so we keep them.
                    it.text == "true" || it.text == "false"
            }
        (logicalExpressions + elementaryBooleanExpressions).forEach { expression ->
            expressionsReplacement.addExpression(expression)
        }
        // Prepare final formatted string
        // At first, substitute all elementary expressions with variables
        val correctedExpression = expressionsReplacement.replaceExpressions(node.textWithoutComments())
        // jBool library is using & as && and | as ||
        return "(${correctedExpression
            .replace("&&", "&")
            .replace("||", "|")})"
    }

    /**
     * Split the complex expression into elementary parts
     */
    private fun ASTNode.collectElementaryExpressions() = this
        .findAllNodesWithCondition { astNode ->
            astNode.elementType == BINARY_EXPRESSION &&
                // filter out boolean conditions in nested lambdas, e.g. `if (foo.filter { a && b })`
                (astNode == this || astNode.parents().takeWhile { it != this }
                    .all { it.elementType in setOf(BINARY_EXPRESSION, PARENTHESIZED, PREFIX_EXPRESSION) })
        }
        .partition {
            val operationReferenceText = (it.psi as KtBinaryExpression).operationReference.text
            operationReferenceText == "&&" || operationReferenceText == "||"
        }

    private fun ASTNode.removeAllParentheses(): ASTNode {
        val result = (this.psi as? KtParenthesizedExpression)?.expression?.node ?: return this
        return result.removeAllParentheses()
    }

    private fun ASTNode.textWithoutComments() = findAllNodesWithCondition(withSelf = false) {
        it.isLeaf()
    }
        .filterNot { it.isPartOfComment() }
        .joinToString(separator = "") { it.text }
        .replace("\n", " ")

    private fun fixBooleanExpression(
        node: ASTNode,
        simplifiedExpr: Expression<String>,
        expressionsReplacement: ExpressionsReplacement
    ) {
        val correctKotlinBooleanExpression = simplifiedExpr
            .toString()
            .replace("&", "&&")
            .replace("|", "||")
            .removePrefix("(")
            .removeSuffix(")")

        node.replaceChild(node.firstChildNode,
            KotlinParser().createNode(expressionsReplacement.restoreFullExpression(correctKotlinBooleanExpression)))
    }

    private fun KtBinaryExpression.isXorExpression() = operationReference.text == "xor"

    /**
     * A special class to replace expressions (and restore it back)
     * Note: mapping is String to Char(and Char to Char) actually, but will keep it as String for simplicity
     */
    internal inner class ExpressionsReplacement {
        private val expressionToToken: HashMap<String, String> = LinkedHashMap()
        private val tokenToOrderedToken: HashMap<String, String> = HashMap()

        /**
         * TokenMapper for first call ExprParser which remembers the order of expression.
         */
        val orderedTokenMapper: TokenMapper<String> = TokenMapper { name -> getLetter(tokenToOrderedToken, name) }

        /**
         * Returns <tt>true</tt> if this object contains no replacements.
         *
         * @return <tt>true</tt> if this object contains no replacements
         */
        fun isEmpty(): Boolean = expressionToToken.isEmpty()

        /**
         * Returns the number of replacements in this object.
         *
         * @return the number of replacements in this object
         */
        fun size(): Int = expressionToToken.size

        /**
         * Register an expression for further replacement
         *
         * @param expressionAstNode astNode which contains boolean expression
         */
        fun addExpression(expressionAstNode: ASTNode) {
            val expressionText = expressionAstNode.textWithoutComments()
            // support case when `boolean_expression` matches to `!boolean_expression`
            val expression = if (expressionText.startsWith('!')) expressionText.substring(1) else expressionText
            getLetter(expressionToToken, expression)
        }

        /**
         * Replaces registered expressions in provided expression
         *
         * @param fullExpression full boolean expression in kotlin
         * @return full expression in jbool format
         */
        @Suppress("UnsafeCallOnNullableType")
        fun replaceExpressions(fullExpression: String): String {
            var resultExpression = fullExpression
            expressionToToken.keys
                .sortedByDescending { it.length }
                .forEach { refExpr ->
                    resultExpression = resultExpression.replace(refExpr, expressionToToken[refExpr]!!)
                }
            return resultExpression
        }

        /**
         * Restores full expression by replacing tokens and restoring the order
         *
         * @param fullExpression full boolean expression in jbool format
         * @return full boolean expression in kotlin
         */
        fun restoreFullExpression(fullExpression: String): String {
            // restore order
            var resultExpression = fullExpression
            tokenToOrderedToken.values.forEachIndexed { index, value ->
                resultExpression = resultExpression.replace(value, "%${index + 1}\$s")
            }
            resultExpression = resultExpression.format(args = tokenToOrderedToken.keys.toTypedArray())
            // restore expression
            expressionToToken.values.forEachIndexed { index, value ->
                resultExpression = resultExpression.replace(value, "%${index + 1}\$s")
            }
            resultExpression = resultExpression.format(args = expressionToToken.keys.toTypedArray())
            return resultExpression
        }

        private fun getLetter(letters: HashMap<String, String>, key: String) = letters
            .computeIfAbsent(key) {
                ('A'.code + letters.size).toChar().toString()
            }
    }

    /**
     * Rule that checks that the expression can be simplified by distributive law.
     * Distributive law - A && B || A && C -> A && (B || C) or (A || B) && (A || C) -> A || (B && C)
     */
    private class DistributiveLaw<K> : Rule<NExpression<K>, K>() {
        override fun applyInternal(input: NExpression<K>?, options: ExprOptions<K>?): Expression<K> {
            requireNotNull(input) {
                "input expression is null"
            }
            val exprFactory = requireNotNull(options?.exprFactory) {
                "jbool issue: Failed to get exprFactory"
            }
            val orExpressionCreator: ExpressionCreator<K> = { expressions -> exprFactory.or(expressions.toTypedArray()) }
            val andExpressionCreator: ExpressionCreator<K> = { expressions -> exprFactory.and(expressions.toTypedArray()) }
            return when (input) {
                is And -> applyInternal(input, orExpressionCreator, andExpressionCreator)
                is Or -> applyInternal(input, andExpressionCreator, orExpressionCreator)
                else -> throw UnsupportedOperationException("Not supported input expression: ${input.exprType}")
            }
        }

        private fun applyInternal(input: NExpression<K>,
                                  upperExpressionCreator: ExpressionCreator<K>,
                                  innerExpressionCreator: ExpressionCreator<K>): Expression<K> {
            val commonExpression = requireNotNull(findCommonExpression(input.children)) {
                "Common expression is not found for $input"
            }
            return upperExpressionCreator(
                listOf(commonExpression,
                    innerExpressionCreator(
                        input.expressions.map { excludeChild(it, upperExpressionCreator, commonExpression) }
                    )))
        }

        private fun excludeChild(expression: Expression<K>,
                                 expressionCreator: ExpressionCreator<K>,
                                 childToExclude: Expression<K>): Expression<K> {
            val leftChildren = expression.children.filterNot { it.equals(childToExclude) }
            return if (leftChildren.size == 1) {
                leftChildren.first()
            } else {
                expressionCreator(leftChildren)
            }
        }

        /**
         * Checks the input expression
         */
        override fun isApply(inputNullable: Expression<K>?): Boolean = inputNullable?.let { input ->
            when (input) {
                is And -> isApplicable<And<K>, Or<K>>(input)
                is Or -> isApplicable<Or<K>, And<K>>(input)
                else -> false
            }
        } ?: false

        private inline fun <E : NExpression<K>, reified C : NExpression<K>> isApplicable(input: E): Boolean {
            val children = input.children ?: return false
            if (children.size < 2 || children.any { it !is C }) {
                return false
            }
            return findCommonExpression(children) != null
        }

        private fun findCommonExpression(children: List<Expression<K>>): Expression<K>? = children.drop(1)
            .fold(children[0].children) { commons, child ->
                commons.filter { childResult ->
                    child.children.any { it.equals(childResult) }
                }
            }.firstOrNull()
    }

    companion object {
        const val NAME_ID = "boolean-expressions-rule"

        private fun <K> allRules(): RuleList<K> {
            val rules: MutableList<Rule<*, K>> = ArrayList(RulesHelper.simplifyRules<K>().rules)
            rules.add(DeMorgan())
            rules.add(DistributiveLaw())
            return RuleList(rules)
        }
    }
}

typealias ExpressionCreator<K> = (List<Expression<K>?>) -> Expression<K>
