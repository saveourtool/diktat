package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import org.cqfn.diktat.ruleset.rules.PackageNaming.Companion.PACKAGE_PATH_ANCHOR
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtParameterList

/**
 * Checks whether function from this [ElementType.FUN] node is from test class based on annotations and file location
 */
fun ASTNode.isTestFun(filePathParts: List<String>): Boolean {
    checkNode(this)
    val hasTestAnnotation = findChildByType(MODIFIER_LIST)?.getAllChildrenWithType(ANNOTATION_ENTRY)
            ?.any { it.findLeafWithSpecificType(IDENTIFIER)?.text == "Test" }
            ?: false
    val isLocatedInTest =
            filePathParts
                    .takeIf { it.contains(PACKAGE_PATH_ANCHOR) }
                    ?.run { subList(lastIndexOf(PACKAGE_PATH_ANCHOR), size) }
                    ?.let {
                        // e.g. src/test/ClassTest.kt
                        it.contains("test") || it.last().substringBeforeLast('.').endsWith("Test")
                    }
                    ?: false

    return hasTestAnnotation || isLocatedInTest
}

fun ASTNode.hasParameters(): Boolean {
    checkNode(this)
    val argList = this.argList()
    return argList != null && argList.hasChildOfType(ElementType.VALUE_PARAMETER)
}

fun ASTNode.parameterNames(): Collection<String?>? {
    checkNode(this)
    return (this.argList()?.psi as KtParameterList?)
        ?.parameters?.map { (it as KtParameter).name }
}

fun ASTNode.hasParametersKDoc(): Boolean {
    checkNode(this)
    val kDocTags = this.kDocTags()
    return kDocTags != null && kDocTags.hasKnownKDocTag(KDocKnownTag.PARAM)
}

private fun ASTNode.argList(): ASTNode? {
    checkNode(this)
    return this.getFirstChildWithType(ElementType.VALUE_PARAMETER_LIST)
}

private fun checkNode(node: ASTNode) =
    require(node.elementType == ElementType.FUN) {
        "This utility method operates on nodes of type ElementType.FUN only"
    }
