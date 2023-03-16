package org.cqfn.diktat.ruleset.generation

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * [SymbolProcessor] to generate a class with contacts for names from provided enum
 */
class EnumNamesSymbolProcessor(
    private val sourceEnumName: String,
    private val targetPackageName: String,
    private val targetClassName: String,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val enumDeclaration = requireNotNull(resolver.getClassDeclarationByName(sourceEnumName)) {
            "Not found class provided by property <${EnumNamesSymbolProcessorProvider.OPTION_NAME_SOURCE_ENUM_NAME}>"
        }
        require(enumDeclaration.classKind == ClassKind.ENUM_CLASS) {
            "Provided class $sourceEnumName is not enum"
        }

        resolver.getNewFiles()
            .find { it.packageName.asString() == targetPackageName && it.fileName == "$targetClassName.kt" }
            ?.run {
                return emptyList()
            }
        codeGenerator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = targetPackageName,
            fileName = targetClassName,
        ).bufferedWriter()
            .use { writer ->
                writer.write(autoGenerationComment)
                writer.newLine()
                writer.write("package $targetPackageName\n")
                writer.newLine()
                writer.write("import kotlin.String\n")
                writer.newLine()
                writer.write("object $targetClassName {\n")
                enumDeclaration.declarations
                    .filterIsInstance<KSClassDeclaration>()
                    .filter { it.classKind == ClassKind.ENUM_ENTRY }
                    .map { it.simpleName.asString() }
                    .forEach { enumEntryName ->
                        writer.write("    const val $enumEntryName: String = \"$enumEntryName\"\n")
                    }
                writer.write("}\n")
            }
        return emptyList()
    }

    companion object {
        /**
         * The comment that will be added to the generated sources file.
         */
        private val autoGenerationComment =
            """
                |/**
                | * This document was auto generated, please don't modify it.
                | * This document contains all enum properties from Warnings.kt as Strings.
                | */
            """.trimMargin()
    }
}
