package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.cqfn.diktat.ruleset.constants.Warnings.*
import org.cqfn.diktat.ruleset.utils.*
import kotlin.text.toUpperCase

/**
 * This visitor covers rules:  1.2, 1.3, 1.4, 1.5 of Huawei code style. It covers following rules:
 * 1) All identifiers should use only ASCII letters or digits, and the names should match regular expressions \w{2,64}
 *  exceptions: variables like i,j,k
 * 2) constants from object companion should have UPPER_SNAKE_CASE
 * 3) fields/variables should have lowerCamelCase and should not contain prefixes
 * 4) interfaces/classes/annotations/enums/object names should be in PascalCase
 * 5) methods: function names should be in camel case, methods that return boolean value should have "is"/"has" prefix
 * 6) custom exceptions: PascalCase and Exception suffix
 * 7) FixMe: should prohibit identifiers with free format with `` (except test functions)
 */
class IdentifierNaming : Rule("identifier-naming") {

    companion object {
        // FixMe: this should be moved to properties
        val ONE_CHAR_IDENTIFIERS = setOf("i", "j", "k", "x", "y", "z")
        val BOOLEAN_METHOD_PREFIXES = setOf("has", "is")
    }

    private lateinit var confiRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        params: KtLint.Params,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        confiRules = params.rulesConfigList!!
        isFixMode = autoCorrect
        emitWarn = emit

        // isVariable will be used in future like a workaround to check corner case with variables that have length == 1
        val (identifierNodes, isVariable) = when (node.elementType) {
            // covers interface, class, enum class and annotation class names
            ElementType.CLASS -> Pair(checkCLassNamings(node), false)
            // covers "object" code blocks
            ElementType.OBJECT_DECLARATION -> Pair(checkObjectNaming(node), false)
            // covers variables and constants
            ElementType.PROPERTY, ElementType.VALUE_PARAMETER -> Pair(checkVariableName(node), true)
            // covers case of enum values
            ElementType.ENUM_ENTRY -> Pair(checkEnumValues(node), false)
            // covers global functions, extensions and class methods
            ElementType.FUN -> Pair(checkFunctionName(node), false)
            // covers arguments of functions and constructors/declaration of classes
            else -> Pair(null, true)
        }

        if (identifierNodes != null) {
            checkIdentifierLength(identifierNodes, isVariable)
        }
    }

    /**
     * all checks for case and naming for vals/vars/constants from companion object
     */
    private fun checkVariableName(node: ASTNode): List<ASTNode> {
        val variableName: ASTNode? = node.getIdentifierName()

        // no need to do checks if variables are in a special list with exceptions
        if (!ONE_CHAR_IDENTIFIERS.contains(variableName!!.text)) {
            // generally variables with prefixes are not allowed (like mVariable)
            if (variableName.text.hasPrefix()) {
                VARIABLE_HAS_PREFIX.warnAndFix(confiRules, emitWarn, isFixMode, variableName.text, variableName.startOffset) {
                    // FixMe: this correction should be done only after we checked variable case (below)
                    (variableName as LeafPsiElement).replaceWithText(variableName.text.removePrefix())
                }
            }

            // variable should not contain only one letter in it's name. This is a bad example: b512
            // but no need to raise a warning here if length of a variable. In this case we will raise IDENTIFIER_LENGTH
            if (variableName.text.containsOneLetterOrZero() && variableName.text.length > 1) {
                VARIABLE_NAME_INCORRECT.warnAndFix(confiRules, emitWarn, isFixMode, variableName.text, variableName.startOffset) {
                }
            }

            // check for constant variables - check for val from companion object or on global file level
            // it should be in UPPER_CASE, no need to raise this warning if it is one-letter variable
            if ((node.isNodeFromCompanionObject() || node.isNodeFromFileLevel()) && node.isValProperty() && node.isConst()) {
                if (!variableName.text.isUpperSnakeCase() && variableName.text.length > 1) {
                    CONSTANT_UPPERCASE.warnAndFix(confiRules, emitWarn, isFixMode, variableName.text, variableName.startOffset) {
                        (variableName as LeafPsiElement).replaceWithText(variableName.text.toUpperCase())
                    }
                }
                return listOf(variableName)
            }

            // variable name should be in camel case. The only exception is a list of industry standard variables like i, j, k.
            if (!variableName.text.isLowerCamelCase()) {
                VARIABLE_NAME_INCORRECT_FORMAT.warnAndFix(confiRules, emitWarn, isFixMode, variableName.text, variableName.startOffset) {
                    // FixMe: cover fixes with tests
                    (variableName as LeafPsiElement).replaceWithText(variableName.text.toLowerCamelCase())
                }
            }

        }
        return listOf(variableName)
    }

    /**
     * basic check for class naming (PascalCase)
     * and checks for generic type declared for this class
     */
    private fun checkCLassNamings(node: ASTNode): List<ASTNode> {
        val genericType: ASTNode? = node.getTypeParameterList()
        if (genericType != null && !validGenericTypeName(genericType.text)) {
            GENERIC_NAME.warnAndFix(confiRules, emitWarn, isFixMode, genericType.text, genericType.startOffset) {
                // FixMe: should fix generic name here
            }
        }

        val className: ASTNode? = node.getIdentifierName()
        if (!(className!!.text.isPascalCase())) {
            CLASS_NAME_INCORRECT.warnAndFix(confiRules, emitWarn, isFixMode, className.text, className.startOffset) {
                (className as LeafPsiElement).replaceWithText(className.text.toPascalCase())
            }
        }

        checkExceptionSuffix(node)

        return listOf(className)
    }

    /**
     * all exceptions should have Exception suffix
     *
     */
    private fun checkExceptionSuffix(node: ASTNode) {

        fun hasExceptionSuffix(text: String) = text.toLowerCase().endsWith("exception")

        val classNameNode: ASTNode? = node.getIdentifierName()
        // getting super class name
        val superClassName: String? = node
            .getFirstChildWithType(ElementType.SUPER_TYPE_LIST)
            ?.findLeafWithSpecificType(TYPE_REFERENCE)
            ?.text

        if (superClassName != null && hasExceptionSuffix(superClassName) && !hasExceptionSuffix(classNameNode!!.text)) {
            EXCEPTION_SUFFIX.warnAndFix(confiRules, emitWarn, isFixMode, classNameNode.text, classNameNode.startOffset) {
                // FixMe: need to add tests for this
                (classNameNode as LeafPsiElement).replaceWithText(classNameNode.text + "Exception")
            }
        }
    }

    /**
     * basic check for object naming of code blocks (PascalCase)
     * fix: fixing object name to PascalCase
     */
    private fun checkObjectNaming(node: ASTNode): List<ASTNode> {
        val objectName: ASTNode? = node.getIdentifierName()
        // checking object naming, the only extension is "companion" keyword
        if (!(objectName!!.text.isPascalCase()) && objectName.text != "companion") {
            OBJECT_NAME_INCORRECT.warnAndFix(confiRules, emitWarn, isFixMode, objectName.text, objectName.startOffset) {
                (objectName as LeafPsiElement).replaceWithText(objectName.text.toPascalCase())

            }
        }
        return listOf(objectName)
    }

    /**
     * check that Enum values match correct case and style
     * node has ENUM_ENTRY type
     * to check all variables will need to check all IDENTIFIERS in ENUM_ENTRY
     */
    private fun checkEnumValues(node: ASTNode): List<ASTNode> {
        val enumValues: List<ASTNode> = node.getChildren(null).filter { it.elementType == ElementType.IDENTIFIER }
        enumValues.forEach { value ->
            if (!value.text.isUpperSnakeCase()) {
                ENUM_VALUE.warnAndFix(confiRules, emitWarn, isFixMode, value.text, value.startOffset) {
                    // FixMe: add tests for this
                    (value as LeafPsiElement).replaceWithText(value.text.toUpperSnakeCase())
                }
            }
        }
        return enumValues
    }

    /**
     * Check function name:
     * 1) function names should be in camel case
     * 2) methods that return boolean value should have "is"/"has" prefix
     * 3) FixMe: The function name is usually a verb or verb phrase (need to add check/fix for it)
     */
    private fun checkFunctionName(node: ASTNode): List<ASTNode> {
        val functionName = node.getIdentifierName()

        // basic check for camel case
        if (!functionName!!.text.isLowerCamelCase()) {
            FUNCTION_NAME_INCORRECT_CASE.warnAndFix(confiRules, emitWarn, isFixMode, functionName.text, functionName.startOffset) {
                // FixMe: add tests for this
                (functionName as LeafPsiElement).replaceWithText(functionName.text.toLowerCamelCase())
            }
        }

        // check for methods that return Boolean
        val functionReturnType = node.findChildAfter(VALUE_PARAMETER_LIST, TYPE_REFERENCE)?.text

        // if function has Boolean return type in 99% of cases it is much better to name it with isXXX or hasXXX prefix
        if (functionReturnType != null && functionReturnType == PrimitiveType.BOOLEAN.typeName.asString()) {
            if (!(BOOLEAN_METHOD_PREFIXES.any { functionReturnType.startsWith(it) })) {
                FUNCTION_BOOLEAN_PREFIX.warnAndFix(confiRules, emitWarn, isFixMode, functionName.text, functionName.startOffset) {
                    // FixMe: add agressive autofix for this
                }
            }
        }

        return listOf(functionName)
    }


    /**
     * check that generic name has single capital letter, can be followed by a number
     * this method will check it for both generic classes and generic methods
     */
    private fun validGenericTypeName(generic: String): Boolean {
        // removing whitespaces and <>
        val genericName = generic.replace(">", "").replace("<", "").trim()
        // first letter should always be a capital
        return genericName[0] in 'A'..'Z' &&
            // other letters - are digits
            (genericName.length == 1 || genericName.substring(1).isDigits())
    }

    /**
     * identifier name length should not be longer than 64 symbols and shorter than 2 symbols
     */
    private fun checkIdentifierLength(nodes: List<ASTNode>,
                                      isVariable: Boolean) {
        nodes.forEach {
            if (!(it.checkLength(2..64) || (ONE_CHAR_IDENTIFIERS.contains(it.text)) && isVariable)) {
                IDENTIFIER_LENGTH.warn(confiRules, emitWarn, isFixMode, it.text, it.startOffset)
            }
        }
    }
}
