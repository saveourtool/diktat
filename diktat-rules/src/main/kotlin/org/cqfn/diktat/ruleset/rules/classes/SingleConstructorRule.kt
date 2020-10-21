package org.cqfn.diktat.ruleset.rules.classes

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.SECONDARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.SINGLE_CONSTRUCTOR_SHOULD_BE_PRIMARY
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getIdentifierName
import org.cqfn.diktat.ruleset.utils.isGoingAfter
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.psiUtil.asAssignment

class SingleConstructorRule(private val config: List<RulesConfig>) : Rule("single-constructor") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false
    private val kotlinParser by lazy { KotlinParser() }

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
                        node.startOffset, node) {
                        convertConstructorToPrimary(node, secondaryCtor)
                    }
                }
        }
    }

    /**
     * This method does the following:
     * - Inside the single secondary constructor find all assignments.
     * - Some of assigned values will have `this` qualifier, they are definitely class properties.
     * - For other assigned variables that are not declared in the same scope we check if they are properties.
     * - Create primary constructor moving all properties that we collected.
     * - Create init block with other statements from the secondary constructor.
     * - Finally, remove the secondary constructor.
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun convertConstructorToPrimary(classNode: ASTNode, secondaryCtor: ASTNode) {
        val (assignments, otherStatements) = (secondaryCtor.psi as KtSecondaryConstructor)
            .bodyBlockExpression
            ?.statements
            ?.partition { (it as? KtBinaryExpression)?.asAssignment() != null }
            ?: emptyList<KtExpression>() to emptyList()

        val (qualifiedAssignedProperties, assignedProperties) = assignments
            .map {
                // non-null assert is safe because of predicate in partitioning
                (it as KtBinaryExpression).asAssignment()!!.left!!
            }
            .filter {
                // todo use secondaryCtor.findAllVariablesWithAssignments()
                (it as? KtDotQualifiedExpression)?.run {
                    receiverExpression is KtThisExpression && selectorExpression is KtNameReferenceExpression
                } ?: false ||
                        it is KtNameReferenceExpression
            }
            .partition { it is KtDotQualifiedExpression }
            .let { (qualified, simple) ->
                qualified.map { (it as KtDotQualifiedExpression).selectorExpression as KtNameReferenceExpression } to
                        simple.map { it as KtNameReferenceExpression }
            }

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
                (classNode.psi as KtClass).getProperties()
                    .firstOrNull { it.nameIdentifier?.text == reference.getReferencedName() }
            }
            .distinct()

        classNode.convertSecondaryConstructorToPrimary(secondaryCtor, declarationsAssignedInCtor, otherStatements)
    }

    private fun ASTNode.convertSecondaryConstructorToPrimary(secondaryCtor: ASTNode,
                                                             declarationsAssignedInCtor: List<KtProperty>,
                                                             otherStatements: List<KtExpression>) {
        require(elementType == CLASS)

        val primaryCtorNode = kotlinParser.createPrimaryConstructor("(${declarationsAssignedInCtor.joinToString(", ") { it.text }})").node
        addChild(primaryCtorNode, findChildByType(CLASS_BODY))
        declarationsAssignedInCtor.forEach { ktProperty ->
            ktProperty.node.let { treeParent.removeChild(it) }
        }

        if (otherStatements.isNotEmpty()) {
            findChildByType(CLASS_BODY)?.run {
                val beforeNode = findChildByType(LBRACE)!!.let { if (it.treeNext.elementType == WHITE_SPACE) it.treeNext else it }
                val classInitializer = kotlinParser.createNode(
                    """
                    |init {
                    |    ${otherStatements.joinToString("\n") { it.text }}
                    |}
                """.trimMargin()
                )
                addChild(PsiWhiteSpaceImpl("\n"), beforeNode)
                addChild(classInitializer, beforeNode)
            }
        }

        findChildByType(CLASS_BODY)?.removeChild(secondaryCtor)
    }
}
