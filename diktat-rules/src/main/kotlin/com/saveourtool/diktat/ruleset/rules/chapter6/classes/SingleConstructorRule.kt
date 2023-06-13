package com.saveourtool.diktat.ruleset.rules.chapter6.classes

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.SINGLE_CONSTRUCTOR_SHOULD_BE_PRIMARY
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.KotlinParser
import com.saveourtool.diktat.ruleset.utils.findChildrenMatching
import com.saveourtool.diktat.ruleset.utils.getAllChildrenWithType
import com.saveourtool.diktat.ruleset.utils.getIdentifierName
import com.saveourtool.diktat.ruleset.utils.hasChildOfType
import com.saveourtool.diktat.ruleset.utils.isGoingAfter
import com.saveourtool.diktat.ruleset.utils.nextCodeSibling

import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.KtNodeTypes.CLASS_BODY
import org.jetbrains.kotlin.KtNodeTypes.MODIFIER_LIST
import org.jetbrains.kotlin.KtNodeTypes.PRIMARY_CONSTRUCTOR
import org.jetbrains.kotlin.KtNodeTypes.SECONDARY_CONSTRUCTOR
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.KDOC
import org.jetbrains.kotlin.lexer.KtTokens.BLOCK_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.psiUtil.asAssignment
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/**
 * This rule ensures that if a class has a single constructor, this constructor is primary.
 * Secondary constructor is converted into primary, statements that are not assignments are moved into an `init` block.
 */
class SingleConstructorRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(SINGLE_CONSTRUCTOR_SHOULD_BE_PRIMARY)
) {
    private val kotlinParser by lazy { KotlinParser() }

    override fun logic(node: ASTNode) {
        if (node.elementType == CLASS) {
            handleClassConstructors(node)
        }
    }

    private fun handleClassConstructors(node: ASTNode) {
        if (!node.hasChildOfType(PRIMARY_CONSTRUCTOR)) {
            // class has no primary constructor, need to count secondary constructors
            node
                .findChildByType(CLASS_BODY)
                ?.getAllChildrenWithType(SECONDARY_CONSTRUCTOR)
                ?.singleOrNull()
                ?.let { secondaryCtor ->
                    SINGLE_CONSTRUCTOR_SHOULD_BE_PRIMARY.warnAndFix(
                        configRules, emitWarn, isFixMode, "in class <${node.getIdentifierName()?.text}>",
                        node.startOffset, node
                    ) {
                        convertConstructorToPrimary(node, secondaryCtor)
                    }
                }
        }
    }

    /**
     * This method does the following:
     * - Inside the single secondary constructor find all assignments.
     * - Some of assigned values will have `this` qualifier, they are definitely class properties.
     * - For other assigned variables that are not declared in the same scope we check if they are properties and whether they depend only on constructor parameters.
     * - Create primary constructor moving all properties that we collected.
     * - Create init block with other statements from the secondary constructor, including initialization of properties that require local variables or complex calls.
     * - Finally, remove the secondary constructor.
     */
    @Suppress(
        "GENERIC_VARIABLE_WRONG_DECLARATION",
        "TOO_LONG_FUNCTION"
    )
    private fun convertConstructorToPrimary(classNode: ASTNode, secondaryCtor: ASTNode) {
        val secondaryCtorArguments = (secondaryCtor.psi as KtSecondaryConstructor).valueParameters

        // split all statements into assignments and all other statements (including comments)
        val (assignments, otherStatements) = (secondaryCtor.psi as KtSecondaryConstructor)
            .bodyBlockExpression
            ?.statements
            ?.partition { it is KtBinaryExpression && it.asAssignment() != null }
            ?.run { first.map { it as KtBinaryExpression } to second }
            ?: (emptyList<KtBinaryExpression>() to emptyList())

        val comments = (secondaryCtor.psi as KtSecondaryConstructor)
            .bodyBlockExpression
            ?.findChildrenMatching { it.elementType == EOL_COMMENT || it.elementType == BLOCK_COMMENT || it.elementType == KDOC }
            ?.associate {
                it.text to it.nextCodeSibling()
            }
            ?.filterValues { it != null }

        val classProperties = (classNode.psi as KtClass).getProperties()
        val localProperties = secondaryCtor.psi.collectDescendantsOfType<KtProperty> { it.isLocal }
        // find all references to class properties that are getting assigned in a constructor
        val assignmentsToReferences = assignments.associateWithAssignedReference(localProperties, classProperties)

        // Split all assignments into trivial (that are just assigned from a constructor parameter) and non-trivial.
        // Logic for non-trivial assignments should than be kept and moved into a dedicated `init` block.
        val (trivialAssignments, nonTrivialAssignments) = assignmentsToReferences
            .toList()
            .partition { (assignment, _) ->
                assignment.right.let { rhs ->
                    rhs is KtNameReferenceExpression && rhs.getReferencedName() in secondaryCtorArguments.map { it.name }
                }
            }
            .let { it.first.toMap() to it.second.toMap() }

        // find corresponding properties' declarations
        val declarationsAssignedInCtor = trivialAssignments
            .mapNotNull { (_, reference) ->
                (classNode.psi as KtClass).getProperties()
                    .firstOrNull { it.nameIdentifier?.text == reference.getReferencedName() }
            }
            .distinct()

        // future init body
        val expressions = (secondaryCtor.psi as KtSecondaryConstructor)
            .bodyBlockExpression
            ?.statements
            ?.map { it.text }
            ?.filter { expr -> expr in otherStatements.map { it.text } || expr in nonTrivialAssignments.keys.map { it.text } }
            ?: emptyList()

        classNode.convertSecondaryConstructorToPrimary(secondaryCtor, declarationsAssignedInCtor, nonTrivialAssignments, otherStatements, comments, expressions)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun List<KtBinaryExpression>.associateWithAssignedReference(localProperties: List<KtProperty>, classProperties: List<KtProperty>) =
        associateWith {
            // non-null assert is safe because of predicate in partitioning
            it.asAssignment()!!.left!!
        }
            .filterValues { left ->
                // we keep only statements where property is referenced via this (like `this.foo = ...`)
                left is KtDotQualifiedExpression && left.receiverExpression is KtThisExpression && left.selectorExpression is KtNameReferenceExpression ||
                        // or directly (like `foo = ...`)
                        left is KtNameReferenceExpression && localProperties.none {
                            // check for shadowing
                            left.node.isGoingAfter(it.node) && it.name == left.name
                        }
            }
            .mapValues { (_, left) ->
                when (left) {
                    is KtDotQualifiedExpression -> left.selectorExpression as KtNameReferenceExpression
                    is KtNameReferenceExpression -> left
                    else -> error("Unexpected psi class ${left::class} with text ${left.text}")
                }
            }
            .filterValues { left -> left.getReferencedName() in classProperties.mapNotNull { it.name } }

    @Suppress(
        "NestedBlockDepth",
        "GENERIC_VARIABLE_WRONG_DECLARATION",
        "TOO_LONG_FUNCTION",
        "TOO_MANY_PARAMETERS",
        "LongParameterList",
    )
    private fun ASTNode.convertSecondaryConstructorToPrimary(
        secondaryCtor: ASTNode,
        declarationsAssignedInCtor: List<KtProperty>,
        nonTrivialAssignments: Map<KtBinaryExpression, KtNameReferenceExpression>,
        otherStatements: List<KtExpression>,
        comments: Map<String, ASTNode?>?,
        initBody: List<String>
    ) {
        require(elementType == CLASS)

        val localProperties = secondaryCtor.psi.collectDescendantsOfType<KtProperty> { it.isLocal }
        // find all arguments that are not directly assigned into properties
        val nonTrivialSecondaryCtorParameters = getNonTrivialParameters(secondaryCtor, nonTrivialAssignments.keys, localProperties)

        val primaryCtorNode = createPrimaryCtor(secondaryCtor, declarationsAssignedInCtor, nonTrivialSecondaryCtorParameters)

        val newArgumentListOfSecondaryCtor: List<KtParameter> = (secondaryCtor.psi as KtSecondaryConstructor)
            .valueParameters
            .filter { arg -> arg.name !in nonTrivialSecondaryCtorParameters.map { it.name } }  // get rid of ctor arguments
            .filter { arg -> arg.name !in declarationsAssignedInCtor.map { it.name } }  // get rid of ctor arguments
            .filter { arg -> initBody.any { expr -> arg.name.toString() in expr } }  // get rid of parameters that do not appear in text

        if (newArgumentListOfSecondaryCtor.isNotEmpty()) {
            return
        }

        addChild(primaryCtorNode, findChildByType(CLASS_BODY))
        declarationsAssignedInCtor.forEach { ktProperty ->
            ktProperty.node.run {
                treePrev.takeIf { it.elementType == WHITE_SPACE }?.let { treeParent.removeChild(it) }
                treeParent.removeChild(this)
            }
        }

        // adding comments to init body
        val initBodyWithComments = initBody.toMutableList()
        comments?.forEach { (comment, nextExpression) ->
            if (initBodyWithComments.indexOf(nextExpression?.text) != -1) {
                initBodyWithComments.add(initBodyWithComments.indexOf(nextExpression?.text), comment)
            }
        }

        if (otherStatements.isNotEmpty() || nonTrivialAssignments.isNotEmpty()) {
            findChildByType(CLASS_BODY)?.run {
                val classInitializer = kotlinParser.createNodeForInit(
                    """|init {
                       |    ${initBodyWithComments.joinToString("\n")}
                       |}
                    """.trimMargin())
                addChild(classInitializer, secondaryCtor)
                addChild(PsiWhiteSpaceImpl("\n"), secondaryCtor)
            }
        }

        secondaryCtor
            .run { treePrev.takeIf { it.elementType == WHITE_SPACE } ?: treeNext }
            .takeIf { it.elementType == WHITE_SPACE }
            ?.run { treeParent.removeChild(this) }
        findChildByType(CLASS_BODY)?.removeChild(secondaryCtor)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun getNonTrivialParameters(secondaryCtor: ASTNode,
                                        nonTrivialAssignments: Collection<KtBinaryExpression>,
                                        localProperties: List<KtProperty>
    ) = (secondaryCtor.psi as KtSecondaryConstructor)
        .valueParameters.run {
            val dependencies = nonTrivialAssignments
                .flatMap { it.left!!.collectDescendantsOfType<KtNameReferenceExpression>() }
                .filterNot { ref ->
                    localProperties.any { ref.node.isGoingAfter(it.node) && ref.getReferencedName() == it.name }
                }
                .map { it.getReferencedName() }
            filter {
                it.name in dependencies
            }
        }

    private fun createPrimaryCtor(secondaryCtor: ASTNode,
                                  declarationsAssignedInCtor: List<KtProperty>,
                                  valueParameters: List<KtParameter>
    ) = kotlinParser.createPrimaryConstructor(
        (secondaryCtor
            .findChildByType(MODIFIER_LIST)
            ?.text
            ?.plus(" constructor ")
            ?: "") +
                "(" +
                declarationsAssignedInCtor.run {
                    joinToString(
                        ", ",
                        postfix = if (isNotEmpty() && valueParameters.isNotEmpty()) ", " else ""
                    ) { it.text }
                } +
                valueParameters.joinToString(", ") { it.text } +
                ")"
    )
        .node

    companion object {
        const val NAME_ID = "single-constructor"
    }
}
