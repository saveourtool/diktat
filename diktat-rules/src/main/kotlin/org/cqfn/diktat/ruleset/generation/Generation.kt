package org.cqfn.diktat.ruleset.generation

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.comments.HeaderCommentRule.Companion.afterCopyrightRegex
import org.cqfn.diktat.ruleset.rules.comments.HeaderCommentRule.Companion.curYear
import org.cqfn.diktat.ruleset.rules.comments.HeaderCommentRule.Companion.hyphenRegex

private val autoGenerationComment =
        """
            | This document was auto generated, please don't modify it.
            | This document contains all enum properties from Warnings.kt as Strings.
        """.trimMargin()

private val autoTable = """
\begin{center}
\scriptsize
\begin{longtable}{ |l|p{0.8cm}|p{0.8cm}| p{3cm} | }
\hline
\multicolumn{4}{|c|}{Table header} \\ 
\hline
\textbf{diKTat rule} & \textbf{code style} & \textbf{autofix} &  \textbf{config} \\
\hline
""".trimIndent()

private val autoEnd = """
\hline
\end{longtable}
\end{center}
""".trimIndent()

fun main() {
    generateWarningNames()
    generateAvailableRules()
    validateYear()
}

private fun generateWarningNames() {
    val enumValNames = Warnings.values().map { it.name }

    val propertyList = enumValNames.map {
        PropertySpec
                .builder(it, String::class)
                .addModifiers(KModifier.CONST)
                .initializer("\"$it\"")
                .build()
    }

    val fileBody = TypeSpec
            .objectBuilder("WarningNames")
            .addProperties(propertyList)
            .build()

    val kotlinFile = FileSpec
            .builder("generated", "WarningNames")
            .addType(fileBody)
            .indent("    ")
            .addComment(autoGenerationComment)
            .build()

    kotlinFile.writeTo(File("diktat-rules/src/main/kotlin"))  // fixme: need to add it to pom
}

@Suppress("MagicNumber")
private fun generateAvailableRules() {
    val ruleMap = File("info/rules-mapping.md").readLines().drop(2)
            .map { it.drop(1).dropLast(1).split("|") }
            .map { RuleDescription(it[0].replace("\\s+".toRegex(), ""), it[1], it[2], null) }
            .map { it.ruleName to it }.toMap()
    File("info/available-rules.md").readLines()
            .drop(2)
            .map { it.drop(1).dropLast(1).split("|") }
            .map { it[2].replace("\\s+".toRegex(), "") to it[5] }
            .forEach { ruleMap[it.first]?.config = it.second }
    val newText = File("wp/sections/appendix.tex").readLines().toMutableList()
    newText.removeAll(newText.subList(newText.indexOf("\\subsection{available-rules}") + 1,newText.indexOf("\\subsection{guide}")))
    var index = newText.indexOf("\\subsection{available-rules}") + 1
    autoTable.split("\n").forEach { newText.add(index++, it) }
    ruleMap.map { it.value }
            .map { "${it.correctRuleName} & ${it.correctCodeStyle} & ${it.autoFix} & ${it.correctConfig}\\\\" }
            .forEach { newText.add(index++, it) }
    autoEnd.split("\n").forEach { newText.add(index++, it) }
    File("wp/sections/appendix.tex").writeText(newText.joinToString(separator = "\n"))
}

@Suppress("UnsafeCallOnNullableType")
data class RuleDescription(val ruleName: String, val codeStyle: String, val autoFix: String, var config: String?) {
    val correctCodeStyle = codeStyle.substring(codeStyle.indexOf("[") + 1, codeStyle.indexOf("]"))
    val correctRuleName = ruleName.replace("_", "\\underline{ }")
    val correctConfig: String by lazy {
        config!!.replace("<br>", " ")
    }
}

private fun validateYear() {
    val file = File("diktat-rules/src/test/resources/test/paragraph2/header/CopyrightDifferentYearExpected.kt")
    val tempFile = createTempFile()
    tempFile.printWriter().use { writer ->
        file.forEachLine { line ->
            writer.println(when {
                hyphenRegex.matches(line) -> hyphenRegex.replace(line) {
                    val years = it.value.split("-")
                    val validYears = "${years[0]}-${curYear}"
                    line.replace(hyphenRegex, validYears)
                }
                afterCopyrightRegex.matches(line) -> afterCopyrightRegex.replace(line) {
                    val copyrightYears = it.value.split("(c)", "(C)", "©")
                    val validYears = "${copyrightYears[0]}-${curYear}"
                    line.replace(afterCopyrightRegex, validYears)
                }
                else -> line
            })
        }
    }
    file.delete()
    tempFile.renameTo(file)
}
