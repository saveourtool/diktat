package org.cqfn.diktat.ruleset.generation

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
        enumName = environment.options.getValue(OPTION_NAME),
        codeGenerator = environment.codeGenerator,
    )

    companion object {
        /**
         * Option name to specify `enumName`
         */
        const val OPTION_NAME = "enumName"
    }
}
