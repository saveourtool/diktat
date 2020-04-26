package rri.fixbot.ruleset.huawei

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import rri.fixbot.ruleset.huawei.constants.Warnings.*
import rri.fixbot.ruleset.huawei.huawei.utils.*

/**
 * This visitor covers rule 1.2 of Huawei code style. It covers following rules:
 * 1) All identifiers should use only ASCII letters or digits, and the names should match regular expressions \w{2,64}
 *  exceptions: variables like i,j,k
 * 2) constants from object companion should have UPPER_SNAKE_CASE
 * 3) fields/variables should have lowerCamelCase and should not contain prefixes
 * 4) interfaces/classes/annotations/enums names should be in PascalCase
 *
 */
class IdentifierNaming1s2r : Rule("identifier-naming") {

    companion object {
        // FixMe: this should be moved to properties
        val ONE_CHAR_IDENTIFIERS = setOf("i", "j", "k", "x", "y", "z")
    }

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {

        // isVariable will be used in future like a workaround to check corner case with variables that have length == 1
        val (identifierNodes, isVariable) = when (node.elementType) {
            // covers interface, class, enum class and annotation class names
            ElementType.CLASS -> Pair(checkCLassName(node, autoCorrect, emit), false)
            // covers variables and constants
            ElementType.PROPERTY -> Pair(checkVariableName(node, autoCorrect, emit), true)
            // covers enum values
            ElementType.ENUM_ENTRY -> Pair(checkEnumValues(node, autoCorrect, emit), false)
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

        if (!ONE_CHAR_IDENTIFIERS.contains(variableName!!.text)) {

            // generally variables with prefixes are not allowed (like mVariable)
            if (variableName.text.hasPrefix()) {
                emit(variableName.startOffset,
                    "${VARIABLE_HAS_PREFIX.text} ${variableName.text}",
                    true
                )
            }

            // check for constant variables - check for val from companion object
            // it should be in UPPER_CASE
            if (node.isVariableFromCompanionObject() && node.isValProperty()) {
                if (!variableName.text.isUpperSnakeCase()) {
                    emit(variableName.startOffset,
                        "${CONSTANT_COMPANION_UPPERCASE.text} ${variableName.text}",
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

            // variable should not contain only one letter in it's name. This is a bad example: b512
            if (variableName.text.containsOneLetterOrZero()) {
                emit(variableName.startOffset,
                    "${VARIABLE_NAME_INCORRECT.text} ${variableName.text}",
                    true)
            }
        }
        return listOf(variableName)
    }

    /**
     * basic check for class naming (PascalCase)
     */
    private fun checkCLassName(node: ASTNode,
                               autoCorrect: Boolean,
                               emit: (offset: Int, errorMessage: String,
                                      canBeAutoCorrected: Boolean) -> Unit): List<ASTNode> {
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
}
