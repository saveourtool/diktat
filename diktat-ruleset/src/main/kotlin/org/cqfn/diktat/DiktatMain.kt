package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatError
import org.cqfn.diktat.api.DiktatMode
import org.cqfn.diktat.cli.DiktatProperties
import org.cqfn.diktat.ktlint.unwrap
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.writeText

fun main(args: Array<String>) {
    val properties = DiktatProperties.parse(args)
    properties.configureLogger()

    val reporter = properties.reporter()
    reporter.beforeAll()

    val currentFolder = Paths.get(".")
    properties.patterns
        .asSequence()
        .flatMap { pattern ->
            pattern.tryToPath()?.let { sequenceOf(it) }
                ?: currentFolder.listDirectoryEntries(pattern).asSequence()
        }
        .filter { file -> file.extension in setOf("kt", "kts") }
        .distinct()
        .map { file ->
            val result = mutableListOf<Pair<DiktatError, Boolean>>()
            DiktatProcessCommand.Builder()
                .file(file)
                .config(properties.config)
                .callback { error, isCorrected ->
                    result.add(error to isCorrected)
                }
                .build()
                .let { command ->
                    when (properties.mode) {
                        DiktatMode.CHECK -> command.check()
                        DiktatMode.FIX -> {
                            val formattedFileContent = command.fix()
                            file.writeText(formattedFileContent, Charsets.UTF_8)
                        }
                    }
                }
            file to result
        }
        .forEach { (file, result) ->
            reporter.before(file.absolutePathString())
            result.forEach { (error, isCorrected) ->
                reporter.onLintError(file.absolutePathString(), error.unwrap(), isCorrected)
            }
            reporter.after(file.absolutePathString())
        }
    reporter.afterAll()
}

private fun String.tryToPath(): Path? = try {
    Paths.get(this).takeIf { it.exists() }
} catch (e: InvalidPathException) {
    null
}
