import java.io.File
import java.lang.StringBuilder

val GUIDE_PATTERN = "guide-chapter-"

val codeGuideParts = File(".")
        .listFiles()
        .filter { it.name.contains(GUIDE_PATTERN) }

val allChapters = codeGuideParts
        .sortedBy { it.nameWithoutExtension.replace(GUIDE_PATTERN, "").toInt() }
        .map { it.readLines() }
        .flatten()
        .joinToString("\n")

File("diktat-coding-convention.md").writeText(allChapters)


