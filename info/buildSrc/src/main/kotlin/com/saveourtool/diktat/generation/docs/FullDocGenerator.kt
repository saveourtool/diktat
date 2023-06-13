package com.saveourtool.diktat.generation.docs

import java.io.File

/**
 * Compiles individual chapters of code style into a single document.
 */
fun generateFullDoc(guideDir: File, fullDocFileName: String) {
    val GUIDE_PATTERN = "guide-chapter-"

    val codeGuideParts = guideDir
        .listFiles()!!
        .filter { it.name.contains(GUIDE_PATTERN) }

    val allChapters = codeGuideParts
        .sortedBy { it.nameWithoutExtension.replace(GUIDE_PATTERN, "").toInt() }
        .map { it.readLines() }
        .flatten()
        .joinToString("\n")

    val tableOfContent = File(guideDir, "guide-TOC.md")
        .readText()

    File(guideDir, fullDocFileName).writeText("$tableOfContent\n\n$allChapters")
}
