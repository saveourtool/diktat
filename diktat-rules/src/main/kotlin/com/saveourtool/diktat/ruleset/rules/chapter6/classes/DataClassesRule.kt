package com.saveourtool.diktat.ruleset.rules.chapter6.classes

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.USE_DATA_CLASS
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*

import org.jetbrains.kotlin.KtNodeTypes.BLOCK
import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.KtNodeTypes.CLASS_BODY
import org.jetbrains.kotlin.KtNodeTypes.CLASS_INITIALIZER
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.MODIFIER_LIST
import org.jetbrains.kotlin.KtNodeTypes.PRIMARY_CONSTRUCTOR
import org.jetbrains.kotlin.KtNodeTypes.PROPERTY
import org.jetbrains.kotlin.KtNodeTypes.PROPERTY_ACCESSOR
import org.jetbrains.kotlin.KtNodeTypes.REFERENCE_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.SUPER_TYPE_LIST
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens.OPEN_KEYWORD
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.psiUtil.isAbstract

/**
 * This rule checks if class can be made as data class
 */
class DataClassesRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(USE_DATA_CLASS)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == CLASS) {
            handleClass(node)
        }
    }

    private fun handleClass(node: ASTNode) {
        if ((node.psi as KtClass).isDefinitelyNotDataClass()) {
            return
        }

        if (node.canBeDataClass()) {
            raiseWarn(node)
        }
    }

    // fixme: Need to know types of vars and props to create data class
    private fun raiseWarn(node: ASTNode) {
        USE_DATA_CLASS.warn(configRules, emitWarn, "${(node.psi as KtClass).name}", node.startOffset, node)
    }

    @Suppress(
        "UnsafeCallOnNullableType",
        "FUNCTION_BOOLEAN_PREFIX",
        "ComplexMethod"
    )
    private fun ASTNode.canBeDataClass(): Boolean {
        val isNotPropertyInClassBody = findChildByType(CLASS_BODY)?.let { (it.psi as KtClassBody).properties.isEmpty() } ?: true
        val constructorParametersNames: MutableList<String> = mutableListOf()
        val hasPropertyInConstructor = findChildByType(PRIMARY_CONSTRUCTOR)
            ?.let { constructor ->
                (constructor.psi as KtPrimaryConstructor)
                    .valueParameters
                    .onEach {
                        if (!it.hasValOrVar()) {
                            constructorParametersNames.add(it.name!!)
                        }
                    }
                    .run { isNotEmpty() && all { it.hasValOrVar() } }
            } ?: false
        if (isNotPropertyInClassBody && !hasPropertyInConstructor) {
            return false
        }
        // if parameter of the primary constructor is used in init block then it is hard to refactor this class to data class
        if (constructorParametersNames.isNotEmpty()) {
            val initBlocks = findChildByType(CLASS_BODY)?.getAllChildrenWithType(CLASS_INITIALIZER)
            initBlocks?.forEach { init ->
                val refExpressions = init.findAllDescendantsWithSpecificType(REFERENCE_EXPRESSION)
                if (refExpressions.any { it.text in constructorParametersNames }) {
                    return false
                }
            }
        }
        return hasAppropriateClassBody()
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun ASTNode.hasAppropriateClassBody(): Boolean {
        val classBody = getFirstChildWithType(CLASS_BODY)
        if (hasChildOfType(MODIFIER_LIST)) {
            val list = getFirstChildWithType(MODIFIER_LIST)!!
            return list.getChildren(null)
                .none { it.elementType in badModifiers } &&
                    classBody?.getAllChildrenWithType(FUN)
                        ?.isEmpty()
                    ?: false &&
                    getFirstChildWithType(SUPER_TYPE_LIST) == null
        }
        return classBody?.getFirstChildWithType(FUN) == null &&
                getFirstChildWithType(SUPER_TYPE_LIST) == null &&
                // if there is any prop with logic in accessor then don't recommend to convert class to data class
                classBody?.let(::areGoodProps)
                ?: true
    }

    /**
     * Checks if any property with accessor contains logic in accessor
     */
    private fun areGoodProps(node: ASTNode): Boolean {
        val propertiesWithAccessors = node.getAllChildrenWithType(PROPERTY).filter { it.hasChildOfType(PROPERTY_ACCESSOR) }

        if (propertiesWithAccessors.isNotEmpty()) {
            return propertiesWithAccessors.any {
                val accessors = it.getAllChildrenWithType(PROPERTY_ACCESSOR)

                areGoodAccessors(accessors)
            }
        }

        return true
    }

    /**
     * We do not exclude inner classes here as if they have no
     * methods, then we definitely can refactor the code and make them data classes.
     * We only exclude: value/inline classes, enums, annotations, interfaces, abstract classes,
     * sealed classes and data classes itself. For sure there will be other corner cases,
     * for example, simple classes in Spring marked with @Entity annotation.
     * For these classes we expect users to Suppress warning manually for each corner case.
     **/
    private fun KtClass.isDefinitelyNotDataClass() =
        isValue() || isAnnotation() || isInterface() || isData() ||
                isSealed() || isInline() || isAbstract() || isEnum()

    @Suppress("UnsafeCallOnNullableType", "PARAMETER_NAME_IN_OUTER_LAMBDA")
    private fun areGoodAccessors(accessors: List<ASTNode>): Boolean {
        accessors.forEach {
            if (it.hasChildOfType(BLOCK)) {
                val block = it.getFirstChildWithType(BLOCK)!!

                return block
                    .getChildren(null)
                    .count { expr -> expr.psi is KtExpression } <= 1
            }
        }

        return true
    }

    companion object {
        const val NAME_ID = "data-classes"
        private val badModifiers = listOf(OPEN_KEYWORD)
    }
}
