package com.saveourtool.diktat.ruleset.rules.chapter6.classes

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.COMPACT_OBJECT_INITIALIZATION
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.KotlinParser
import com.saveourtool.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import com.saveourtool.diktat.ruleset.utils.findLeafWithSpecificType
import com.saveourtool.diktat.ruleset.utils.getFunctionName
import com.saveourtool.diktat.ruleset.utils.isPartOfComment

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlin.KtNodeTypes.CALL_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.OPERATION_REFERENCE
import org.jetbrains.kotlin.KtNodeTypes.REFERENCE_EXPRESSION
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.KDOC
import org.jetbrains.kotlin.lexer.KtTokens.BLOCK_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.EQ
import org.jetbrains.kotlin.lexer.KtTokens.LBRACE
import org.jetbrains.kotlin.lexer.KtTokens.THIS_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.psi.psiUtil.startOffset

/**
 * This rules checks if an object initialization can be wrapped into an `apply` function.
 * This is useful for classes that, e.g. have single constructor without parameters and setters for all the parameters.
 * FixMe: When assigned variable's name is also a `this@apply`'s property, it should be changed to qualified name,
 *  e.g `this@Foo`. But for this we need a mechanism to determine declaration scope and it's label.
 */
class CompactInitialization(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(COMPACT_OBJECT_INITIALIZATION)
) {
    private val kotlinParser by lazy { KotlinParser() }

    override fun logic(node: ASTNode) {
        node
            .psi
            .let { it as? KtProperty }
            ?.takeIf { it.hasInitializer() }
            ?.let(::handleProperty)
    }

    /**
     * Check property's initializer: if it is a method call, we find all consecutive statements that are this property's
     * fields accessors and wrap them in an `apply` function.
     */
    @Suppress("UnsafeCallOnNullableType", "PARAMETER_NAME_IN_OUTER_LAMBDA")
    private fun handleProperty(property: KtProperty) {
        property.run {
            val propertyName = name
            siblings(forward = true, withItself = false)
                .filterNot { it.node.isPartOfComment() || it is PsiWhiteSpace }
                .takeWhile {
                    // statements like `name.field = value` where name == propertyName
                    it is KtBinaryExpression && it.node.findChildByType(OPERATION_REFERENCE)?.findChildByType(EQ) != null &&
                            (it.left as? KtDotQualifiedExpression)?.run {
                                (receiverExpression as? KtNameReferenceExpression)?.getReferencedName() == propertyName
                            }
                            ?: false
                }
                .map {
                    // collect as an assignment associated with assigned field name
                    it as KtBinaryExpression to (it.left as KtDotQualifiedExpression).selectorExpression!!
                }
        }
            .filter { (assignment, _) ->
                assignment.node.findLeafWithSpecificType(THIS_KEYWORD) == null
            }
            .toList()
            .forEach { (assignment, field) ->
                COMPACT_OBJECT_INITIALIZATION.warnAndFix(
                    configRules, emitWarn, isFixMode,
                    field.text, assignment.startOffset, assignment.node
                ) {
                    moveAssignmentIntoApply(property, assignment)
                }
            }
    }

    @Suppress(
        "UnsafeCallOnNullableType",
        "NestedBlockDepth",
        "TOO_LONG_FUNCTION"
    )
    private fun moveAssignmentIntoApply(property: KtProperty, assignment: KtBinaryExpression) {
        // get apply expression or create empty; convert `apply(::foo)` to `apply { foo(this) }` if necessary
        getOrCreateApplyBlock(property).let(::convertValueParametersToLambdaArgument)
        // apply expression can have been changed earlier, so we need to get it once again
        with(getOrCreateApplyBlock(property)) {
            lambdaArguments
                .single()
                // KtLambdaArgument#getArgumentExpression is Nullable IfNotParsed
                .getLambdaExpression()!!
                .run {
                    val bodyExpression = functionLiteral
                        // note: we are dealing with function literal: braces belong to KtFunctionLiteral,
                        // but it's body is a KtBlockExpression, which therefore doesn't have braces
                        .bodyExpression!!
                        .node
                    // move comments and empty lines before `assignment` into `apply`
                    assignment
                        .node
                        .siblings(forward = false)
                        .takeWhile { it.elementType in listOf(WHITE_SPACE, EOL_COMMENT, BLOCK_COMMENT, KDOC) }
                        .toList()
                        .reversed()
                        .forEachIndexed { index, it ->
                            // adds whiteSpace to functional literal if previous of bodyExpression is LBRACE
                            if (index == 0 && bodyExpression.treePrev.elementType == LBRACE && it.elementType == WHITE_SPACE) {
                                bodyExpression.treeParent.addChild(it.clone() as ASTNode, bodyExpression)
                            } else {
                                bodyExpression.addChild(it.clone() as ASTNode, null)
                            }
                            it.treeParent.removeChild(it)
                        }
                    val receiverName = (assignment.left as KtDotQualifiedExpression).receiverExpression
                    // looking for usages of receiver in right part
                    val identifiers = assignment.right!!.node.findAllDescendantsWithSpecificType(REFERENCE_EXPRESSION)
                    identifiers.forEach {
                        if (it.text == receiverName.text && it.treeParent.elementType != CALL_EXPRESSION) {
                            it.treeParent.replaceChild(it, kotlinParser.createNode("this"))
                        }
                    }
                    // strip receiver name and move assignment itself into `apply`
                    bodyExpression.addChild(kotlinParser.createNode(assignment.text.substringAfter('.')), null)
                    assignment.node.run { treeParent.removeChild(this) }
                }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun getOrCreateApplyBlock(property: KtProperty): KtCallExpression = (property.initializer as? KtDotQualifiedExpression)
        ?.selectorExpression
        ?.let { it as? KtCallExpression }
        ?.takeIf { it.getFunctionName() == "apply" }
        ?: run {
            // add apply block
            property.node.run {
                val newInitializerNodeText = buildInitializerNodeText(property)
                val newInitializerNode = kotlinParser.createNode(newInitializerNodeText)
                replaceChild(property.initializer!!.node, newInitializerNode)
            }
            (property.initializer as KtDotQualifiedExpression).selectorExpression!! as KtCallExpression
        }

    @Suppress("UnsafeCallOnNullableType")
    private fun buildInitializerNodeText(property: KtProperty): String {
        val isRequiresParentheses = property.initializer.let {
            // list of expression types, that can be directly followed by a dot-qualified call
            // e.g. val x = foo()  ->  val x = foo().apply {}
            // e.g. val x = foo + bar  ->  val x = (foo + bar).apply {}
            it is KtParenthesizedExpression || it is KtQualifiedExpression || it is KtReferenceExpression
        }.not()
        return buildString {
            if (isRequiresParentheses) {
                append("(")
            }
            append(property.initializer!!.text)
            if (isRequiresParentheses) {
                append(")")
            }
            append(".apply {}")
        }
    }

    /**
     * convert `apply(::foo)` to `apply { foo(this) }` if necessary
     */
    private fun convertValueParametersToLambdaArgument(applyExpression: KtCallExpression) {
        if (applyExpression.lambdaArguments.isEmpty()) {
            val referenceExpression = applyExpression
                .valueArguments
                .singleOrNull()
                ?.getArgumentExpression()
                ?.let { it as? KtCallableReferenceExpression }
                ?.callableReference
            referenceExpression?.let {
                applyExpression.node.run {
                    treeParent.replaceChild(
                        this,
                        kotlinParser.createNode(
                            """
                                |apply {
                                |    ${referenceExpression.getReferencedName()}(this)
                                |}
                            """.trimMargin()
                        )
                    )
                }
            }
                ?: run {
                    // valid code should always have apply with either lambdaArguments or valueArguments
                    log.warn { "apply with unexpected parameters: ${applyExpression.text}" }
                }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
        const val NAME_ID = "class-compact-initialization"
    }
}
