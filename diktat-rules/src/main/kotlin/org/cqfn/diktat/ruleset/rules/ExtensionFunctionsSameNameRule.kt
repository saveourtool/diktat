package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.COLON
import com.pinterest.ktlint.core.ast.ElementType.DOT
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_CALL_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_LIST
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.EXTENSION_FUNCTION_SAME_SIGNATURE
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.findChildAfter
import org.cqfn.diktat.ruleset.utils.findChildBefore
import org.cqfn.diktat.ruleset.utils.findLeafWithSpecificType
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtParameterList

typealias RelatedClasses = List<Pair<String, String>>
typealias SimilarSignatures = List<Pair<ExtensionFunctionsSameNameRule.ExtensionFunction, ExtensionFunctionsSameNameRule.ExtensionFunction>>

/**
 * This rule checks if extension functions with the same signature don't have related classes
 */
class ExtensionFunctionsSameNameRule(private val configRules: List<RulesConfig>) : Rule("extension-functions-same-name") {
    private lateinit var emitWarn: EmitType
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        /**
         * 1) Collect all classes that extend other classes (collect related classes)
         * 2) Collect all extension functions with same signature
         * 3) Check if classes of functions are related
         */
        if (node.elementType == FILE) {
            val relatedClasses = collectAllRelatedClasses(node)
            val extFunctionsWithSameName = collectAllExtensionFunctions(node)
            handleFunctions(relatedClasses, extFunctionsWithSameName)
        }

    }

    // Fixme: should find all related classes in project, not only in file
    @Suppress("UnsafeCallOnNullableType")
    private fun collectAllRelatedClasses(node: ASTNode) : List<Pair<String, String>> {
        val classListWithInheritance = node.findAllNodesWithSpecificType(CLASS)
                .filterNot { (it.psi as KtClass).isInterface() }
                .filter { it.hasChildOfType(SUPER_TYPE_LIST) }

        val pairs = mutableListOf<Pair<String, String>>()
        classListWithInheritance.forEach { classNode ->
            val callEntries = classNode.findChildByType(SUPER_TYPE_LIST)!!.getAllChildrenWithType(SUPER_TYPE_CALL_ENTRY)

            callEntries.forEach { entry ->
                val className = (classNode.psi as KtClass).name!!
                val entryName = entry.findLeafWithSpecificType(IDENTIFIER)!!
                pairs.add(Pair(className, entryName.text))
            }
        }
        return pairs
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun collectAllExtensionFunctions(node: ASTNode) : SimilarSignatures {
        val extensionFunctionList = node.findAllNodesWithSpecificType(FUN).filter { it.hasChildOfType(TYPE_REFERENCE) && it.hasChildOfType(DOT) }
        val distinctFunctionSignatures = mutableMapOf<FunctionSignature, ASTNode>() // maps function signatures on node it is used by
        val extensionFunctionsPairs = mutableListOf<Pair<ExtensionFunction, ExtensionFunction>>() // pairs extension functions with same signature

        extensionFunctionList.forEach { func ->
            val functionName = (func.psi as KtNamedFunction).name!!
            // List<String> is used to show param names in warning
            val params = (func.getFirstChildWithType(VALUE_PARAMETER_LIST)!!.psi as KtParameterList).parameters.map { it.name!! }
            val returnType = func.findChildAfter(COLON, TYPE_REFERENCE)?.text
            val className = func.findChildBefore(DOT, TYPE_REFERENCE)!!.text
            val signature = FunctionSignature(functionName, params, returnType)

            if (distinctFunctionSignatures.contains(signature)) {
                val secondFuncClassName = distinctFunctionSignatures[signature]!!.findChildBefore(DOT, TYPE_REFERENCE)!!.text
                extensionFunctionsPairs.add(Pair(
                        ExtensionFunction(secondFuncClassName, signature, distinctFunctionSignatures[signature]!!),
                        ExtensionFunction(className, signature, func)))
            } else {
                distinctFunctionSignatures[signature] = func
            }
        }

        return extensionFunctionsPairs
    }

    private fun handleFunctions(relatedClasses: RelatedClasses, functions: SimilarSignatures) {
        functions.forEach {
            val firstClassName = it.first.className
            val secondClassName = it.second.className

            if (relatedClasses.hasRelatedClasses(Pair(firstClassName, secondClassName))) {
                raiseWarning(it.first.node, it.first, it.second)
                raiseWarning(it.second.node, it.first, it.second)
            }
        }
    }

    private fun RelatedClasses.hasRelatedClasses(pair: Pair<String, String>): Boolean {
        return any { it.first == pair.first && it.second == pair.second
                || it.first == pair.second && it.second == pair.first }
    }

    private fun raiseWarning(node: ASTNode, firstFunc: ExtensionFunction, secondFunc: ExtensionFunction) {
        EXTENSION_FUNCTION_SAME_SIGNATURE.warn(configRules, emitWarn, isFixMode, "$firstFunc and $secondFunc", node.startOffset, node)
    }

    data class FunctionSignature(val name: String, val parameters: List<String>, val returnType: String?) {
        override fun toString(): String {
            return "$name$parameters${if(returnType != null) ": $returnType" else ""}"
        }
    }
    data class ExtensionFunction(val className: String, val signature: FunctionSignature, val node: ASTNode) {
        override fun toString(): String {
            return "fun $className.$signature"
        }
    }
}
