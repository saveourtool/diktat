package org.cqfn.diktat.ruleset.rules.classes

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isPartOfComment
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.getFunctionName
import org.cqfn.diktat.ruleset.utils.log
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.psi.psiUtil.startOffset

/**
 * This rules checks if an object initialization can be wrapped into an `apply` function.
 * This is useful for classes that, e.g. have single constructor without parameters and setters for all the parameters.
 * FixMe: When assigned variable's name is also a `this@apply`'s property, it should be changed to qualified name,
 *  e.g `this@Foo`. But for this we need a mechanism to determine declaration scope and it's label.
 */
class CompactInitialization(private val configRules: List<RulesConfig>) : Rule("class-compact-initialization") {
    private var isFixMode: Boolean = false
    private val kotlinParser by lazy { KotlinParser() }
    private lateinit var emitWarn: EmitType

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitType
    ) {
        emitWarn = emit
        isFixMode = autoCorrect

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
    @Suppress("UnsafeCallOnNullableType")
    private fun handleProperty(property: KtProperty) {
        property.run {
            val propertyName = name
            siblings(forward = true, withItself = false)
                .filterNot { it.node.isPartOfComment() || it is PsiWhiteSpace }
                .takeWhile {
                    // statements like `name.field = value` where name == propertyName
                    it is KtBinaryExpression && (it.left as? KtDotQualifiedExpression)?.run {
                        (receiverExpression as? KtNameReferenceExpression)?.getReferencedName() == propertyName
                    }
                        ?: false
                }
                .map {
                    // collect as an assignment associated with assigned field name
                    it as KtBinaryExpression to (it.left as KtDotQualifiedExpression).selectorExpression!!
                }
        }
            .toList()
            .forEach { (assignment, field) ->
                Warnings.COMPACT_OBJECT_INITIALIZATION.warnAndFix(
                    configRules, emitWarn, isFixMode,
                    field.text, assignment.startOffset, assignment.node
                ) {
                    moveAssignmentIntoApply(property, assignment)
                }
            }
    }

    @Suppress("UnsafeCallOnNullableType", "NestedBlockDepth")
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
                        .takeWhile { it != property.node }
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
                val newInitializerNode = kotlinParser.createNode("${property.initializer!!.text}.apply {}")
                replaceChild(property.initializer!!.node, newInitializerNode)
            }
            (property.initializer as KtDotQualifiedExpression).selectorExpression!! as KtCallExpression
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
                    log.warn("apply with unexpected parameters: ${applyExpression.text}")
                }
        }
    }
}
