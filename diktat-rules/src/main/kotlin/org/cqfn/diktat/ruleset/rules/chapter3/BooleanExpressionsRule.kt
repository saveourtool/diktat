package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.COMPLEX_BOOLEAN_EXPRESSION
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.findAllNodesWithCondition
import org.cqfn.diktat.ruleset.utils.findLeafWithSpecificType

import com.bpodgursky.jbool_expressions.Expression
import com.bpodgursky.jbool_expressions.parsers.ExprParser
import com.bpodgursky.jbool_expressions.rules.RuleSet
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.PARENTHESIZED
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

import java.lang.RuntimeException

/**
 * Rule that warns if the boolean expression can be simplified.
 */
class BooleanExpressionsRule(configRules: List<RulesConfig>) : DiktatRule(
    "boolean-expressions-rule",
    configRules,
    listOf(COMPLEX_BOOLEAN_EXPRESSION)) {
    override fun logic(node: ASTNode) {
        if (node.elementType == ElementType.CONDITION) {
            checkBooleanExpression(node)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun checkBooleanExpression(node: ASTNode) {
        // This map is used to assign a variable name for every elementary boolean expression.
        val mapOfExpressionToChar: HashMap<String, Char> = HashMap()
        val correctedExpression = formatBooleanExpressionAsString(node, mapOfExpressionToChar)
        // If there are method calls in conditions
        val expr: Expression<String> = try {
            ExprParser.parse(correctedExpression)
        } catch (exc: RuntimeException) {
            if (exc.message?.startsWith("Unrecognized!") == true) {
                // this comes up if there is an unparsable expression. For example a.and(b)
                return
            } else {
                throw exc
            }
        }
        val distributiveLawString = checkDistributiveLaw(expr, mapOfExpressionToChar, node)
        val simplifiedExpression = distributiveLawString?.let {
            ExprParser.parse(distributiveLawString)
        }
            ?: RuleSet.simplify(expr)
        if (expr != simplifiedExpression) {
            COMPLEX_BOOLEAN_EXPRESSION.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                fixBooleanExpression(node, simplifiedExpression, mapOfExpressionToChar)
            }
        }
    }

    /**
     * @param node
     * @param mapOfExpressionToChar
     * @return corrected string
     */
    internal fun formatBooleanExpressionAsString(node: ASTNode, mapOfExpressionToChar: HashMap<String, Char>): String {
        // `A` character in ASCII
        var characterAsciiCode = 'A'.code
        node
            .findAllNodesWithCondition({ it.elementType == BINARY_EXPRESSION })
            .filterNot { it.text.contains("&&") || it.text.contains("||") }
            .forEach { expression ->
                mapOfExpressionToChar.computeIfAbsent(expression.text) {
                    characterAsciiCode++.toChar()
                }
            }
        // Library is using & as && and | as ||.
        var correctedExpression = "(${node
            .text
            .replace("&&", "&")
            .replace("||", "|")})"
        mapOfExpressionToChar.forEach { (refExpr, char) ->
            correctedExpression = correctedExpression.replace(refExpr, char.toString())
        }
        return correctedExpression
    }

    private fun fixBooleanExpression(
        node: ASTNode,
        simplifiedExpr: Expression<String>,
        mapOfExpressionToChar: HashMap<String, Char>) {
        var correctKotlinBooleanExpression = simplifiedExpr
            .toString()
            .replace("&", "&&")
            .replace("|", "||")
            .drop(1)  // dropping first (
            .dropLast(1)  // dropping last )
        mapOfExpressionToChar.forEach { (key, value) ->
            correctKotlinBooleanExpression = correctKotlinBooleanExpression.replace(value.toString(), key)
        }
        node.replaceChild(node.firstChildNode, KotlinParser().createNode(correctKotlinBooleanExpression))
    }

    /**
     * Checks if boolean expression can be simplified with distributive law.
     *
     * @return String? null if it cannot be simplified. Simplified string otherwise.
     */
    private fun checkDistributiveLaw(
        expr: Expression<String>,
        mapOfExpressionToChar: HashMap<String, Char>,
        node: ASTNode): String? {
        // checking that expression can be considered as distributive law
        val commonDistributiveOperand = getCommonDistributiveOperand(node, expr.toString(), mapOfExpressionToChar) ?: return null
        val correctSymbolsSequence = mapOfExpressionToChar.values.toMutableList()
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
        mapOfExpressionToChar: HashMap<String, Char>): Char? {
        val operationSequence = expression.filter { it == '&' || it == '|' }
        val numberOfOperationReferences = operationSequence.length
        // There should be three operands and three operation references in order to consider the expression
        // Moreover the operation references between operands should alternate.
        if (mapOfExpressionToChar.size < DISTRIBUTIVE_LAW_MIN_EXPRESSIONS ||
                numberOfOperationReferences < DISTRIBUTIVE_LAW_MIN_OPERATIONS ||
                !isSequenceAlternate(operationSequence)) {
            return null
        }
        return if (operationSequence.first() == '&') {
            getCommonOperand(expression, '|', '&')
        } else {
            // this is done for excluding A || B && A || C without parenthesis.
            val parenthesizedExpressions = node.findAllNodesWithCondition({ it.elementType == PARENTHESIZED })
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
        secondSplitDelimiter: Char): Char? {
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

    companion object {
        const val DISTRIBUTIVE_LAW_MIN_EXPRESSIONS = 3
        const val DISTRIBUTIVE_LAW_MIN_OPERATIONS = 3
    }
}
