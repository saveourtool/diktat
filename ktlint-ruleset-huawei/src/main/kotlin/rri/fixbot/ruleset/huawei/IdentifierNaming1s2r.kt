package rri.fixbot.ruleset.huawei

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import rri.fixbot.ruleset.huawei.constants.Warnings.*
import rri.fixbot.ruleset.huawei.huawei.utils.*

/**
 * This visitor covers rule 1.2 of Huawei code style. It covers following rules:
 * 1) All identifiers should use only ASCII letters or digits, and the names should match regular expressions \w{2,64}
 *  exceptions: variables like i,j,k
 * 2) constants from object companion should have UPPER_SNAKE_CASE
 * 3) fields/variables should have lowerCamelCase and should not contain prefixes
 * 4) interfaces/classes/annotations/enums/object names should be in PascalCase
 *
 */
class IdentifierNaming1s2r : Rule("identifier-naming") {

    companion object {
        // FixMe: this should be moved to properties
        val ONE_CHAR_IDENTIFIERS = setOf("i", "j", "k", "x", "y", "z")
        val BOOLEAN_METHOD_PREFIXES = setOf("has", "is")
    }

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        params: KtLint.Params,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {

        // isVariable will be used in future like a workaround to check corner case with variables that have length == 1
        val (identifierNodes, isVariable) = when (node.elementType) {
            // covers interface, class, enum class and annotation class names
            ElementType.CLASS -> Pair(checkCLassNamings(node, autoCorrect, emit), false)
            // covers "object" code blocks
            ElementType.OBJECT_DECLARATION -> Pair(checkObjectNaming(node, autoCorrect, emit), false)
            // covers variables and constants
            ElementType.PROPERTY, ElementType.VALUE_PARAMETER -> Pair(checkVariableName(node, autoCorrect, emit), true)
            // covers case of enum values
            ElementType.ENUM_ENTRY -> Pair(checkEnumValues(node, autoCorrect, emit), false)
            // covers global functions, extensions and class methods
            ElementType.FUN -> Pair(checkFunctionName(node, autoCorrect, emit), false)
            // covers arguments of functionsa and constructors/declaration of classes
            else -> Pair(null, true)
        }

        if (identifierNodes != null) {
            checkIdentifierLength(identifierNodes, isVariable, autoCorrect, emit)
        }
    }

    /**
     * all checks for case and naming for vals/vars/constants from companion object
     */
    private fun checkVariableName(node: ASTNode,
                                  autoCorrect: Boolean,
                                  emit: (offset: Int, errorMessage: String,
                                         canBeAutoCorrected: Boolean) -> Unit): List<ASTNode> {
        val variableName: ASTNode? = node.getIdentifierName()

        // no need to do checks if variables are in a special list with exceptions
        if (!ONE_CHAR_IDENTIFIERS.contains(variableName!!.text)) {
            // generally variables with prefixes are not allowed (like mVariable)
            if (variableName.text.hasPrefix()) {
                emit(variableName.startOffset,
                    "${VARIABLE_HAS_PREFIX.text} ${variableName.text}",
                    true
                )
            }

            // variable should not contain only one letter in it's name. This is a bad example: b512
            // but no need to raise a warning here if length of a variable. In this case we will raise IDENTIFIER_LENGTH
            if (variableName.text.containsOneLetterOrZero() && variableName.text.length > 1) {
                emit(variableName.startOffset,
                    "${VARIABLE_NAME_INCORRECT.text} ${variableName.text}",
                    true)
            }

            // check for constant variables - check for val from companion object or on global file level
            // it should be in UPPER_CASE, no need to raise this warning if it is one-letter variable
            if ((node.isNodeFromCompanionObject() || node.isNodeFromFileLevel()) && node.isValProperty()) {
                if (!variableName.text.isUpperSnakeCase() && variableName.text.length > 1) {
                    emit(variableName.startOffset,
                        "${CONSTANT_UPPERCASE.text} ${variableName.text}",
                        true
                    )
                }
                return listOf(variableName)
            }

            // variable name should be in camel case. The only exception is a list of industry standard variables like i, j, k.
            if (!variableName.text.isLowerCamelCase()) {
                emit(variableName.startOffset,
                    "${VARIABLE_NAME_INCORRECT_FORMAT.text} ${variableName.text}",
                    true
                )
            }

        }
        return listOf(variableName)
    }

    /**
     * basic check for class naming (PascalCase)
     * and checks for generic type declared for this class
     */
    private fun checkCLassNamings(node: ASTNode,
                                  autoCorrect: Boolean,
                                  emit: (offset: Int, errorMessage: String,
                                         canBeAutoCorrected: Boolean) -> Unit): List<ASTNode> {
        val genericType: ASTNode? = node.getTypeParameterList()
        if (genericType != null && !validGenericTypeName(genericType.text)) {
            emit(genericType.startOffset,
                "${GENERIC_NAME.text} ${genericType.text}",
                true
            )
        }

        val className: ASTNode? = node.getIdentifierName()
        if (!(className!!.text.isUpperCamelCase())) {
            emit(className.startOffset,
                "${CLASS_NAME_INCORRECT.text} ${className.text}",
                true
            )
        }

        return listOf(className)
    }

    /**
     * basic check for object naming of code blocks (PascalCase)
     *
     */
    private fun checkObjectNaming(node: ASTNode,
                                  autoCorrect: Boolean,
                                  emit: (offset: Int, errorMessage: String,
                                         canBeAutoCorrected: Boolean) -> Unit): List<ASTNode> {
        val objectName: ASTNode? = node.getIdentifierName()
        // checking object naming, the only extension is "companion" keyword
        // FixMe: need to find a constant with "companion" string in Kotlin core and remove hardcode
        if (!(objectName!!.text.isUpperCamelCase()) && objectName.text != "companion") {
            emit(objectName.startOffset,
                "${OBJECT_NAME_INCORRECT.text} ${objectName.text}",
                true
            )
        }

        return listOf(objectName)
    }

    /**
     * check that Enum values match correct case and style
     * node has ENUM_ENTRY type
     * to check all variables will need to check all IDENTIFIERS in ENUM_ENTRY
     */
    private fun checkEnumValues(node: ASTNode,
                                autoCorrect: Boolean,
                                emit: (offset: Int, errorMessage: String,
                                       canBeAutoCorrected: Boolean) -> Unit): List<ASTNode> {
        val enumValues: List<ASTNode> = node.getChildren(null).filter { it.elementType == ElementType.IDENTIFIER }
        enumValues.forEach { value ->
            if (!value.text.isUpperSnakeCase()) {
                emit(value.startOffset,
                    "${ENUM_VALUE.text} ${value.text}",
                    true
                )
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
    private fun checkFunctionName(node: ASTNode,
                                  autoCorrect: Boolean,
                                  emit: (offset: Int, errorMessage: String,
                                         canBeAutoCorrected: Boolean) -> Unit): List<ASTNode> {
        val functionName = node.getIdentifierName()

        // basic check for camel case
        if (!functionName!!.text.isLowerCamelCase()) {
            emit(functionName.startOffset,
                "${FUNCTION_NAME_INCORRECT_CASE.text} ${functionName.text}",
                true
            )
        }

        // check for methods that return Boolean
        val functionReturnType = node.findChildAfter(VALUE_PARAMETER_LIST, TYPE_REFERENCE)?.text

        // if function has Boolean return type in 99% of cases it is much better to name it with isXXX or hasXXX prefix
        if (functionReturnType != null && functionReturnType == PrimitiveType.BOOLEAN.typeName.asString()) {
            if (!(BOOLEAN_METHOD_PREFIXES.any { functionReturnType.startsWith(it) })) {
                emit(functionName.startOffset,
                    "${FUNCTION_BOOLEAN_PREFIX.text} ${functionName.text}",
                    true
                )
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
                                      isVariable: Boolean,
                                      autoCorrect: Boolean,
                                      emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        nodes.forEach {
            if (!(it.checkLength(2..64) || (ONE_CHAR_IDENTIFIERS.contains(it.text)) && isVariable)) {
                emit(it.startOffset,
                    "${IDENTIFIER_LENGTH.text} ${it.text}",
                    true
                )
            }
        }
    }
}
