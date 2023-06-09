@file:Suppress("FILE_NAME_MATCH_CLASS")

package com.saveourtool.diktat.generation.docs

import java.io.File
import java.io.PrintWriter


/**
 * Adds/updates diktat code style in white paper document.
 */
@Suppress(
        "LoopWithTooManyJumpStatements",
        "LongMethod",
        "MagicNumber",
        "ComplexMethod",
        "NestedBlockDepth",
        "TOO_LONG_FUNCTION")
fun generateCodeStyle(guideDir: File, wpDir: File) {
    val file = File(guideDir, "diktat-coding-convention.md")
    val tempFile = File(wpDir, "convention.tex")
    val lines = file.readLines().toMutableList().drop(1)
    tempFile.printWriter().use { writer ->
        val iterator = lines.iterator()
        writer.writeWithoutApostrophe("%CodeStyle")
        while (iterator.hasNext()) {
            val line = iterator.next()
            if (line.contains("## <a name=\"c0\"></a> Preface"))
                break
            when {
                line.startsWith("#") -> {
                    writer.writeWithoutApostrophe("\\section*{${line.removePrefix("#").trim()}}")
                }
                line.startsWith("*") -> {
                    writeTableContentLine(writer, line, 0.5)
                }
                line.startsWith(" ") -> {
                    writeTableContentLine(writer, line, 1.0)
                }
                else -> {
                    writeTableContentLine(writer, line, 0.0)
                }
            }
        }

        while (iterator.hasNext()) {
            var line = iterator.next()
            if (line.contains("<!--")) {
                // for now there are no multiline comments in our doc
                continue
            } else if (line.startsWith("#")) {
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
                            1 -> writer.writeWithoutApostrophe("""\section*{\textbf{${line.removePrefix("#").trim()}}}""")
                            2 -> writer.writeWithoutApostrophe("""\section*{\textbf{${line.removePrefix("##").trim()}}}""")
                            3 -> writer.writeWithoutApostrophe("""\subsection*{\textbf{${line.removePrefix("###").trim()}}}""")
                            4 -> writer.writeWithoutApostrophe("""\subsubsection*{\textbf{${line.removePrefix("####").trim()}}}${"\n"}\leavevmode\newline""")
                            else -> {}
                        }
                        continue
                    }
                    error("String starts with # but has no number or name - $line")
                }
                when (number.count { it == '.' }) {
                    0 -> writer.writeWithoutApostrophe("""\section*{\textbf{$name}}""")
                    1 -> writer.writeWithoutApostrophe("""\subsection*{\textbf{$name}}""")
                    2 -> writer.writeWithoutApostrophe("""\subsubsection*{\textbf{$name}}${"\n"}\leavevmode\newline""")
                    else -> {}
                }
                writer.writeWithoutApostrophe("\\label{sec:${name.getFirstNumber()}}")

                continue
            } else if (iterator.hasNext() && line.trim().startsWith("```")) {
                writer.writeCode("""\begin{lstlisting}[language=Kotlin]""")
                line = iterator.next()
                while (!line.trim().startsWith("```")) {
                    writer.writeCode(line)
                    line = iterator.next()
                }
                writer.writeCode("""\end{lstlisting}""")
                continue
            } else if (line.trim().startsWith("|")) {
                val columnNumber = line.count { it == '|' } - 1
                val columnWidth: Float = (A4_PAPER_WIDTH / columnNumber)  // For now it makes all column width equal
                val createTable = "|p{${columnWidth.format(1)}cm}".repeat(columnNumber).plus("|")
                writer.writeWithoutApostrophe("""\begin{center}""")
                writer.writeWithoutApostrophe("""\begin{tabular}{ $createTable }""")
                writer.writeWithoutApostrophe("""\hline""")
                val columnNames = TABLE_COLUMN_NAMES
                        .findAll(line)
                        .filter { it.value.isNotEmpty() }
                        .map { it.value.trim() }
                        .toList()
                writer.write(columnNames.joinToString(separator = "&"))
                writer.writeWithoutApostrophe("""\\""")
                writer.writeWithoutApostrophe("\\hline")
                iterator.next()
                line = iterator.next()
                while (iterator.hasNext() && line.trim().startsWith("|")) {
                    writer.writeWithoutApostrophe(line
                            .replace("&", "\\&")
                            .replace('|', '&')
                            .drop(1)
                            .dropLast(1)
                            .plus("""\\""")
                            .replace("_", "\\_")
                            .replace(ANCHORS, ""))
                    line = iterator.next()
                }
                writer.writeWithoutApostrophe("""\hline""")
                writer.writeWithoutApostrophe("\\end{tabular}")
                writer.writeWithoutApostrophe("\\end{center}")
            } else {
                writer.writeWithoutApostrophe(line, false)
            }
        }
    }
    val appendixFileLines = File(wpDir, "sections/appendix.tex")
            .readLines()
            .takeWhile { it != "%CodeStyle" }
            .toMutableList()
    appendixFileLines.addAll(tempFile.readLines())
    File(wpDir, "sections/appendix.tex").writeText(appendixFileLines.joinToString(separator = "\n"))
    tempFile.delete()
}

private fun PrintWriter.writeWithoutApostrophe(text: String, isCommand: Boolean = true) {
    this.writeln(text.replaceApostrophe(isCommand))
}

private fun String.replaceApostrophe(isCommand: Boolean): String {
    if (isCommand){
        return this
    }
    var correctedString = this
    if (correctedString.contains("\\") && (correctedString.contains("""\\[a-zA-Z]""".toRegex()))) {
        correctedString = correctedString.replace("\\", "\\textbackslash ")
    }
    correctedString = correctedString.replace("{", "\\{")
    correctedString = correctedString.replace("}", "\\}")
    correctedString = correctedString.replace("_", "\\_")
    correctedString = correctedString.replace(ANCHORS, "")
    correctedString = correctedString.replace("#", "\\#")
    correctedString = correctedString.replace("&", "\\&")
    correctedString = handleHyperlinks(correctedString)
    correctedString = findBoldOrItalicText(BOLD_TEXT, correctedString, FindType.BOLD)
    correctedString = findBoldOrItalicText(ITALIC_TEXT, correctedString, FindType.ITALIC)
    correctedString = findBoldOrItalicText(BACKTICKS_TEXT, correctedString, FindType.BACKTICKS)
    return correctedString
}

private fun writeTableContentLine(writer: PrintWriter, line: String, numbOfSpaces: Double) {
    writer.write("\\hspace{${numbOfSpaces}cm}")
    val correctLine = line
            .trim()
            .replace("[", "")
            .replace("]", "")
            .replace("*", "")
            .replace(ANCHORS, "")
            //.replace("_", "\\_")
            .replace("#", "\\#")
            .replace("&", "\\&")
    writer.writeWithoutApostrophe("\\hyperref[sec:${correctLine.getFirstNumber()}]{$correctLine}")
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
            //link = link.replace("\\_", "_")
            // need to replace ` in hyperlink, because it breaks latex compilation
            text = text.replace("`", "")
            text = text.trim().drop(1).dropLast(1) // dropping [ and ]
            val hyperlink = """\href{$link}{$text}"""
            correctedString = correctedString.replace(it.value, hyperlink)
        }
    }

    return correctedString
}

private fun findBoldOrItalicText(regex: Regex,
                                 line: String,
                                 type: FindType): String {
    val allRegex = regex
            .findAll(line)
            .map { it.value }
            .toMutableList()
    allRegex.forEachIndexed { index, value ->
        when (type) {
            FindType.BOLD, FindType.BACKTICKS -> allRegex[index] = "\\textbf{${value.replace("**", "").replace("`", "")}}"
            FindType.ITALIC -> allRegex[index] = "\\textit{${value.replace("*", "")}}"
        }
    }
    var correctedLine = line.replace(regex, REGEX_PLACEHOLDER)
    allRegex.forEach {
        correctedLine = correctedLine.replaceFirst(REGEX_PLACEHOLDER, it)
    }
    return correctedLine
}

private fun String.getFirstNumber() = trimStart().takeWhile { it.isDigit() || it == '.' }
