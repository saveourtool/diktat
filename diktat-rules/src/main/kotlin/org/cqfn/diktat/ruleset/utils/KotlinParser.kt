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

class KotlinParser {

    /**
     * Set idea.io.use.nio2 in system property to true
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
        val node = ktPsiFactory.createBlockCodeFragment(text, null).node.findChildByType(BLOCK)!!
        if (node.findAllNodesWithSpecificType(ERROR_ELEMENT).isNotEmpty()) {
            val fileNode = ktPsiFactory.createFile(text).node
            if (fileNode.findAllNodesWithSpecificType(ERROR_ELEMENT).isNotEmpty())
                throw KotlinParseException("Your text is not valid")
            else return fileNode
        }
        return node.firstChildNode
    }
}
