package org.cqfn.diktat.ruleset.rules.chapter3

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
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.COMPLEX_BOOLEAN_EXPRESSION
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.findAllNodesWithCondition
import org.cqfn.diktat.ruleset.utils.findLeafWithSpecificType
import org.cqfn.diktat.ruleset.utils.logicalInfixMethods
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
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
        val expressionReplacement = ExpressionReplacement()
        val correctedExpression = formatBooleanExpressionAsString(node, expressionReplacement)
        if (expressionReplacement.isEmpty()) {
            // this happens, if we haven't found any expressions that can be simplified
            return
        }

        val orderTokenMapper = OrderTokenMapper()
        // If there are method calls in conditions
        val expr: Expression<String> = try {
            ExprParser.parse(correctedExpression, orderTokenMapper)
        } catch (exc: RuntimeException) {
            if (exc.message?.startsWith("Unrecognized!") == true) {
                // this comes up if there is an unparsable expression (jbool doesn't have own exception type). For example a.and(b)
                return
            } else {
                throw exc
            }
        }
        val distributiveLawString = checkDistributiveLaw(expr, expressionReplacement, node)
        val simplifiedExpression = distributiveLawString?.let {
            ExprParser.parse(distributiveLawString)
        }
            ?: RulesHelper.applySet(expr, RulesHelper.demorganRules(), ExprOptions.noCaching())
        if (expr != simplifiedExpression) {
            COMPLEX_BOOLEAN_EXPRESSION.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                fixBooleanExpression(node, simplifiedExpression, expressionReplacement, orderTokenMapper)
            }
        }
    }

    /**
     * Converts a complex boolean expression into a string representation, mapping each elementary expression to a letter token.
     * These tokens are collected into [mapOfExpressionToChar].
     * For example:
     * ```
     * (a > 5 && b != 2) -> A & B
     * (a > 5 || false) -> A | false
     * (a > 5 || x.foo()) -> A | B
     * ```
     *
     * @param node
     * @param mapOfExpressionToChar a mutable map for expression->token
     * @return formatted string representation of expression
     */
    @Suppress("UnsafeCallOnNullableType", "ForbiddenComment")
    internal fun formatBooleanExpressionAsString(node: ASTNode, expressionReplacement: ExpressionReplacement): String {
        val isLogicalExpression = { it: ASTNode ->
            // keeping only boolean expressions, keeping things like `a + b < 6` and excluding `a + b`
            (it.psi as KtBinaryExpression).operationReference.text in logicalInfixMethods &&
                    // todo: support xor; for now skip all expressions that are nested in xor
                    it.parents().takeWhile { it != node }
                        .none { (it.psi as? KtBinaryExpression)?.isXorExpression() ?: false }
        }
        node.collectElementaryExpressions()
            .flatMap {
                if (it.isBooleanBinaryExpression()) {
                    it.extractElementaryBooleanExpressions()
                } else {
                    if (isLogicalExpression(it)) listOf(it) else listOf()
                }
            }
            .forEach { expression ->
                expressionReplacement.addExpression(expression)
            }
        // Prepare final formatted string
        var correctedExpression = node.textWithoutComments()
        // At first, substitute all elementary expressions with variables
        correctedExpression = expressionReplacement.replaceExpressions(correctedExpression)
        // jBool library is using & as && and | as ||
        return "(${
            correctedExpression
                .replace("&&", "&")
                .replace("||", "|")
        })"
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

    private fun ASTNode.isBooleanBinaryExpression() = setOf("&&", "||")
        .contains((this.psi as KtBinaryExpression).operationReference.text)

    // Boolean expressions like `a > 5 && b < 7` or `x.isEmpty() || (y.isNotEmpty())` we convert to individual parts.
    private fun ASTNode.extractElementaryBooleanExpressions() = listOf(this)
            .map { it.psi as KtBinaryExpression }
            .flatMap { listOf(it.left!!.node, it.right!!.node) }
            .map {
                // remove parentheses around expression, if there are any
                (it.psi as? KtParenthesizedExpression)?.expression?.node ?: it
            }
            .filterNot {
                // finally, if parts are binary expressions themselves, they should be present in our lists and we will process them later.
                // `true` and `false` are valid tokens for jBool, so we keep them.
                it.elementType == BINARY_EXPRESSION || it.text == "true" || it.text == "false"
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
        expressionReplacement: ExpressionReplacement,
        orderTokenMapper: OrderTokenMapper
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
        correctKotlinBooleanExpression = orderTokenMapper.restoreOrder(correctKotlinBooleanExpression)
        correctKotlinBooleanExpression = expressionReplacement.restoreFullExpression(correctKotlinBooleanExpression)

        node.replaceChild(node.firstChildNode, KotlinParser().createNode(correctKotlinBooleanExpression))
    }

    /**
     * Checks if boolean expression can be simplified with distributive law.
     *
     * @return String? null if it cannot be simplified. Simplified string otherwise.
     */
    private fun checkDistributiveLaw(
        expr: Expression<String>,
        expressionReplacement: ExpressionReplacement,
        node: ASTNode
    ): String? {
        // checking that expression can be considered as distributive law
        val commonDistributiveOperand = getCommonDistributiveOperand(node, expr.toString(), expressionReplacement) ?: return null
        val correctSymbolsSequence = expressionReplacement.getReplacements().toMutableList()
        correctSymbolsSequence.remove(commonDistributiveOperand)
        correctSymbolsSequence.add(0, commonDistributiveOperand)
        val expressionsLogicalOperator = expr.toString().first { it == '&' || it == '|' }
        // we return expression depending on second operator
        return returnNeededDistributiveExpression(expressionsLogicalOperator, correctSymbolsSequence)
    }

    /**
     * Returns correct result string in distributive law
     */
    private fun returnNeededDistributiveExpression(firstLogicalOperator: Char, symbols: List<Char>): String {
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
        expressionReplacement: ExpressionReplacement
    ): Char? {
        val operationSequence = expression.filter { it == '&' || it == '|' }
        val numberOfOperationReferences = operationSequence.length
        // There should be three operands and three operation references in order to consider the expression
        // Moreover the operation references between operands should alternate.
        if (expressionReplacement.size() < DISTRIBUTIVE_LAW_MIN_EXPRESSIONS ||
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

    internal inner class ExpressionReplacement {
        private val replacements = LinkedHashMap<String, Char>()

        fun isEmpty(): Boolean = replacements.isEmpty()

        fun size(): Int = replacements.size

        fun addExpression(expressionAstNode: ASTNode) {
            val expression = expressionAstNode.textWithoutComments()
            replacements.computeIfAbsent(expression) {
                ('A'.code + replacements.size).toChar()
            }
        }

        fun replaceExpressions(fullExpression: String): String {
            var resultExpression = fullExpression
            replacements.forEach { (refExpr, replacement) ->
                resultExpression = resultExpression.replace(refExpr, replacement.toString())
            }
            return resultExpression
        }

        fun restoreFullExpression(fullExpression: String): String {
            var resultExpression = fullExpression
            replacements.values.forEachIndexed { index, value ->
                resultExpression = resultExpression.replace(value.toString(), "%${index + 1}\$s")
            }
            resultExpression = resultExpression.format(*replacements.keys.toTypedArray())
            return resultExpression
        }

        fun getReplacements(): Collection<Char> {
            return replacements.values
        }
    }

    // it's Char to Char actually, but will keep it String for simplicity
    inner class OrderTokenMapper : TokenMapper<String> {
        private val variables = HashMap<String, String>()

        override fun getVariable(name: String): String {
            return variables.computeIfAbsent(name) {
                ('A'.code + variables.size).toChar().toString()
            }
        }

        fun restoreOrder(expression: String): String {
            var resultExpression = expression
            variables.values.forEachIndexed { index, value ->
                resultExpression = resultExpression.replace(value, "%${index + 1}\$s")
            }
            return resultExpression.format(*variables.keys.toTypedArray())
        }
    }

    private fun KtBinaryExpression.isXorExpression() = operationReference.text == "xor"

    companion object {
        const val DISTRIBUTIVE_LAW_MIN_EXPRESSIONS = 3
        const val DISTRIBUTIVE_LAW_MIN_OPERATIONS = 3
        const val NAME_ID = "acm-boolean-expressions-rule"
    }
}
