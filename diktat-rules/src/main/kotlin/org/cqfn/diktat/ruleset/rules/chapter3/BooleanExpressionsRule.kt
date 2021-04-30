package org.cqfn.diktat.ruleset.rules.chapter3

import com.bpodgursky.jbool_expressions.Expression
import com.bpodgursky.jbool_expressions.parsers.ExprParser
import com.bpodgursky.jbool_expressions.rules.RuleSet
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.COMPLEX_BOOLEAN_EXPRESSION
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.findAllNodesWithCondition
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import java.lang.RuntimeException

class BooleanExpressionsRule(configRules: List<RulesConfig>) : DiktatRule(
    "boolean-expressions-rule",
    configRules,
    listOf(COMPLEX_BOOLEAN_EXPRESSION)) {
    override fun logic(node: ASTNode) {
        if (node.elementType == ElementType.CONDITION) {
            checkBooleanExpression(node)
        }
    }

    private fun checkBooleanExpression(node: ASTNode) {
        val hashMap = HashMap<String, Char>()
        var characterAsciiCode = 65
        node
            .findAllNodesWithCondition({ it.elementType == BINARY_EXPRESSION })
            .filterNot { it.text.contains("&&") || it.text.contains("||") }
            .forEach {
                if (hashMap.containsKey(it.text)) {
                    return@forEach
                }
                hashMap[it.text] = characterAsciiCode.toChar()
                characterAsciiCode++
            }
        // Library is using & as && and | as ||.
        var correctedExpression = "(${node
            .text
            .replace("&&", "&")
            .replace("||", "|")})"
        hashMap.forEach { (refExpr, char) ->
            correctedExpression = correctedExpression.replace(refExpr, char.toString())
        }
        // If there is method calls in conditions
        val expr: Expression<String> = try {
            ExprParser.parse(correctedExpression)
        } catch (runTimeExc: RuntimeException) {
            return
        }
        val simplifiedExpression = RuleSet.simplify(expr)
        if (expr != simplifiedExpression) {
            COMPLEX_BOOLEAN_EXPRESSION.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                fixBooleanExpression(node, simplifiedExpression, hashMap)
            }
        }
    }

    private fun fixBooleanExpression(node: ASTNode, simplifiedExpr: Expression<String>, hashMap: HashMap<String, Char>) {
        var correctKotlinBooleanExpression = simplifiedExpr
            .toString()
            .replace("&", "&&")
            .replace("|", "||")
            .drop(1) // dropping first (
            .dropLast(1) // dropping last )
        hashMap.forEach { (key, value) ->
            correctKotlinBooleanExpression = correctKotlinBooleanExpression.replace(value.toString(), key)
        }
        node.replaceChild(node.firstChildNode, KotlinParser().createNode(correctKotlinBooleanExpression))
    }
}
