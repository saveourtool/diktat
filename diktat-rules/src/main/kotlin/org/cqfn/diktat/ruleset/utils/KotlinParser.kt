package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.psi.TokenType.ERROR_ELEMENT
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.ImportPath

class KotlinParser {

    /**
     * Set property
     */
    init {
        setIdeaIoUseFallback()
    }

    fun createNode(text: String): ASTNode {
        val project = KotlinCoreEnvironment.createForProduction(
                Disposable {},
                CompilerConfiguration(),
                EnvironmentConfigFiles.JVM_CONFIG_FILES
        ).project
        val ktPsiFactory = KtPsiFactory(project, true)
        val node = ktPsiFactory.createBlockCodeFragment(text, null).node
        if (node.findAllNodesWithSpecificType(ERROR_ELEMENT).isNotEmpty()){
            node.findAllNodesWithSpecificType(ERROR_ELEMENT).forEach {
                val q = ktPsiFactory.createImportDirective(ImportPath.fromString(it.text)).node
                println(q.prettyPrint())
            }
            throw KotlinParseException("Your text is not valid")
        }
        return node.findChildByType(BLOCK)!!.firstChildNode
    }
}
