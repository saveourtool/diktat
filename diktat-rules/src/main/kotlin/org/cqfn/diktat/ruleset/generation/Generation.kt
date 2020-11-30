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
import org.cqfn.diktat.ruleset.utils.format
import org.cqfn.diktat.ruleset.utils.writeCode
import org.cqfn.diktat.ruleset.utils.writeln

private val autoGenerationComment =
        """
            | This document was auto generated, please don't modify it.
            | This document contains all enum properties from Warnings.kt as Strings.
        """.trimMargin()

fun main() {
    generateWarningNames()
    generateCodeStyle()
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

val NUMBER_IN_TAG = Regex("\"([a-z0-9.]*)\"") // finds "r1.0.2"
val RULE_NAME = Regex("(</a>[A-Za-z 0-9.-]*)") // get's rule name from ### <a>...</a> Rule name
val BOLD_TEXT = Regex("""\*\*([^*]+)\*\*""") // finds bold text in regular lines
val ITALIC_TEXT = Regex("""\*([A-Za-z ]+)\*""") // finds italic text in regular lines
val BACKTICKS_TEXT = Regex("""`([^`]*)`""") // finds backtick in regular text (not used for now, may be we will need to use it in future)
val ANCHORS = Regex("""\(#(.*)\)""") // finds anchors on rules and deletes them
val TABLE_COLUMN_NAMES = Regex("""[A-Za-z ]*""")  // used to find column names in tables only

private fun generateCodeStyle() {
    val file = File("info/guide/diktat-coding-convention.md")
    val tempFile = File("info/guide/convention.tex")
    val lines = file.readLines().toMutableList()
    tempFile.printWriter().use { writer ->
        val iterator = lines.iterator()
        writer.writeln("\\section*{guide}")
        writer.writeln("\\lstMakeShortInline[basicstyle=\\ttfamily\\bfseries]`")
        while (iterator.hasNext()) {
            var line = iterator.next()
            if (line.contains("<!--")) // for now there are no multiline comments in our doc
                continue
            if (line.startsWith("#")) {
                val number = NUMBER_IN_TAG.find(line)?.value?.trim('"')?.substring(1)
                val name = RULE_NAME.find(line)?.value?.removePrefix("</a>")?.trim()
                if (name.isNullOrEmpty() || number.isNullOrEmpty())
                    error("String starts with # but has no number or name - $line")
                when(number.count { it == '.' }) {
                    0 -> writer.writeln("""\section*{\textbf{$name}}""")
                    1 -> writer.writeln("""\subsection*{\textbf{$name}}""")
                    2 -> writer.writeln("""\subsubsection*{\textbf{$name}}${"\n"}\leavevmode\newline""")
                }
                continue
            }
            if (iterator.hasNext() && line.trim().startsWith("```")) {
                writer.writeCode("""\begin{lstlisting}[language=Kotlin]""")
                line = iterator.next()
                while (!line.trim().startsWith("```")) {
                    writer.writeCode(line)
                    line = iterator.next()
                }
                writer.writeCode("""\end{lstlisting}""")
                continue
            }

            if (line.trim().startsWith("|")) {
                val columnNumber = line.count { it =='|' } - 1
                val columnWidth: Float = (15f / columnNumber) // For now it makes all column width equal
                val createTable = "|p{${columnWidth.format(1)}cm}".repeat(columnNumber).plus("|")
                writer.writeln("""\begin{center}""")
                writer.writeln("""\begin{tabular}{ $createTable }""")
                writer.writeln("""\hline""")
                val columnNames = TABLE_COLUMN_NAMES
                        .findAll(line)
                        .filter { it.value.isNotEmpty() }
                        .map { it.value.trim() }
                        .toList()
                writer.write(columnNames.joinToString(separator = "&"))
                writer.writeln("""\\""")
                writer.writeln("\\hline")
                iterator.next()
                line = iterator.next()
                while(iterator.hasNext() && line.trim().startsWith("|")) {
                    writer.writeln(line
                            .replace('|', '&')
                            .drop(1)
                            .dropLast(1)
                            .plus("""\\""")
                            .replace("_", "\\_")
                            .replace(ANCHORS, ""))
                    line = iterator.next()
                }
                writer.writeln("""\hline""")
                writer.writeln("\\end{tabular}")
                writer.writeln("\\end{center}")
            } else {
                var correctedString = findBoldOrItalicText(line, BOLD_TEXT, FindType.BOLD)
                correctedString = findBoldOrItalicText(correctedString, ITALIC_TEXT, FindType.ITALIC)
                correctedString = correctedString.replace(ANCHORS, "")
                correctedString = correctedString.replace("#", "\\#")
                correctedString = correctedString.replace("&", "\\&")
                correctedString = correctedString.replace("_", "\\_")
                // find backticks should be the last to replace \_ with _
                correctedString = findBoldOrItalicText(correctedString, BACKTICKS_TEXT, FindType.BACKTICKS)
                writer.writeln(correctedString)
            }
        }
    }
    val appendixFileLines = File("wp/sections/appendix.tex").readLines().toMutableList()
    appendixFileLines.removeAll(appendixFileLines.subList(appendixFileLines.indexOf("\\section*{guide}"), appendixFileLines.lastIndex + 1))
    appendixFileLines.addAll(tempFile.readLines())
    File("wp/sections/appendix.tex").writeText(appendixFileLines.joinToString(separator = "\n"))
}

enum class FindType {
    BOLD,
    ITALIC,
    BACKTICKS
}

private fun findBoldOrItalicText(line: String, regex: Regex, type: FindType) : String {
    val allRegex = regex.findAll(line).map { it.value }.toMutableList()
    var correctedLine = line.replace(regex, "RE_PL_AC_E_ME")
    allRegex.forEachIndexed {index, value ->
        when (type) {
            FindType.BOLD -> allRegex[index] = "\\textbf{${value.replace("**", "")}}"
            FindType.ITALIC -> allRegex[index] = "\\textit{${value.replace("*", "")}}"
            /*
                replaces \_ with _. In default latex we needed to place \ before _.
                But because of lstMakeShortInline[basicstyle=\ttfamily\bfseries]`
                it isn't needed.
             */
            FindType.BACKTICKS -> allRegex[index] = value.replace("\\_", "_")
        }
    }
    allRegex.forEach {
        correctedLine = correctedLine.replaceFirst("RE_PL_AC_E_ME", it)
    }
    return correctedLine
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
