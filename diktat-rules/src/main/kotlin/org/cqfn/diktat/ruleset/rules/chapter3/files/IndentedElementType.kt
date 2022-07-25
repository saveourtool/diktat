@file:Suppress(
    "HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE",
    "TOP_LEVEL_ORDER",
)

package org.cqfn.diktat.ruleset.rules.chapter3.files

import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType

/**
 * An [IElementType] along with the indentation change it induces.
 */
internal typealias IndentedElementType = Pair<IElementType, IndentationAmount>

/**
 * @return the element type.
 */
@Suppress("CUSTOM_GETTERS_SETTERS")
internal val IndentedElementType.type: IElementType
    get() =
        first

/**
 * @return the indentation change.
 */
@Suppress("CUSTOM_GETTERS_SETTERS")
internal val IndentedElementType.indentationChange: IndentationAmount
    get() =
        second
