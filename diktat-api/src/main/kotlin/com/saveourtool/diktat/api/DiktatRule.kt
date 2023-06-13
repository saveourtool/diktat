package com.saveourtool.diktat.api

import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * This is a base interface for diktat's rules
 */
interface DiktatRule : Function3<ASTNode, Boolean, DiktatErrorEmitter, Unit> {
    /**
     * A unique ID of this rule.
     */
    val id: String

    /**
     * This method is going to be executed for each node in AST (in DFS fashion).
     *
     * @param node AST node
     * @param autoCorrect indicates whether rule should attempt autocorrection
     * @param emitter a way for rule to notify about a violation (lint error)
     */
    override fun invoke(
        node: ASTNode,
        autoCorrect: Boolean,
        emitter: DiktatErrorEmitter,
    )
}
