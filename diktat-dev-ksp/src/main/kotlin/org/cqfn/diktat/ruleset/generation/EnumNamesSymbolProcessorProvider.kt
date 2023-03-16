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
        sourceEnumName = environment.options.getValue(OPTION_NAME_SOURCE_ENUM_NAME),
        targetPackageName = environment.options.getValue(OPTION_NAME_TARGET_PACKAGE_NAME),
        targetClassName = environment.options.getValue(OPTION_NAME_TARGET_CLASS_NAME),
        codeGenerator = environment.codeGenerator,
    )

    companion object {
        /**
         * Option name to specify `enumName`
         */
        const val OPTION_NAME_SOURCE_ENUM_NAME = "sourceEnumName"

        /**
         * Option name to specify `className` for target class
         */
        const val OPTION_NAME_TARGET_CLASS_NAME = "targetClassName"

        /**
         * Option name to specify `packageName` for target class
         */
        const val OPTION_NAME_TARGET_PACKAGE_NAME = "targetPackageName"
    }
}
