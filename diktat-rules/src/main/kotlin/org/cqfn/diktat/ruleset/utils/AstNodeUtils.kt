// todo fix inspections on KDocs
@file:Suppress("FILE_NAME_MATCH_CLASS", "KDOC_WITHOUT_RETURN_TAG", "KDOC_WITHOUT_PARAM_TAG")

package org.cqfn.diktat.ruleset.utils

import org.cqfn.diktat.ruleset.rules.PackageNaming

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.CONST_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FILE_ANNOTATION_LIST
import com.pinterest.ktlint.core.ast.ElementType.INTERNAL_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PROTECTED_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PUBLIC_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isLeaf
import com.pinterest.ktlint.core.ast.isRoot
import com.pinterest.ktlint.core.ast.lineNumber
import com.pinterest.ktlint.core.ast.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.TokenType
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A [Logger] that can be used throughout diktat
 */
val log: Logger = LoggerFactory.getLogger(ASTNode::class.java)

/**
 * Returns the highest parent node of the tree
 */
fun ASTNode.getRootNode() = if (isRoot()) this else parents().last()

/**
 * Checks whether [this] node's text has length in range [range]
 *
 * @param range an [IntRange] for text
 * @return boolean result
 */
fun ASTNode.isTextLengthInRange(range: IntRange): Boolean = this.textLength in range

/**
 * getting first child name with IDENTIFIER type
 *
 * @return node with type [IDENTIFIER] or null if it is not present
 */
fun ASTNode.getIdentifierName(): ASTNode? =
        this.getChildren(null).find { it.elementType == ElementType.IDENTIFIER }

/**
 * getting first child name with TYPE_PARAMETER_LIST type
 *
 * @return a node with type TYPE_PARAMETER_LIST or null if it is not present
 */
fun ASTNode.getTypeParameterList(): ASTNode? =
        this.getChildren(null).find { it.elementType == ElementType.TYPE_PARAMETER_LIST }

/**
 * getting all children that have IDENTIFIER type
 *
 * @return a list of nodes
 */
fun ASTNode.getAllIdentifierChildren(): List<ASTNode> =
        this.getChildren(null).filter { it.elementType == ElementType.IDENTIFIER }

/**
 * check is node doesn't contain error elements
 */
fun ASTNode.isCorrect() = this.findAllNodesWithSpecificType(TokenType.ERROR_ELEMENT).isEmpty()

/**
 * obviously returns list with children that match particular element type
 *
 * @param elementType the [IElementType] to search
 * @return list of nodes
 */
fun ASTNode.getAllChildrenWithType(elementType: IElementType): List<ASTNode> =
        this.getChildren(null).filter { it.elementType == elementType }

/**
 * Replaces the [beforeNode] of type [WHITE_SPACE] with the node with specified [text]
 *
 * @param beforeNode a node to replace
 * @param text a text (white space characters only) for the new node
 */
fun ASTNode.replaceWhiteSpaceText(beforeNode: ASTNode, text: String) {
    require(beforeNode.elementType == WHITE_SPACE)
    this.addChild(PsiWhiteSpaceImpl(text), beforeNode)
    this.removeChild(beforeNode)
}

/**
 * obviously returns first child that match particular element type
 *
 * @param elementType an [IElementType
 * @return a node or null if it was not found
 */
fun ASTNode.getFirstChildWithType(elementType: IElementType): ASTNode? =
        this.getChildren(null).find { it.elementType == elementType }

/**
 * Checks if the symbols in this node are at the end of line
 */
fun ASTNode.isEol() = parent({ it.treeNext != null }, false)?.isFollowedByNewline() ?: true

/**
 * Checks if there is a newline after symbol corresponding to this element. We can't always check only this node itself, because
 * some nodes are placed not on the same level as white spaces, e.g. operators like [ElementType.ANDAND] are children of [ElementType.OPERATION_REFERENCE].
 * Same is true also for semicolons in some cases.
 * Therefore, to check if they are followed by newline we need to check their parents.
 */
fun ASTNode.isFollowedByNewline() =
        parent({ it.treeNext != null }, strict = false)?.let {
            it.treeNext.elementType == WHITE_SPACE && it.treeNext.text.contains("\n")
        } ?: false

/**
 * Checks if there is a newline before this element. See [isFollowedByNewline] for motivation on parents check.
 */
fun ASTNode.isBeginByNewline() =
        parent({ it.treePrev != null }, strict = false)?.let {
            it.treePrev.elementType == WHITE_SPACE && it.treePrev.text.contains("\n")
        } ?: false

/**
 * checks if the node has corresponding child with elementTyp
 */
fun ASTNode.hasChildOfType(elementType: IElementType) =
        this.getFirstChildWithType(elementType) != null

/**
 * Checks whether the node has at least one child of at least one of [elementType]
 *
 * @param elementType vararg of [IElementType]
 */
fun ASTNode.hasAnyChildOfTypes(vararg elementType: IElementType) =
        elementType.any { this.hasChildOfType(it) }

/**
 * checks if node has parent of type
 */
fun ASTNode.hasParent(type: IElementType) = parent(type) != null

/**
 * check if node's text is empty (contains only left and right braces)
 * check text because some nodes have empty BLOCK element inside (lambda)
 */
fun ASTNode?.isBlockEmpty() = this?.let {
    this.text.replace("\\s+".toRegex(), "") == EMPTY_BLOCK_TEXT
} ?: true

/**
 * Method that is trying to find and return child of this node, which
 * 1) stands before the node with type @beforeThisNodeType
 * 2) has type @childNodeType
 * 2) is closest to node with @beforeThisNodeType (i.e. is first in reversed list of children, starting at @beforeThisNodeType)
 */
fun ASTNode.findChildBefore(beforeThisNodeType: IElementType, childNodeType: IElementType): ASTNode? {
    val anchorNode = getChildren(null)
        .find { it.elementType == beforeThisNodeType }
    getChildren(null)
        .toList()
        .let {
            if (anchorNode != null) {
                it.subList(0, it.indexOf(anchorNode))
            } else {
                it
            }
        }
        .reversed()
        .find { it.elementType == childNodeType }
        ?.let { return it }

    return null
}

/**
 * method that is trying to find and return FIRST node that matches these conditions:
 * 1) it is one of children of "this"
 * 2) it stands in the list of children AFTER the node with type @afterThisNodeType
 * 3) it has type @childNodeType
 */
fun ASTNode.findChildAfter(afterThisNodeType: IElementType, childNodeType: IElementType): ASTNode? {
    var foundAnchorNode = false
    getChildren(null).forEach {
        if (foundAnchorNode && it.elementType == childNodeType) {
            // if we have already found previous node and type matches - then can return child
            return it
        }
        if (it.elementType == afterThisNodeType) {
            // found the node that is used as anchor and we are trying to find
            // a node with IElementType that stands after this anchor node
            foundAnchorNode = true
        }
    }

    return null
}

/**
 * method that traverses previous nodes until it finds needed node or it finds stop node
 *
 * @return ASTNode?
 */
fun ASTNode.prevNodeUntilNode(stopNodeType: IElementType, checkNodeType: IElementType): ASTNode? =
        siblings(false).takeWhile { it.elementType != stopNodeType }.find { it.elementType == checkNodeType }

/**
 * Returns all siblings of [this] node
 *
 * @param withSelf whether [this] node is included in the result
 * @return list of siblings
 */
fun ASTNode.allSiblings(withSelf: Boolean = false): List<ASTNode> =
        siblings(false).toList() + (if (withSelf) listOf(this) else listOf()) + siblings(true)

/**
 * Checks whether [this] node belongs to a companion object
 *
 * @return boolean result
 */
fun ASTNode.isNodeFromCompanionObject(): Boolean {
    val parent = this.treeParent
    if (parent != null) {
        val grandParent = parent.treeParent
        if (grandParent != null && grandParent.elementType == ElementType.OBJECT_DECLARATION) {
            if (grandParent.findLeafWithSpecificType(ElementType.COMPANION_KEYWORD) != null) {
                return true
            }
        }
    }
    return false
}

/**
 * Checks whether this node is a constant
 *
 * @return boolean result
 */
fun ASTNode.isConstant() = (this.isNodeFromFileLevel() || this.isNodeFromObject()) && this.isValProperty() && this.isConst()

/**
 * Checks whether this node is an object
 *
 * @return boolean result
 */
fun ASTNode.isNodeFromObject(): Boolean {
    val parent = this.treeParent
    if (parent != null && parent.elementType == ElementType.CLASS_BODY) {
        val grandParent = parent.treeParent
        if (grandParent != null && grandParent.elementType == ElementType.OBJECT_DECLARATION) {
            return true
        }
    }
    return false
}

/**
 * Checks whether this node is declared on a file level
 *
 * @return boolean result
 */
fun ASTNode.isNodeFromFileLevel(): Boolean = this.treeParent.elementType == FILE

/**
 * Checks whether [this] node of type PROPERTY is `val`
 */
fun ASTNode.isValProperty() =
        this.getChildren(null)
            .any { it.elementType == ElementType.VAL_KEYWORD }

/**
 * Checks whether this node of type PROPERTY has `const` modifier
 */
fun ASTNode.isConst() = this.findLeafWithSpecificType(CONST_KEYWORD) != null

/**
 * Checks whether [this] node of type PROPERTY is `var`
 */
fun ASTNode.isVarProperty() =
        this.getChildren(null)
            .any { it.elementType == ElementType.VAR_KEYWORD }

/**
 * Replaces text of [this] node with lowercase text
 */
fun ASTNode.toLower() {
    (this as LeafPsiElement).replaceWithText(this.text.toLowerCase())
}

/**
 * This util method does tree traversal and stores to the result all tree leaf node of particular type (elementType).
 * Recursively will visit each and every node and will get leaves of specific type. Those nodes will be added to the result.
 */
fun ASTNode.getAllLeafsWithSpecificType(elementType: IElementType, result: MutableList<ASTNode>) {
    // if statements here have the only right order - don't change it
    if (this.isLeaf()) {
        if (this.elementType == elementType) {
            result.add(this)
        }
    } else {
        this.getChildren(null).forEach {
            it.getAllLeafsWithSpecificType(elementType, result)
        }
    }
}

/**
 * This util method does tree traversal and returns first node that matches specific type
 * This node isn't necessarily a leaf though method name implies it
 */
fun ASTNode.findLeafWithSpecificType(elementType: IElementType): ASTNode? {
    if (this.elementType == elementType) {
        return this
    }
    if (this.isLeaf()) {
        return null
    }

    this.getChildren(null).forEach {
        val result = it.findLeafWithSpecificType(elementType)
        if (result != null) {
            return result
        }
    }
    return null
}

/**
 * This method counts number of \n in node's text
 */
fun ASTNode.numNewLines() = text.count { it == '\n' }

/**
 * This method performs tree traversal and returns all nodes with specific element type
 */
fun ASTNode.findAllNodesWithSpecificType(elementType: IElementType, withSelf: Boolean = true) =
        findAllNodesWithCondition({ it.elementType == elementType }, withSelf)

/**
 * This method performs tree traversal and returns all nodes which satisfy the condition
 */
@Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")
fun ASTNode.findAllNodesWithCondition(condition: (ASTNode) -> Boolean, withSelf: Boolean = true): List<ASTNode> {
    val result = if (condition(this) && withSelf) mutableListOf(this) else mutableListOf()
    return result + this.getChildren(null).flatMap {
        it.findAllNodesWithCondition(condition)
    }
}

/**
 * Check a node of type CLASS if it is a enum class
 */
fun ASTNode.isClassEnum(): Boolean = (psi as? KtClass)?.isEnum() ?: false

/**
 * This method finds first parent node from the sequence of parents that has specified elementType
 */
fun ASTNode.findParentNodeWithSpecificType(elementType: IElementType) =
        this.parents().find { it.elementType == elementType }

/**
 * Finds all children of optional type which match the predicate
 */
fun ASTNode.findChildrenMatching(elementType: IElementType? = null, predicate: (ASTNode) -> Boolean): List<ASTNode> =
        getChildren(elementType?.let { TokenSet.create(it) })
            .filter(predicate)

/**
 * Check if this node has any children of optional type matching the predicate
 */
fun ASTNode.hasChildMatching(elementType: IElementType? = null, predicate: (ASTNode) -> Boolean): Boolean =
        findChildrenMatching(elementType, predicate).isNotEmpty()

/**
 * Converts this AST node and all its children to pretty string representation
 */
@Suppress("AVOID_NESTED_FUNCTIONS")
fun ASTNode.prettyPrint(level: Int = 0, maxLevel: Int = -1): String {
    // AST operates with \n only, so we need to build the whole string representation and then change line separator
    fun ASTNode.doPrettyPrint(level: Int, maxLevel: Int): String {
        val result = StringBuilder("${this.elementType}: \"${this.text}\"").append('\n')
        if (maxLevel != 0) {
            this.getChildren(null).forEach { child ->
                result.append(
                    "${"-".repeat(level + 1)} " +
                            child.doPrettyPrint(level + 1, maxLevel - 1)
                )
            }
        }
        return result.toString()
    }
    return doPrettyPrint(level, maxLevel).replace("\n", System.lineSeparator())
}

/**
 * Checks if this modifier list corresponds to accessible outside entity.
 * The receiver should be an ASTNode with ElementType.MODIFIER_LIST, can be null if entity has no modifier list
 */
fun ASTNode?.isAccessibleOutside(): Boolean =
        if (this != null) {
            assert(this.elementType == MODIFIER_LIST)
            this.hasAnyChildOfTypes(PUBLIC_KEYWORD, PROTECTED_KEYWORD, INTERNAL_KEYWORD) ||
                    !this.hasAnyChildOfTypes(PUBLIC_KEYWORD, INTERNAL_KEYWORD, PROTECTED_KEYWORD, PRIVATE_KEYWORD)
        } else {
            true
        }

/**
 * Checks whether [this] node has a parent annotated with `@Suppress` with [warningName]
 *
 * @param warningName a name of the warning which is checked
 * @return boolean result
 */
fun ASTNode.hasSuppress(warningName: String) = parent({ node ->
    val annotationNode = if (node.elementType != FILE) {
        node.findChildByType(MODIFIER_LIST)
    } else {
        node.findChildByType(FILE_ANNOTATION_LIST)
    }
    annotationNode?.findAllNodesWithSpecificType(ANNOTATION_ENTRY)
        ?.map { it.psi as KtAnnotationEntry }
        ?.any {
            it.shortName.toString() == Suppress::class.simpleName &&
                    it.valueArgumentList?.arguments
                        ?.any { annotationName -> annotationName.text.trim('"', ' ') == warningName }
                        ?: false
        } ?: false
}, strict = false) != null

/**
 * creation of operation reference in a node
 */
fun ASTNode.createOperationReference(elementType: IElementType, text: String) {
    val operationReference = CompositeElement(OPERATION_REFERENCE)
    this.addChild(operationReference, null)
    operationReference.addChild(LeafPsiElement(elementType, text), null)
}

/**
 * removing all newlines in WHITE_SPACE node and replacing it to a one newline saving the initial indenting format
 */
fun ASTNode.leaveOnlyOneNewLine() = leaveExactlyNumNewLines(1)

/**
 * removing all newlines in WHITE_SPACE node and replacing it to [num] newlines saving the initial indenting format
 */
fun ASTNode.leaveExactlyNumNewLines(num: Int) {
    require(this.elementType == WHITE_SPACE)
    (this as LeafPsiElement).replaceWithText("${"\n".repeat(num)}${this.text.replace("\n", "")}")
}

/**
 * If [whiteSpaceNode] is not null and has type [WHITE_SPACE], prepend a line break to it's text.
 * Otherwise, insert a new node with a line break before [beforeNode]
 *
 * @param whiteSpaceNode a node that can possibly be modified
 * @param beforeNode a node before which a new WHITE_SPACE node will be inserted
 */
fun ASTNode.appendNewlineMergingWhiteSpace(whiteSpaceNode: ASTNode?, beforeNode: ASTNode?) {
    if (whiteSpaceNode != null && whiteSpaceNode.elementType == WHITE_SPACE) {
        (whiteSpaceNode as LeafPsiElement).replaceWithText("\n${whiteSpaceNode.text}")
    } else {
        addChild(PsiWhiteSpaceImpl("\n"), beforeNode)
    }
}

/**
 * Transforms last line of this WHITE_SPACE to exactly [indent] spaces
 */
fun ASTNode.indentBy(indent: Int) {
    require(this.elementType == WHITE_SPACE)
    (this as LeafPsiElement).rawReplaceWithText(text.substringBeforeLast('\n') + "\n" + " ".repeat(indent))
}

/**
 * @param beforeThisNode node before which childToMove will be placed. If null, childToMove will be appended after last child of this node.
 * @param withNextNode whether next node after childToMove should be moved too. In most cases it corresponds to moving
 *     the node with newline.
 */
fun ASTNode.moveChildBefore(
    childToMove: ASTNode,
    beforeThisNode: ASTNode?,
    withNextNode: Boolean = false
): ReplacementResult {
    require(childToMove in children()) { "can only move child of this node" }
    require(beforeThisNode == null || beforeThisNode in children()) { "can only place node before another child of this node" }
    val movedChild = childToMove.clone() as ASTNode
    val nextMovedChild = childToMove.treeNext?.takeIf { withNextNode }?.let { it.clone() as ASTNode }
    val nextOldChild = childToMove.treeNext.takeIf { withNextNode && it != null }
    addChild(movedChild, beforeThisNode)
    if (nextMovedChild != null && nextOldChild != null) {
        addChild(nextMovedChild, beforeThisNode)
        removeChild(nextOldChild)
    }
    removeChild(childToMove)
    return ReplacementResult(listOfNotNull(childToMove, nextOldChild), listOfNotNull(movedChild, nextMovedChild))
}

/**
 * Finds a first `{` node inside [this] node
 *
 * @return a LBRACE node or `null` if it can't be found
 */
@Suppress("UnsafeCallOnNullableType", "FUNCTION_NAME_INCORRECT_CASE")
fun ASTNode.findLBrace(): ASTNode? = when (this.elementType) {
    ElementType.THEN, ElementType.ELSE, ElementType.FUN, ElementType.TRY, ElementType.CATCH, ElementType.FINALLY ->
        this.findChildByType(ElementType.BLOCK)?.findChildByType(LBRACE)
    ElementType.WHEN -> this.findChildByType(LBRACE)!!
    ElementType.FOR, ElementType.WHILE, ElementType.DO_WHILE ->
        this.findChildByType(ElementType.BODY)
            ?.findChildByType(ElementType.BLOCK)
            ?.findChildByType(LBRACE)
    ElementType.CLASS, ElementType.OBJECT_DECLARATION -> this.findChildByType(ElementType.CLASS_BODY)
        ?.findChildByType(LBRACE)
    ElementType.FUNCTION_LITERAL -> this.findChildByType(LBRACE)
    else -> null
}

/**
 * Checks whether this node of type IF is a single line expression with single else, like `if (true) x else y`
 *
 * @return boolean result
 */
fun ASTNode.isSingleLineIfElse(): Boolean {
    val elseNode = (psi as KtIfExpression).`else`?.node
    val hasSingleElse = elseNode != null && elseNode.elementType != ElementType.IF
    return treeParent.elementType != ElementType.ELSE && hasSingleElse && text.lines().size == 1
}

/**
 * Checks whether [child] is after [afterChild] among the children of [this] node
 *
 * @return boolean result
 */
fun ASTNode.isChildAfterAnother(child: ASTNode, afterChild: ASTNode): Boolean =
        getChildren(null).indexOf(child) > getChildren(null).indexOf(afterChild)

/**
 * Checks whether [child] is after all nodes from [group] among the children of [this] node
 *
 * @return boolean result
 */
fun ASTNode.isChildAfterGroup(child: ASTNode, group: List<ASTNode>): Boolean =
        getChildren(null).indexOf(child) > (group.map { getChildren(null).indexOf(it) }.max() ?: 0)

/**
 * Checks whether [child] is before [beforeChild] among the children of [this] node
 *
 * @return boolean result
 */
fun ASTNode.isChildBeforeAnother(child: ASTNode, beforeChild: ASTNode): Boolean =
        areChildrenBeforeGroup(listOf(child), listOf(beforeChild))

/**
 * Checks whether [child] is before all nodes is [group] among the children of [this] node
 *
 * @return boolean result
 */
fun ASTNode.isChildBeforeGroup(child: ASTNode, group: List<ASTNode>): Boolean =
        areChildrenBeforeGroup(listOf(child), group)

/**
 * Checks whether all nodes in [children] is before [beforeChild] among the children of [this] node
 *
 * @return boolean result
 */
@Suppress("FUNCTION_BOOLEAN_PREFIX")
fun ASTNode.areChildrenBeforeChild(children: List<ASTNode>, beforeChild: ASTNode): Boolean =
        areChildrenBeforeGroup(children, listOf(beforeChild))

/**
 * Checks whether all nodes in [children] is before all nodes in [group] among the children of [this] node
 *
 * @return boolean result
 */
@Suppress("UnsafeCallOnNullableType", "FUNCTION_BOOLEAN_PREFIX")
fun ASTNode.areChildrenBeforeGroup(children: List<ASTNode>, group: List<ASTNode>): Boolean {
    require(children.isNotEmpty() && group.isNotEmpty()) { "no sense to operate on empty lists" }
    return children.map { getChildren(null).indexOf(it) }.max()!! < group.map { getChildren(null).indexOf(it) }.min()!!
}

/**
 * A function that rearranges nodes in a [this] list.
 *
 * @param getSiblingBlocks a function which returns nodes that should be before and after the current node
 * @param incorrectPositionHandler function that moves the current node with respect to node before which in should be placed
 */
@Suppress("TYPE_ALIAS")
fun List<ASTNode>.handleIncorrectOrder(
    getSiblingBlocks: ASTNode.() -> Pair<ASTNode?, ASTNode>,
    incorrectPositionHandler: (nodeToMove: ASTNode, beforeThisNode: ASTNode) -> Unit
) {
    forEach { astNode ->
        val (afterThisNode, beforeThisNode) = astNode.getSiblingBlocks()
        val isPositionIncorrect =
                (afterThisNode != null && !astNode.treeParent.isChildAfterAnother(astNode, afterThisNode)) ||
                        !astNode.treeParent.isChildBeforeAnother(astNode, beforeThisNode)

        if (isPositionIncorrect) {
            incorrectPositionHandler(astNode, beforeThisNode)
        }
    }
}

/**
 * This method returns text of this [ASTNode] plus text from it's siblings after last and until next newline, if present in siblings.
 * I.e., if this node occupies no more than a single line, this whole line or it's part will be returned.
 */
fun ASTNode.extractLineOfText(): String {
    var text = mutableListOf<String>()
    val nextNode = parent({ it.treeNext != null }, false) ?: this
    siblings(false)
        .map { it.text.split("\n") }
        .takeWhileInclusive { it.size <= 1 }
        .forEach { text.add(it.last()) }
    text = text.asReversed()
    text.add(this.text)
    nextNode.siblings(true)
        .map { it.text.split("\n") }
        .takeWhileInclusive { it.size <= 1 }
        .forEach { text.add(it.first()) }
    return text.joinToString(separator = "").trim()
}

/**
 * Checks node has `@Test` annotation
 */
fun ASTNode.hasTestAnnotation() = findChildByType(MODIFIER_LIST)
    ?.getAllChildrenWithType(ElementType.ANNOTATION_ENTRY)
    ?.flatMap { it.findAllNodesWithSpecificType(ElementType.CONSTRUCTOR_CALLEE) }
    ?.any { it.findLeafWithSpecificType(ElementType.IDENTIFIER)?.text == "Test" }
    ?: false

/**
 * Checks node is located in file src/test/**/*Test.kt
 *
 * @param testAnchors names of test directories, e.g. "test", "jvmTest"
 */
fun isLocatedInTest(filePathParts: List<String>, testAnchors: List<String>) = filePathParts
    .takeIf { it.contains(PackageNaming.PACKAGE_PATH_ANCHOR) }
    ?.run { subList(lastIndexOf(PackageNaming.PACKAGE_PATH_ANCHOR), size) }
    ?.run {
        // e.g. src/test/ClassTest.kt, other files like src/test/Utils.kt are still checked
        testAnchors.any { contains(it) } && last().substringBeforeLast('.').endsWith("Test")
    }
    ?: false

/**
 * Returns the first line of this node's text if it is single, or the first line followed by [suffix] if there are more than one.
 *
 * @param suffix a suffix to append if there are more than one lines of text
 */
fun ASTNode.firstLineOfText(suffix: String = "") = text.lines().run { singleOrNull() ?: (first() + suffix) }

/**
 * Return the number of the last line in the file
 *
 * @param isFixMode whether autofix mode is on
 */
fun ASTNode.lastLineNumber(isFixMode: Boolean) = getLineNumber(isFixMode)?.plus(text.count { it == '\n' })

/**
 * copy-pasted method from ktlint to determine line and column number by offset
 */
@Suppress("WRONG_NEWLINES")  // to keep this function with explicit return type
fun ASTNode.calculateLineColByOffset(): (offset: Int) -> Pair<Int, Int> {
    return buildPositionInTextLocator(text)
}

/**
 * Retrieves file name from user data of this node
 *
 * @return name of the file [this] node belongs to
 */
fun ASTNode.getFileName(): String = getUserData(KtLint.FILE_PATH_USER_DATA_KEY).let {
    require(it != null) { "File path is not present in user data" }
    it
}

/**
 * A class that represents result of nodes swapping. [oldNodes] should always have same size as [newNodes]
 *
 * @property oldNodes nodes that were to be moved
 * @property newNodes nodes that have been moved
 */
data class ReplacementResult(val oldNodes: List<ASTNode>, val newNodes: List<ASTNode>) {
    init {
        require(oldNodes.size == newNodes.size)
    }
}

/**
 * checks that this one node is placed after the other node in code (by comparing lines of code where nodes start)
 */
fun ASTNode.isGoingAfter(otherNode: ASTNode): Boolean {
    val thisLineNumber = this.calculateLineNumber()
    val otherLineNumber = otherNode.calculateLineNumber()

    require(thisLineNumber != null) { "Node ${this.text} should have a line number" }
    require(otherLineNumber != null) { "Node ${otherNode.text} should have a line number" }

    return (thisLineNumber > otherLineNumber)
}

/**
 * Get line number, where this node's content starts
 *
 * @param isFixMode if fix mode is off, then the text can't be altered and we can use cached values for line numbers
 * @return line number or null if it cannot be calculated
 */
fun ASTNode.getLineNumber(isFixMode: Boolean): Int? =
        if (!isFixMode) {
            lineNumber()
        } else {
            calculateLineNumber()
        }

/**
 * This function calculates line number instead of using cached values.
 * It should be used when AST could be previously mutated by auto fixers.
 */
private fun ASTNode.calculateLineNumber(): Int? {
    var count = 0
    // todo use runningFold or something similar when we migrate to apiVersion 1.4
    return parents()
        .last()
        .text
        .lineSequence()
        // calculate offset for every line end, `+1` for `\n` which is trimmed in `lineSequence`
        .indexOfFirst {
            count += it.length + 1
            count > startOffset
        }
        .let { if (it == -1) null else it + 1 }
}
