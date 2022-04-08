package org.cqfn.diktat.ruleset.rules.chapter4

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.AVOID_NULL_CHECKS
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.BREAK
import com.pinterest.ktlint.core.ast.ElementType.CONDITION
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.IF_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.NULL
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.THEN
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.core.ast.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.psiUtil.blockExpressionsOrSingle

/**
 * This rule check and fixes explicit null checks (explicit comparison with `null`)
 * There are several code-structures that can be used in Kotlin to avoid null-checks. For example: `?:`,  `.let {}`, `.also {}`, e.t.c
 */
class NullChecksRule(configRules: List<RulesConfig>) : DiktatRule(
    nameId,
    configRules,
    listOf(AVOID_NULL_CHECKS)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == CONDITION) {
            node.parent(IF)?.let {
                // excluding complex cases with else-if statements, because they look better with explicit null-check
                if (!isComplexIfStatement(it)) {
                    // this can be autofixed as the condition stays in simple if-statement
                    conditionInIfStatement(node)
                }
            }
        }

        if (node.elementType == BINARY_EXPRESSION) {
            // `condition` case is already checked above, so no need to check it once again
            node.parent(CONDITION) ?: run {
                // only warning here, because autofix in other statements (like) lambda (or value) can break the code
                nullCheckInOtherStatements(node)
            }
        }
    }

    /**
     * checks that if-statement is a complex condition
     * You can name a statement - "complex if-statement" if it has other if in the else branch (else-if structure)
     */
    private fun isComplexIfStatement(parentIf: ASTNode): Boolean {
        val parentIfPsi = parentIf.psi
        require(parentIfPsi is KtIfExpression)
        return (parentIfPsi.`else`?.node?.firstChildNode?.elementType == IF_KEYWORD)
    }

    private fun conditionInIfStatement(node: ASTNode) {
        node.findAllDescendantsWithSpecificType(BINARY_EXPRESSION).forEach { binaryExprNode ->
            val condition = (binaryExprNode.psi as KtBinaryExpression)
            if (isNullCheckBinaryExpression(condition)) {
                when (condition.operationToken) {
                    // `==` and `===` comparison can be fixed with `?:` operator
                    ElementType.EQEQ, ElementType.EQEQEQ ->
                        warnAndFixOnNullCheck(
                            condition,
                            isFixable(node, true),
                            "use '.let/.also/?:/e.t.c' instead of ${condition.text}"
                        ) {
                            fixNullInIfCondition(node, condition, true)
                        }
                    // `!==` and `!==` comparison can be fixed with `.let/also` operators
                    ElementType.EXCLEQ, ElementType.EXCLEQEQEQ ->
                        warnAndFixOnNullCheck(
                            condition,
                            isFixable(node, false),
                            "use '.let/.also/?:/e.t.c' instead of ${condition.text}"
                        ) {
                            fixNullInIfCondition(node, condition, false)
                        }
                    else -> return
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun isFixable(condition: ASTNode,
                          isEqualToNull: Boolean): Boolean {
        // Handle cases with `break` word in blocks
        val typePair = if (isEqualToNull) {
            (ELSE to THEN)
        } else {
            (THEN to ELSE)
        }
        val isBlockInIfWithBreak = condition.getBreakNodeFromIf(typePair.first)
        val isOneLineBlockInIfWithBreak = condition
            .treeParent
            .findChildByType(typePair.second)
            ?.let { it.findChildByType(BLOCK) ?: it }
            ?.let { astNode ->
                astNode.hasChildOfType(BREAK) &&
                        (astNode.psi as? KtBlockExpression)?.statements?.size != 1
            } ?: false
        return (!isBlockInIfWithBreak && !isOneLineBlockInIfWithBreak)
    }

    @Suppress("UnsafeCallOnNullableType", "TOO_LONG_FUNCTION")
    private fun fixNullInIfCondition(condition: ASTNode,
                                     binaryExpression: KtBinaryExpression,
                                     isEqualToNull: Boolean
    ) {
        val variableName = binaryExpression.left!!.text
        val thenFromExistingCode = condition.extractLinesFromBlock(THEN)
        val elseFromExistingCode = condition.extractLinesFromBlock(ELSE)

        // if (a == null) { foo() } else { bar() } -> if (a != null) { bar() } else { foo() }
        val thenCodeLines = if (isEqualToNull) {
            elseFromExistingCode
        } else {
            thenFromExistingCode
        }
        val elseCodeLines = if (isEqualToNull) {
            thenFromExistingCode
        } else {
            elseFromExistingCode
        }
        val numberOfStatementsInElseBlock = if (isEqualToNull) {
            (condition.treeParent.psi as KtIfExpression).then?.blockExpressionsOrSingle()?.count() ?: 0
        } else {
            (condition.treeParent.psi as KtIfExpression).`else`?.blockExpressionsOrSingle()?.count() ?: 0
        }

        val elseEditedCodeLines = getEditedElseCodeLines(elseCodeLines, numberOfStatementsInElseBlock)
        val thenEditedCodeLines = getEditedThenCodeLines(variableName, thenCodeLines, elseEditedCodeLines)

        val text = "$thenEditedCodeLines $elseEditedCodeLines"
        val tree = KotlinParser().createNode(text)
        condition.treeParent.treeParent.addChild(tree, condition.treeParent)
        condition.treeParent.treeParent.removeChild(condition.treeParent)
    }

    private fun getEditedElseCodeLines(elseCodeLines: List<String>?, numberOfStatementsInElseBlock: Int): String = when {
        // else { "null"/empty } -> ""
        elseCodeLines == null || elseCodeLines.singleOrNull() == "null" -> ""
        // else { bar() } -> ?: bar()
        numberOfStatementsInElseBlock == 1 -> "?: ${elseCodeLines.joinToString(postfix = "\n", separator = "\n")}"
        // else { ... } -> ?: run { ... }
        else -> getDefaultCaseElseCodeLines(elseCodeLines)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun getEditedThenCodeLines(
        variableName: String,
        thenCodeLines: List<String>?,
        elseEditedCodeLines: String
    ): String = when {
        // if (a != null) {  } -> a ?: editedElse
        (thenCodeLines.isNullOrEmpty() && elseEditedCodeLines.isNotEmpty()) ||
                // if (a != null) { a } else { ... } -> a ?: editedElse
                (thenCodeLines?.singleOrNull() == variableName && elseEditedCodeLines.isNotEmpty()) -> variableName
        // if (a != null) { a.foo() } -> a?.foo()
        thenCodeLines?.singleOrNull()?.startsWith("$variableName.") ?: false -> "$variableName?${thenCodeLines?.firstOrNull()!!.removePrefix(variableName)}"
        // if (a != null) { break } -> a?.let { ... }
        // if (a != null) { foo() } -> a?.let { ... }
        else -> getDefaultCaseThenCodeLines(variableName, thenCodeLines)
    }

    private fun getDefaultCaseThenCodeLines(variableName: String, thenCodeLines: List<String>?): String =
            "$variableName?.let {${thenCodeLines?.joinToString(prefix = "\n", postfix = "\n", separator = "\n")}}"

    private fun getDefaultCaseElseCodeLines(elseCodeLines: List<String>): String = "?: run {${elseCodeLines.joinToString(prefix = "\n", postfix = "\n", separator = "\n")}}"

    @Suppress("COMMENT_WHITE_SPACE", "UnsafeCallOnNullableType")
    private fun nullCheckInOtherStatements(binaryExprNode: ASTNode) {
        val condition = (binaryExprNode.psi as KtBinaryExpression)
        if (isNullCheckBinaryExpression(condition)) {
            // require(a != null) is incorrect, Kotlin has special method: `requireNotNull` - need to use it instead
            // hierarchy is the following:
            //              require(a != null)
            //                 /            \
            //      REFERENCE_EXPRESSION    VALUE_ARGUMENT_LIST
            //                |                       |
            //          IDENTIFIER(require)     VALUE_ARGUMENT
            val parent = binaryExprNode.treeParent
            if (parent != null && parent.elementType == VALUE_ARGUMENT) {
                val grandParent = parent.treeParent
                if (grandParent != null && grandParent.elementType == VALUE_ARGUMENT_LIST && isRequireFun(grandParent.treePrev)) {
                    @Suppress("COLLAPSE_IF_STATEMENTS")
                    if (listOf(ElementType.EXCLEQ, ElementType.EXCLEQEQEQ).contains(condition.operationToken)) {
                        warnAndFixOnNullCheck(
                            condition,
                            true,
                            "use 'requireNotNull' instead of require(${condition.text})"
                        ) {
                            val variableName = (binaryExprNode.psi as KtBinaryExpression).left!!.text
                            val newMethod = KotlinParser().createNode("requireNotNull($variableName)")
                            grandParent.treeParent.treeParent.addChild(newMethod, grandParent.treeParent)
                            grandParent.treeParent.treeParent.removeChild(grandParent.treeParent)
                        }
                    }
                }
            }
        }
    }

    private fun ASTNode.getBreakNodeFromIf(type: IElementType) = this
        .treeParent
        .findChildByType(type)
        ?.let { it.findChildByType(BLOCK) ?: it }
        ?.findAllNodesWithCondition { it.elementType == BREAK }?.isNotEmpty()
        ?: false

    private fun ASTNode.extractLinesFromBlock(type: IElementType): List<String>? =
            treeParent
                .getFirstChildWithType(type)
                ?.text
                ?.trim('{', '}')
                ?.split("\n")
                ?.filter { it.isNotBlank() }
                ?.map { it.trim() }
                ?.toList()

    @Suppress("UnsafeCallOnNullableType")
    private fun isNullCheckBinaryExpression(condition: KtBinaryExpression): Boolean =
            // check that binary expression has `null` as right or left operand
            setOf(condition.right, condition.left).map { it!!.node.elementType }.contains(NULL) &&
                    // checks that it is the comparison condition
                    setOf(ElementType.EQEQ, ElementType.EQEQEQ, ElementType.EXCLEQ, ElementType.EXCLEQEQEQ)
                        .contains(condition.operationToken) &&
                    // no need to raise warning or fix null checks in complex expressions
                    !condition.isComplexCondition() &&
                    !condition.isInLambda()

    /**
     * checks if condition is a complex expression. For example:
     * (a == 5) - is not a complex condition, but (a == 5 && b != 6) is a complex condition
     */
    private fun KtBinaryExpression.isComplexCondition(): Boolean {
        // KtBinaryExpression is complex if it has a parent that is also a binary expression
        return this.parent is KtBinaryExpression
    }

    /**
     * Expression could be used in lambda:
     * if (a.any { it == null })
     */
    private fun KtBinaryExpression.isInLambda(): Boolean {
        // KtBinaryExpression is in lambda if it has a parent that is a block expression
        return this.parent is KtBlockExpression
    }

    private fun warnAndFixOnNullCheck(
        condition: KtBinaryExpression,
        canBeAutoFixed: Boolean,
        freeText: String,
        autofix: () -> Unit
    ) {
        AVOID_NULL_CHECKS.warnAndFix(
            configRules,
            emitWarn,
            isFixMode,
            freeText,
            condition.node.startOffset,
            condition.node,
            canBeAutoFixed,
        ) {
            autofix()
        }
    }

    private fun isRequireFun(referenceExpression: ASTNode) =
            referenceExpression.elementType == REFERENCE_EXPRESSION && referenceExpression.firstChildNode.text == "require"

    companion object {
        val nameId = "ach-null-checks"
    }
}
