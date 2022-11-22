package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatError
import org.cqfn.diktat.api.DiktatMode
import org.cqfn.diktat.cli.DiktatProperties
import org.cqfn.diktat.ktlint.unwrap
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.extension

fun main(args: Array<String>) {
    val properties = DiktatProperties.parse(args)
    properties.configureLogger()

    val reporter = properties.reporter()
    reporter.beforeAll()

    val currentFolder = Paths.get(".")
    properties.patterns
        .asSequence()
        .flatMap { pattern ->
            Files.newDirectoryStream(currentFolder, pattern).asSequence()
        }
        .map { file ->
            val result = mutableListOf<Pair<DiktatError, Boolean>>()
            DiktatProcessCommand.Builder()
                .file(file)
                .config(properties.config)
                .callback { error, isCorrected ->
                    result.add(error to isCorrected)
                }
                .isScript(!file.extension.endsWith(".kt", ignoreCase = true))
                .logLevel(properties.logLevel)
                .build()
                .let { command ->
                    when (properties.mode) {
                        DiktatMode.CHECK -> command.check()
                        DiktatMode.FIX -> command.check()
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
