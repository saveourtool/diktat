package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.COMPLEX_BOOLEAN_EXPRESSION
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.findAllNodesWithCondition
import org.cqfn.diktat.ruleset.utils.findLeafWithSpecificType
import org.cqfn.diktat.ruleset.utils.logicalInfixMethods

import com.bpodgursky.jbool_expressions.Expression
import com.bpodgursky.jbool_expressions.options.ExprOptions
import com.bpodgursky.jbool_expressions.parsers.ExprParser
import com.bpodgursky.jbool_expressions.parsers.TokenMapper
import com.bpodgursky.jbool_expressions.rules.RulesHelper
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CONDITION
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.PARENTHESIZED
import com.pinterest.ktlint.core.ast.ElementType.PREFIX_EXPRESSION
import com.pinterest.ktlint.core.ast.isLeaf
import com.pinterest.ktlint.core.ast.isPartOfComment
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.psiUtil.parents

import java.lang.RuntimeException

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
            ExprParser.parse(correctedExpression, expressionsReplacement.getTokenMapper())
        } catch (exc: RuntimeException) {
            if (exc.message?.startsWith("Unrecognized!") == true) {
                // this comes up if there is an unparsable expression (jbool doesn't have own exception type). For example a.and(b)
                return
            } else {
                throw exc
            }
        }
        val distributiveLawString = checkDistributiveLaw(expr, expressionsReplacement, node)
        val simplifiedExpression = distributiveLawString?.let {
            ExprParser.parse(distributiveLawString)
        }
            ?: RulesHelper.applySet(expr, RulesHelper.demorganRules(), ExprOptions.noCaching())
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
        val logicalExpressions = otherBinaryExpressions.filter {
            // keeping only boolean expressions, keeping things like `a + b < 6` and excluding `a + b`
            (it.psi as KtBinaryExpression).operationReference.text in logicalInfixMethods &&
                    // todo: support xor; for now skip all expressions that are nested in xor
                    it.parents().takeWhile { it != node }.none { (it.psi as? KtBinaryExpression)?.isXorExpression() ?: false }
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
                                (it.psi as? KtPrefixExpression)?.lastChild?.node?.removeAllParentheses()?.elementType == BINARY_EXPRESSION ||
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
        var correctKotlinBooleanExpression = simplifiedExpr
            .toString()
            .replace("&", "&&")
            .replace("|", "||")

        if (simplifiedExpr.toString().first() == '(' && simplifiedExpr.toString().last() == ')') {
            correctKotlinBooleanExpression = correctKotlinBooleanExpression
                .drop(1)
                .dropLast(1)
        }
        correctKotlinBooleanExpression = expressionsReplacement.restoreFullExpression(correctKotlinBooleanExpression)

        node.replaceChild(node.firstChildNode, KotlinParser().createNode(correctKotlinBooleanExpression))
    }

    /**
     * Checks if boolean expression can be simplified with distributive law.
     *
     * @return String? null if it cannot be simplified. Simplified string otherwise.
     */
    private fun checkDistributiveLaw(
        expr: Expression<String>,
        expressionsReplacement: ExpressionsReplacement,
        node: ASTNode
    ): String? {
        // checking that expression can be considered as distributive law
        val commonDistributiveOperand = getCommonDistributiveOperand(node, expr.toString(), expressionsReplacement)?.toString() ?: return null
        val correctSymbolsSequence = expressionsReplacement.getTokens().toMutableList()
        correctSymbolsSequence.remove(commonDistributiveOperand)
        correctSymbolsSequence.add(0, commonDistributiveOperand)
        val expressionsLogicalOperator = expr.toString().first { it == '&' || it == '|' }
        // we return expression depending on second operator
        return returnNeededDistributiveExpression(expressionsLogicalOperator, correctSymbolsSequence)
    }

    /**
     * Returns correct result string in distributive law
     */
    private fun returnNeededDistributiveExpression(firstLogicalOperator: Char, symbols: List<String>): String {
        val secondSymbol = if (firstLogicalOperator == '&') '|' else '&'  // this is used to alter symbols
        val resultString = StringBuilder()
        symbols.forEachIndexed { index, symbol ->
            if (index == 0) {
                resultString.append("$symbol $firstLogicalOperator (")
            } else {
                resultString.append("$symbol $secondSymbol ")
            }
        }
        // remove last space and last operate
        return StringBuilder(resultString.dropLast(2)).append(")").toString()
    }

    /**
     * Method that checks that the expression can be simplified by distributive law.
     * Distributive law - A && B || A && C -> A && (B || C) or (A || B) && (A || C) -> A || (B && C)
     *
     * @return common operand for distributed law
     */
    private fun getCommonDistributiveOperand(
        node: ASTNode,
        expression: String,
        expressionsReplacement: ExpressionsReplacement
    ): Char? {
        val operationSequence = expression.filter { it == '&' || it == '|' }
        val numberOfOperationReferences = operationSequence.length
        // There should be three operands and three operation references in order to consider the expression
        // Moreover the operation references between operands should alternate.
        if (expressionsReplacement.size() < DISTRIBUTIVE_LAW_MIN_EXPRESSIONS ||
                numberOfOperationReferences < DISTRIBUTIVE_LAW_MIN_OPERATIONS ||
                !isSequenceAlternate(operationSequence)) {
            return null
        }
        return if (operationSequence.first() == '&') {
            getCommonOperand(expression, '|', '&')
        } else {
            // this is done for excluding A || B && A || C without parenthesis.
            val parenthesizedExpressions = node.findAllNodesWithCondition { it.elementType == PARENTHESIZED }
            parenthesizedExpressions.forEach {
                it.findLeafWithSpecificType(OPERATION_REFERENCE) ?: run {
                    return null
                }
            }
            getCommonOperand(expression, '&', '|')
        }
    }

    private fun isSequenceAlternate(seq: String) = seq.zipWithNext().all { it.first != it.second }

    /**
     * This method returns common operand in distributive law.
     * We need common operand for special case, when the first expression is not common.
     * For example: (some != null && a) || (a && c) || (a && d). When the expressions are mapped to `char`s, `some != null` points to `A` character
     */
    private fun getCommonOperand(
        expression: String,
        firstSplitDelimiter: Char,
        secondSplitDelimiter: Char
    ): Char? {
        val expressions = expression.split(firstSplitDelimiter)
        val listOfPairs: MutableList<List<String>> = mutableListOf()
        expressions.forEach { expr ->
            listOfPairs.add(expr.filterNot { it == ' ' || it == '(' || it == ')' }.split(secondSplitDelimiter))
        }
        val firstOperands = listOfPairs.first()
        listOfPairs.removeFirst()
        return when {
            listOfPairs.all { it.contains(firstOperands.first()) } -> firstOperands.first().first()
            listOfPairs.all { it.contains(firstOperands.last()) } -> firstOperands.last().first()
            else -> null
        }
    }

    private fun KtBinaryExpression.isXorExpression() = operationReference.text == "xor"

    /**
     * A special class to replace expressions (and restore it back)
     * Note: mapping is String to Char(and Char to Char) actually, but will keep it as String for simplicity
     */
    internal inner class ExpressionsReplacement {
        private val expressionToToken: HashMap<String, String> = LinkedHashMap()
        private val tokenToOrderedToken: HashMap<String, String>  = HashMap()
        private val orderedTokenMapper: TokenMapper<String> = TokenMapper { name -> getLetter(tokenToOrderedToken, name) }

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
         * Returns the TokenMapper for first call ExprParser which remembers the order of expression.
         *
         * @return the TokenMapper for first call ExprParser which remembers the order of expression
         */
        fun getTokenMapper(): TokenMapper<String> = orderedTokenMapper

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
         *
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
         *
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

        /**
         * Returns collection of token are used to construct full expression in jbool format.
         *
         * @return collection of token are used to construct full expression in jbool format
         */
        fun getTokens(): Collection<String> {
            return expressionToToken.values
        }

        private fun getLetter(letters: HashMap<String, String>, key: String) = letters
            .computeIfAbsent(key) {
                ('A'.code + letters.size).toChar().toString()
            }
    }

    companion object {
        const val DISTRIBUTIVE_LAW_MIN_EXPRESSIONS = 3
        const val DISTRIBUTIVE_LAW_MIN_OPERATIONS = 3
        const val NAME_ID = "acm-boolean-expressions-rule"
    }
}
