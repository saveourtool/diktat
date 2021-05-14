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
        val correctedExpression = makeCorrectExpressionString(node, mapOfExpressionToChar)
        // If there are method calls in conditions
        val expr: Expression<String> = try {
            ExprParser.parse(correctedExpression)
        } catch (exc: RuntimeException) {
            // this comes up if there is an unparsable expression. For example a.and(b)
            return
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
     * @return
     */
    internal fun makeCorrectExpressionString(node: ASTNode, mapOfExpressionToChar: HashMap<String, Char>): String {
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
        if (!isDistributiveLaw(node, expr.toString(), mapOfExpressionToChar)) {
            return null
        }
        val expressionsLogicalOperator = expr.toString().first { it == '&' || it == '|' }
        // we return expression depending on second operator
        return if (expressionsLogicalOperator == '&') {
            "A & (B | C)"
        } else {
            "A | (B & C)"
        }
    }

    /**
     * Method that checks that the expression can be simplified by distributive law.
     * Distributive law - A && B || A && C -> A && (B || C) or (A || B) && (A || C) -> A || (B && C)
     */
    private fun isDistributiveLaw(
        node: ASTNode,
        expression: String,
        mapOfExpressionToChar: HashMap<String, Char>): Boolean {
        val numberOfOperationReferences = expression.count { it == '&' || it == '|' }
        val operationSequence = expression.filter { it == '&' || it == '|' }
        // There should be three operands and three operation references in order to consider the expression
        // Moreover the operation references between operands should alternate.
        if (mapOfExpressionToChar.size != DISTRIBUTIVE_LAW_NEEDED_EXPRESSIONS ||
                numberOfOperationReferences != DISTRIBUTIVE_LAW_NEEDED_OPERATIONS ||
                (operationSequence != "&|&" && operationSequence != "|&|")) {
            return false
        }
        return if (operationSequence == "&|&") {
            hasCommonOperand(expression, '|', '&')
        } else {
            // this is done for excluding A || B && A || C without parenthesis.
            val parenthesizedExpressions = node.findAllNodesWithCondition({ it.elementType == PARENTHESIZED })
            parenthesizedExpressions.forEach {
                it.findLeafWithSpecificType(OPERATION_REFERENCE) ?: run {
                    return false
                }
            }
            hasCommonOperand(expression, '&', '|')
        }
    }

    private fun hasCommonOperand(
        expression: String,
        firstSplitDelimiter: Char,
        secondSplitDelimiter: Char): Boolean {
        val twoExpressions = expression.split(firstSplitDelimiter)
        val firstOperands = twoExpressions[0].filterNot { it == ' ' || it == '(' || it == ')' }.split(secondSplitDelimiter)
        val secondOperands = twoExpressions[1].filterNot { it == ' ' || it == '(' || it == ')' }.split(secondSplitDelimiter)
        return firstOperands.any { secondOperands.contains(it) }
    }

    companion object {
        const val DISTRIBUTIVE_LAW_NEEDED_EXPRESSIONS = 3
        const val DISTRIBUTIVE_LAW_NEEDED_OPERATIONS = 3
    }
}
