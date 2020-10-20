package org.cqfn.diktat.ruleset.rules.classes

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.COMMA
import com.pinterest.ktlint.core.ast.ElementType.LPAR
import com.pinterest.ktlint.core.ast.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.RPAR
import com.pinterest.ktlint.core.ast.ElementType.SECONDARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.SINGLE_CONSTRUCTOR_SHOULD_BE_PRIMARY
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getIdentifierName
import org.cqfn.diktat.ruleset.utils.isGoingAfter
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.psiUtil.asAssignment

class SingleConstructorRule(private val config: List<RulesConfig>) : Rule("single-constructor") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == CLASS) {
            handleClassConstructors(node)
        }
    }

    private fun handleClassConstructors(node: ASTNode) {
        if (node.findChildByType(PRIMARY_CONSTRUCTOR) == null) {
            // class has no primary constructor, need to count secondary constructors
            node
                .findChildByType(CLASS_BODY)
                ?.getAllChildrenWithType(SECONDARY_CONSTRUCTOR)
                ?.singleOrNull()
                ?.let { secondaryCtor ->
                    SINGLE_CONSTRUCTOR_SHOULD_BE_PRIMARY.warnAndFix(
                        config, emitWarn, isFixMode, "in class <${node.getIdentifierName()?.text}>",
                        node.startOffset, node
                    ) {
                        // Inside the single secondary constructor find all assignments.
                        // Some of assigned values will have `this` qualifier, they are definitely class properties.
                        // For other assigned variables that are not declared in the same scope we will check later if they are properties.
                        val (qualifiedAssignedProperties, assignedProperties) = (secondaryCtor.psi as KtSecondaryConstructor).bodyBlockExpression
                            ?.statements
                            ?.mapNotNull { (it as? KtBinaryExpression)?.asAssignment()?.left }
                            ?.filter {
                                // todo use secondaryCtor.findAllVariablesWithAssignments()
                                (it as? KtDotQualifiedExpression)?.run {
                                    receiverExpression is KtThisExpression && selectorExpression is KtNameReferenceExpression
                                } ?: false ||
                                        it is KtNameReferenceExpression
                            }
                            ?.partition { it is KtDotQualifiedExpression }
                            ?.let { (qualified, simple) ->
                                qualified.map { (it as KtDotQualifiedExpression).selectorExpression as KtNameReferenceExpression } to
                                        simple.map { it as KtNameReferenceExpression }
                            }
                            ?: (emptyList<KtNameReferenceExpression>() to emptyList())


                        val assignedClassProperties = qualifiedAssignedProperties + assignedProperties
                            .run {
                                val localProperties = secondaryCtor.findAllNodesWithSpecificType(ElementType.PROPERTY)
                                filterNot { reference ->
                                    // check for shadowing
                                    localProperties.any {
                                        reference.node.isGoingAfter(it) && (it.psi as KtProperty).name == reference.name
                                    }
                                }
                            }

                        // find properties declarations
                        val declarationsAssignedInCtor = assignedClassProperties
                            .mapNotNull { reference ->
                                (node.psi as KtClass).getProperties()
                                    .firstOrNull { it.nameIdentifier?.text == reference.getReferencedName() }
                            }
                            .distinct()

                        // move them; todo: with their initial values (unless they are `lateinit`) to the primary
                        val primaryCtorNode = CompositeElement(PRIMARY_CONSTRUCTOR)
                        node.addChild(primaryCtorNode, node.findChildByType(CLASS_BODY))
                        val valueParameterList = CompositeElement(VALUE_PARAMETER_LIST)
                        primaryCtorNode.addChild(valueParameterList)
                        valueParameterList.addChild(LeafPsiElement(LPAR, "("))
                        declarationsAssignedInCtor.forEachIndexed { index, ktProperty ->
                            valueParameterList.addChild(ktProperty.node.clone() as ASTNode, null)
                            if (index != declarationsAssignedInCtor.size - 1) {
                                valueParameterList.addChild(LeafPsiElement(COMMA, ","), null)
                            }
                            ktProperty.node.run { treeParent.removeChild(this) }
                        }
                        valueParameterList.addChild(LeafPsiElement(RPAR, ")"))

                        node.findChildByType(CLASS_BODY)?.removeChild(secondaryCtor)

                        // move all other operations to init block
                    }
                }
        }
    }
}
