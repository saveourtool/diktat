package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_LIST
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.util.UserDataHolderBase
import org.jetbrains.kotlin.com.intellij.pom.PomModel
import org.jetbrains.kotlin.com.intellij.pom.PomModelAspect
import org.jetbrains.kotlin.com.intellij.pom.PomTransaction
import org.jetbrains.kotlin.com.intellij.pom.impl.PomTransactionBase
import org.jetbrains.kotlin.com.intellij.pom.tree.TreeAspect
import org.jetbrains.kotlin.com.intellij.psi.TokenType.ERROR_ELEMENT
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.ImportPath
import sun.reflect.ReflectionFactory

class KotlinParser {

    /**
     * Set idea.io.use.nio2 in system property to true
     * fixme Maybe should check OS to don't change system property
     */
    init {
        setIdeaIoUseFallback()
    }

    fun createNode(text: String, isPackage: Boolean = false): ASTNode {
        return makeNode(text, isPackage) ?: throw KotlinParseException("Your text is not valid")
    }

    /**
     * This method create a node based on text.
     * @param isPackage - flag to check if node will contains package.
     * If this flag is true, node's element type will be FILE.
     * Else, try to create node based on text.
     * If this node will contain ERROR_ELEMENT type children this mean that cannot create node based on this text
     */
    private fun makeNode(text: String, isPackage: Boolean = false): ASTNode? {
        val compilerConfiguration = CompilerConfiguration()
        compilerConfiguration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE) // mute the output logging to process it themselves
        val pomModel: PomModel = object : UserDataHolderBase(), PomModel {
            override fun runTransaction(transaction: PomTransaction) {
                (transaction as PomTransactionBase).run()
            }

            @Suppress("UNCHECKED_CAST")
            override fun <T : PomModelAspect> getModelAspect(aspect: Class<T>): T? {
                if (aspect == TreeAspect::class.java) {
                    val constructor = ReflectionFactory.getReflectionFactory().newConstructorForSerialization(
                            aspect,
                            Any::class.java.getDeclaredConstructor(*arrayOfNulls<Class<*>>(0))
                    )
                    return constructor.newInstance() as T
                }
                return null
            }
        } // I don't really understand what's going on here, but thanks to this, you can use this node in the future
        val project = KotlinCoreEnvironment.createForProduction(
                Disposable {},
                compilerConfiguration,
                EnvironmentConfigFiles.JVM_CONFIG_FILES
        ).project // create project
        project as MockProject
        project.registerService(PomModel::class.java, pomModel)
        if (text.isEmpty())
            return null
        val ktPsiFactory = KtPsiFactory(project, true)
        if (text.trim().isEmpty())
            return ktPsiFactory.createWhiteSpace(text).node
        var node = if (isPackage || isContainKDoc(text)) {
            ktPsiFactory.createFile(text).node
        } else {
            if (text.contains(KtTokens.IMPORT_KEYWORD.value)) {
                val (imports, blockCode) = text.lines().partition { it.contains(KtTokens.IMPORT_KEYWORD.value) }
                if (blockCode.isNotEmpty())
                    return null
                if (imports.size == 1) {
                    val importText = ImportPath.fromString(text.substringAfter("$IMPORT_KEYWORD "))
                    ktPsiFactory.createImportDirective(importText).node
                } else {
                    ktPsiFactory.createBlockCodeFragment(text, null).node.findChildByType(BLOCK)!!
                            .findChildByType(ERROR_ELEMENT)!!.findChildByType(IMPORT_LIST)!!
                }
            } else {
                ktPsiFactory.createBlockCodeFragment(text, null).node.findChildByType(BLOCK)!!
            }
        }
        if (node.getChildren(null).size == 1)
            node = node.firstChildNode
        if (!node.isCorrect()) {
            node = ktPsiFactory.createFile(text).node
            if (!node.isCorrect())
                return null
        }
        return node
    }

    private fun isContainKDoc(text: String) =
            text.lines().any { it.trim().startsWith("/**") }
}
