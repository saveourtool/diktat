@file:Suppress("FILE_NAME_MATCH_CLASS")

package org.cqfn.diktat.generation.docs

import java.io.File

/**
 * Adds/updates diktat code style in white paper document.
 */
@Suppress(
        "LoopWithTooManyJumpStatements",
        "LongMethod",
        "MagicNumber",
        "ComplexMethod",
        "NestedBlockDepth",
        "WRONG_INDENTATION",
        "TOO_LONG_FUNCTION")
fun generateCodeStyle(guideDir: File, wpDir: File) {
    val file = File(guideDir, "diktat-coding-convention.md")
    val tempFile = File(wpDir, "convention.tex")
    val lines = file.readLines().toMutableList().drop(1)
    tempFile.printWriter().use { writer ->
        val iterator = lines.iterator()
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
                            2 -> writer.writeln("""\section*{\textbf{${line.removePrefix("##").trim()}}}""")
                            3 -> writer.writeln("""\subsection*{\textbf{${line.removePrefix("###").trim()}}}""")
                            4 -> writer.writeln("""\subsubsection*{\textbf{${line.removePrefix("####").trim()}}}${"\n"}\leavevmode\newline""")
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
                correctedString = handleHyperlinks(correctedString)
                correctedString = findBoldOrItalicText(BACKTICKS_TEXT, correctedString, FindType.BACKTICKS)

                writer.writeln(correctedString)
            }
        }
    }
    val appendixFileLines = File(wpDir, "sections/appendix.tex").readLines().toMutableList()
    appendixFileLines.removeAll(appendixFileLines.subList(appendixFileLines.indexOf("\\lstMakeShortInline[basicstyle=\\ttfamily\\bfseries]`"), appendixFileLines.lastIndex + 1))
    appendixFileLines.addAll(tempFile.readLines())
    File(wpDir, "sections/appendix.tex").writeText(appendixFileLines.joinToString(separator = "\n"))
    tempFile.delete()
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

private fun handleHyperlinks(line: String): String {
    var correctedString = line
    if (correctedString.contains(HYPERLINKS)) {
        val hyperlinkSubString = HYPERLINKS.findAll(correctedString)
        hyperlinkSubString.forEach {
            var (text, link) = HYPERLINKS.find(it.value)!!.destructured
            // need to replace back, because it is a hyperlink
            link = link.trim().drop(1).dropLast(1) // drop ( and )
            link = link.replace("\\#", "#")
            link = link.replace("\\&", "&")
            link = link.replace("\\_", "_")
            // need to replace ` in hyperlink, because it breaks latex compilation
            text = text.replace("`", "")
            text = text.trim().drop(1).dropLast(1) // dropping [ and ]
            val hyperlink = """\href{$link}{$text}"""
            correctedString = correctedString.replace(it.value, hyperlink)
        }
    }

    return correctedString
}

@Suppress("WRONG_INDENTATION")
private fun findBoldOrItalicText(regex: Regex,
                                 line: String,
                                 type: FindType): String {
    val allRegex = regex
            .findAll(line)
            .map { it.value }
            .toMutableList()
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
    var correctedLine = line.replace(regex, REGEX_PLACEHOLDER)
    allRegex.forEach {
        correctedLine = correctedLine.replaceFirst(REGEX_PLACEHOLDER, it)
    }
    return correctedLine
}
