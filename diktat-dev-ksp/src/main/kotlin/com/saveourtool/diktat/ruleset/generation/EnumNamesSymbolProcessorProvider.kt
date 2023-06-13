package com.saveourtool.diktat.ruleset.generation

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * [SymbolProcessorProvider] for [EnumNamesSymbolProcessor]
 */
class EnumNamesSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment,
    ): SymbolProcessor = EnumNamesSymbolProcessor(
        codeGenerator = environment.codeGenerator,
    )
}
