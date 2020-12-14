@file:Suppress("FILE_NAME_MATCH_CLASS")

package org.cqfn.diktat.ruleset.generation

import org.cqfn.diktat.ruleset.utils.A4_PAPER_WIDTH
import org.cqfn.diktat.ruleset.utils.ANCHORS
import org.cqfn.diktat.ruleset.utils.BACKTICKS_TEXT
import org.cqfn.diktat.ruleset.utils.BOLD_TEXT
import org.cqfn.diktat.ruleset.utils.ITALIC_TEXT
import org.cqfn.diktat.ruleset.utils.NUMBER_IN_TAG
import org.cqfn.diktat.ruleset.utils.REGEX_PLACEHOLDER
import org.cqfn.diktat.ruleset.utils.RULE_NAME
import org.cqfn.diktat.ruleset.utils.TABLE_COLUMN_NAMES
import org.cqfn.diktat.ruleset.utils.format
import org.cqfn.diktat.ruleset.utils.writeCode
import org.cqfn.diktat.ruleset.utils.writeln
import java.io.File

fun main() {
    generateCodeStyle()
}

@Suppress(
        "LoopWithTooManyJumpStatements",
        "LongMethod",
        "ComplexMethod",
        "NestedBlockDepth",
        "WRONG_INDENTATION",
        "TOO_LONG_FUNCTION")
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
            if (line.contains("<!--")) {
                // for now there are no multiline comments in our doc
                continue
            }
            if (line.startsWith("#")) {
                val number = NUMBER_IN_TAG
                        .find(line)
                        ?.value
                        ?.trim('"')
                        ?.substring(1)
                val name = RULE_NAME
                        .find(line)
                        ?.value
                        ?.removePrefix("</a>")
                        ?.trim()
                if (name.isNullOrEmpty() || number.isNullOrEmpty()) {
                    if (number.isNullOrEmpty() && name.isNullOrEmpty()) {
                        when (line.takeWhile { it == '#' }.count()) {
                            1 -> writer.writeln("""\section*{\textbf{${line.removePrefix("#").trim()}}}""")
                            2 -> writer.writeln("""\subsection*{\textbf{${line.removePrefix("##").trim()}}}""")
                            3 -> writer.writeln("""\subsubsection*{\textbf{${line.removePrefix("###").trim()}}}${"\n"}\leavevmode\newline""")
                            else -> {}
                        }
                        continue
                    }
                    error("String starts with # but has no number or name - $line")
                }
                when (number.count { it == '.' }) {
                    0 -> writer.writeln("""\section*{\textbf{$name}}""")
                    1 -> writer.writeln("""\subsection*{\textbf{$name}}""")
                    2 -> writer.writeln("""\subsubsection*{\textbf{$name}}${"\n"}\leavevmode\newline""")
                    else -> {}
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
                val columnNumber = line.count { it == '|' } - 1
                val columnWidth: Float = (A4_PAPER_WIDTH / columnNumber)  // For now it makes all column width equal
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
                while (iterator.hasNext() && line.trim().startsWith("|")) {
                    writer.writeln(line
                            .replace("&", "\\&")
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
                var correctedString = findBoldOrItalicText(BOLD_TEXT, line, FindType.BOLD)
                correctedString = findBoldOrItalicText(ITALIC_TEXT, correctedString, FindType.ITALIC)
                correctedString = correctedString.replace(ANCHORS, "")
                correctedString = correctedString.replace("#", "\\#")
                correctedString = correctedString.replace("&", "\\&")
                correctedString = correctedString.replace("_", "\\_")
                // find backticks should be the last to replace \_ with _
                correctedString = findBoldOrItalicText(BACKTICKS_TEXT, correctedString, FindType.BACKTICKS)
                writer.writeln(correctedString)
            }
        }
    }
    val appendixFileLines = File("wp/sections/appendix.tex").readLines().toMutableList()
    appendixFileLines.removeAll(appendixFileLines.subList(appendixFileLines.indexOf("\\section*{guide}"), appendixFileLines.lastIndex + 1))
    appendixFileLines.addAll(tempFile.readLines())
    File("wp/sections/appendix.tex").writeText(appendixFileLines.joinToString(separator = "\n"))
}

/**
 * Type of text in markdown to be written in latex
 */
enum class FindType {
    BACKTICKS,
    BOLD,
    ITALIC,
    ;
}

@Suppress("WRONG_INDENTATION")
private fun findBoldOrItalicText(regex: Regex,
                                 line: String,
                                 type: FindType): String {
    val allRegex = regex
            .findAll(line)
            .map { it.value }
            .toMutableList()
    var correctedLine = line.replace(regex, REGEX_PLACEHOLDER)
    allRegex.forEachIndexed { index, value ->
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
        correctedLine = correctedLine.replaceFirst(REGEX_PLACEHOLDER, it)
    }
    return correctedLine
}
