package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.STRING_TEMPLATE_CURLY_BRACES
import org.cqfn.diktat.ruleset.constants.Warnings.STRING_TEMPLATE_QUOTES
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import org.cqfn.diktat.ruleset.utils.hasAnyChildOfTypes

import com.pinterest.ktlint.core.ast.ElementType.ARRAY_ACCESS_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CLOSING_QUOTE
import com.pinterest.ktlint.core.ast.ElementType.FLOAT_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.LITERAL_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.LONG_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.SHORT_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.SHORT_TEMPLATE_ENTRY_START
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * In String templates there should not be redundant curly braces. In case of using a not complex statement (one argument)
 * there should not be curly braces.
 *
 * FixMe: The important caveat here: in "$foo" kotlin compiler adds implicit call to foo.toString() in case foo type is not string.
 */
class StringTemplateFormatRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(STRING_TEMPLATE_CURLY_BRACES, STRING_TEMPLATE_QUOTES)
) {
    override fun logic(node: ASTNode) {
        when (node.elementType) {
            LONG_STRING_TEMPLATE_ENTRY -> handleLongStringTemplate(node)
            SHORT_STRING_TEMPLATE_ENTRY -> handleShortStringTemplate(node)
            else -> {
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleLongStringTemplate(node: ASTNode) {
        // Checking if in long templates {a.foo()} there are function calls or class toString call
        if (bracesCanBeOmitted(node)) {
            STRING_TEMPLATE_CURLY_BRACES.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                val identifierName = node.findChildByType(REFERENCE_EXPRESSION)
                identifierName?.let {
                    val shortTemplate = CompositeElement(SHORT_STRING_TEMPLATE_ENTRY)
                    val reference = CompositeElement(REFERENCE_EXPRESSION)

                    node.treeParent.addChild(shortTemplate, node)
                    shortTemplate.addChild(LeafPsiElement(SHORT_TEMPLATE_ENTRY_START, "$"), null)
                    shortTemplate.addChild(reference)
                    reference.addChild(LeafPsiElement(IDENTIFIER, identifierName.text))
                    node.treeParent.removeChild(node)
                }
                    ?: run {
                        val stringTemplate = node.treeParent
                        val appropriateText = node.text.trim('$', '{', '}')
                        stringTemplate.addChild(LeafPsiElement(LITERAL_STRING_TEMPLATE_ENTRY, appropriateText), node)
                        stringTemplate.removeChild(node)
                    }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleShortStringTemplate(node: ASTNode) {
        val identifierName = node.findChildByType(REFERENCE_EXPRESSION)?.text

        if (identifierName != null && node.treeParent.text.trim('"', '$') == identifierName) {
            STRING_TEMPLATE_QUOTES.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                val identifier = node.findChildByType(REFERENCE_EXPRESSION)!!.copyElement()
                // node.treeParent is String template that we need to delete
                node.treeParent.treeParent.addChild(identifier, node.treeParent)
                node.treeParent.treeParent.removeChild(node.treeParent)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType", "FUNCTION_BOOLEAN_PREFIX")
    private fun bracesCanBeOmitted(node: ASTNode): Boolean {
        val onlyOneRefExpr = node
            .findAllDescendantsWithSpecificType(REFERENCE_EXPRESSION)
            .singleOrNull()
            ?.treeParent
            ?.elementType == LONG_STRING_TEMPLATE_ENTRY

        val isArrayAccessExpression = node  // this should be omitted in previous expression, used for safe warranties
            .findAllDescendantsWithSpecificType(REFERENCE_EXPRESSION)
            .singleOrNull()
            ?.treeParent
            ?.elementType == ARRAY_ACCESS_EXPRESSION

        return if (onlyOneRefExpr && !isArrayAccessExpression) {
            (!(node.treeNext
                .text
                .first()
                // checking if first letter is valid
                .isLetterOrDigit() ||
                node.treeNext.text.startsWith("_")) ||
                node.treeNext.elementType == CLOSING_QUOTE
            )
        } else if (!isArrayAccessExpression) {
            node.hasAnyChildOfTypes(FLOAT_CONSTANT, INTEGER_CONSTANT)  // it also fixes "${1.0}asd" cases
        } else {
            false
        }
    }

    companion object {
        const val NAME_ID = "string-template-format"
    }
}
