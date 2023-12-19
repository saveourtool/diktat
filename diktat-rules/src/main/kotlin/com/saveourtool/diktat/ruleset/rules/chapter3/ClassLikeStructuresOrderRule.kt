package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.BLANK_LINE_BETWEEN_PROPERTIES
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*
import com.saveourtool.diktat.ruleset.utils.isPartOfComment
import com.saveourtool.diktat.ruleset.utils.nextSibling
import com.saveourtool.diktat.ruleset.utils.parent
import com.saveourtool.diktat.ruleset.utils.prevSibling

import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.KtNodeTypes.CLASS_BODY
import org.jetbrains.kotlin.KtNodeTypes.CLASS_INITIALIZER
import org.jetbrains.kotlin.KtNodeTypes.ENUM_ENTRY
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.OBJECT_DECLARATION
import org.jetbrains.kotlin.KtNodeTypes.PROPERTY
import org.jetbrains.kotlin.KtNodeTypes.REFERENCE_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.SECONDARY_CONSTRUCTOR
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.KDOC
import org.jetbrains.kotlin.lexer.KtTokens.BLOCK_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.COMPANION_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.CONST_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.lexer.KtTokens.LATEINIT_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.LBRACE
import org.jetbrains.kotlin.lexer.KtTokens.PRIVATE_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.RBRACE
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * Rule that checks order of declarations inside classes, interfaces and objects.
 */
class ClassLikeStructuresOrderRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(BLANK_LINE_BETWEEN_PROPERTIES, WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == CLASS_BODY) {
            checkDeclarationsOrderInClass(node)
        } else if (node.elementType == PROPERTY) {
            checkNewLinesBeforeProperty(node)
        }
    }

    private fun checkDeclarationsOrderInClass(node: ASTNode) {
        val allProperties = AllProperties.fromClassBody(node)
        val initBlocks = node.getAllChildrenWithType(CLASS_INITIALIZER)
        val constructors = node.getAllChildrenWithType(SECONDARY_CONSTRUCTOR)
        val methods = node.getAllChildrenWithType(FUN)
        val (usedClasses, unusedClasses) = node.getUsedAndUnusedClasses()
        val (companionObject, objects) = node.getAllChildrenWithType(OBJECT_DECLARATION)
            .partition { it.hasModifier(COMPANION_KEYWORD) }
        val blocks = Blocks(
            (node.psi as KtClassBody).enumEntries.map { it.node },
            allProperties, objects, initBlocks, constructors,
            methods, usedClasses, companionObject, unusedClasses
        )
            .allBlockFlattened()
            .map { astNode ->
                listOf(astNode) +
                        astNode.siblings(false)
                            .takeWhile { it.elementType == WHITE_SPACE || it.isPartOfComment() }
                            .toList()
            }

        node.checkAndReorderBlocks(blocks)
    }

    @Suppress("UnsafeCallOnNullableType", "CyclomaticComplexMethod")
    private fun checkNewLinesBeforeProperty(node: ASTNode) {
        // checking only top-level and class-level properties
        if (node.treeParent.elementType != CLASS_BODY) {
            return
        }

        val previousProperty = node.prevSibling { it.elementType == PROPERTY } ?: return
        val nearComment = node.findChildByType(TokenSet.create(KDOC, EOL_COMMENT, BLOCK_COMMENT))
        val prevComment = nearComment?.prevSibling()
        val nextComment = nearComment?.nextSibling()
        val isCorrectEolComment = (prevComment == null || !prevComment.textContains('\n')) &&
                nextComment == null

        val hasCommentBefore = node
            .findChildByType(TokenSet.create(KDOC, EOL_COMMENT, BLOCK_COMMENT))
            ?.isFollowedByNewline()
            ?: false
        val hasAnnotationsBefore = (node.psi as KtProperty)
            .annotationEntries
            .any { it.node.isFollowedByNewline() }
        val hasCustomAccessors = (node.psi as KtProperty).accessors.isNotEmpty() ||
                (previousProperty.psi as KtProperty).accessors.isNotEmpty()

        val whiteSpaceBefore = previousProperty.nextSibling { it.elementType == WHITE_SPACE } ?: return
        val isBlankLineRequired = (!isCorrectEolComment && hasCommentBefore) || hasAnnotationsBefore || hasCustomAccessors
        val numRequiredNewLines = 1 + (if (isBlankLineRequired) 1 else 0)
        val actualNewLines = whiteSpaceBefore.text.count { it == '\n' }
        // for some cases (now - if this or previous property has custom accessors), blank line is allowed before it
        if (!hasCustomAccessors && actualNewLines != numRequiredNewLines ||
                hasCustomAccessors && actualNewLines > numRequiredNewLines) {
            BLANK_LINE_BETWEEN_PROPERTIES.warnAndFix(configRules, emitWarn, isFixMode, node.getIdentifierName()?.text ?: node.text, node.startOffset, node) {
                whiteSpaceBefore.leaveExactlyNumNewLines(numRequiredNewLines)
            }
        }
    }

    /**
     * Returns nested classes grouped by whether they are used inside [this] file.
     * [this] ASTNode should have elementType [CLASS_BODY]
     */
    private fun ASTNode.getUsedAndUnusedClasses() = getAllChildrenWithType(CLASS)
        .partition { classNode ->
            classNode.getIdentifierName()?.let { identifierNode ->
                parents()
                    .last()
                    .findAllDescendantsWithSpecificType(REFERENCE_EXPRESSION)
                    .any { ref ->
                        ref.parent { it == classNode } == null && ref.text.contains(identifierNode.text)
                    }
            } ?: false
        }

    /**
     * Checks whether all class elements in [this] node are correctly ordered and reorders them in fix mode.
     * [this] ASTNode should have elementType [CLASS_BODY]
     *
     * @param blocks list of class elements with leading whitespaces and comments
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun ASTNode.checkAndReorderBlocks(blocks: List<List<ASTNode>>) {
        val classChildren = this.children().filter { it.elementType in childrenTypes }.toList()

        check(blocks.size == classChildren.size) {
            StringBuilder().apply {
                append("`classChildren` has a size of ${classChildren.size} while `blocks` has a size of ${blocks.size}$NEWLINE")

                append("`blocks`:$NEWLINE")
                blocks.forEachIndexed { index, block ->
                    append("\t$index: ${block.firstOrNull()?.text}$NEWLINE")
                }

                append("`classChildren`:$NEWLINE")
                classChildren.forEachIndexed { index, child ->
                    append("\t$index: ${child.text}$NEWLINE")
                }
            }
        }

        if (classChildren != blocks.map { it.first() }) {
            blocks.filterIndexed { index, pair -> classChildren[index] != pair.first() }
                .forEach { listOfChildren ->
                    val astNode = listOfChildren.first()
                    WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES.warnAndFix(configRules, emitWarn, isFixMode,
                        "${astNode.elementType}: ${astNode.findChildByType(IDENTIFIER)?.text ?: astNode.text}", astNode.startOffset, astNode) {
                        removeRange(findChildByType(LBRACE)!!.treeNext, findChildByType(RBRACE)!!)
                        blocks.reversed()
                            .forEach { bodyChild ->
                                bodyChild.forEach { this.addChild(it, this.children().take(2).last()) }
                            }
                        // Add newline before the closing `}`. All other newlines will be properly formatted by `NewlinesRule`.
                        this.addChild(PsiWhiteSpaceImpl("\n"), this.lastChildNode)
                    }
                }
        }
    }

    /**
     * Data class containing different groups of properties in file
     *
     * @property loggers loggers (for example, properties called `log` or `logger`)
     * @property constProperties `const val`s
     * @property properties all other properties
     * @property lateInitProperties `lateinit var`s
     */
    private data class AllProperties(
        val loggers: List<ASTNode>,
        val constProperties: List<ASTNode>,
        val properties: List<ASTNode>,
        val lateInitProperties: List<ASTNode>
    ) {
        companion object {
            /**
             * Create [AllProperties] wrapper from node with type [CLASS_BODY]
             *
             * @param node an ASTNode with type [CLASS_BODY]
             * @return an instance of [AllProperties]
             */
            @Suppress("UnsafeCallOnNullableType")
            fun fromClassBody(node: ASTNode): AllProperties {
                val allProperties = node.getAllChildrenWithType(PROPERTY)
                val constProperties = allProperties.filterByModifier(CONST_KEYWORD)
                val lateInitProperties = allProperties.filterByModifier(LATEINIT_KEYWORD)
                val referencesFromSameScope = allProperties.mapNotNull { it.getIdentifierName()?.text }
                val loggers = allProperties.filterByModifier(PRIVATE_KEYWORD)
                    .filterNot { astNode ->
                        /*
                         * A `const` field named "logger" is unlikely to be a logger.
                         */
                        astNode in constProperties
                    }
                    .filterNot { astNode ->
                        /*
                         * A `lateinit` field named "logger" is unlikely to be a logger.
                         */
                        astNode in lateInitProperties
                    }
                    .filter { astNode ->
                        astNode.getIdentifierName()?.text?.matches(loggerPropertyRegex) ?: false
                    }
                    .let {
                        getLoggerDependencyNames(it)
                    }
                    .filter { (_, dependencyReferences) ->
                        dependencyReferences.all {
                            it !in referencesFromSameScope
                        }
                    }
                    .keys
                    .toList()

                val properties = allProperties.filter { it !in lateInitProperties && it !in loggers && it !in constProperties }
                return AllProperties(loggers, constProperties, properties, lateInitProperties)
            }

            @Suppress("TYPE_ALIAS")
            private fun getLoggerDependencyNames(loggers: List<ASTNode>): Map<ASTNode, List<String>> = loggers.map { astNode ->
                astNode to astNode.findAllDescendantsWithSpecificType(REFERENCE_EXPRESSION, false)
            }.associate { (astNode, possibleDependencies) ->
                astNode to possibleDependencies.map { it.text }
            }
        }
    }

    /**
     * @property enumEntries if this class is a enum class, list of its entries. Otherwise an empty list.
     * @property allProperties an instance of [AllProperties]
     * @property objects objects
     * @property initBlocks `init` blocks
     * @property constructors constructors
     * @property methods functions
     * @property usedClasses nested classes that are used in the enclosing class
     * @property companion `companion object`s
     * @property unusedClasses nested classes that are *not* used in the enclosing class
     */
    private data class Blocks(val enumEntries: List<ASTNode>,
                              val allProperties: AllProperties,
                              val objects: List<ASTNode>,
                              val initBlocks: List<ASTNode>,
                              val constructors: List<ASTNode>,
                              val methods: List<ASTNode>,
                              val usedClasses: List<ASTNode>,
                              val companion: List<ASTNode>,
                              val unusedClasses: List<ASTNode>
    ) {
        init {
            require(companion.size in 0..1) { "There is more than one companion object in class" }
        }

        /**
         * @return all groups of structures in the class
         */
        fun allBlocks() = with(allProperties) {
            listOf(enumEntries, loggers, constProperties, properties, lateInitProperties, objects,
                initBlocks, constructors, methods, usedClasses, companion, unusedClasses)
        }

        /**
         * @return all blocks as a flattened list of [ASTNode]s
         */
        fun allBlockFlattened() = allBlocks().flatten()
    }

    companion object {
        const val NAME_ID = "class-like-structures"
        private val childrenTypes = listOf(PROPERTY, CLASS, CLASS_INITIALIZER, SECONDARY_CONSTRUCTOR, FUN, OBJECT_DECLARATION, ENUM_ENTRY)
    }
}

private fun Iterable<ASTNode>.filterByModifier(modifier: IElementType) = filter {
    it.findLeafWithSpecificType(modifier) != null
}
