/**
 * The file contains main method
 */

package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatError
import org.cqfn.diktat.api.DiktatMode
import org.cqfn.diktat.cli.DiktatProperties
import org.cqfn.diktat.common.utils.loggerWithKtlintConfig
import org.cqfn.diktat.ktlint.unwrap
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetFactory
import mu.KotlinLogging
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.PathWalkOption
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.walk
import kotlin.io.path.writeText

@Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
private val log = KotlinLogging.loggerWithKtlintConfig {}

typealias DiktatErrorWithCorrectionInfo = Pair<DiktatError, Boolean>

private fun String.tryToPath(): Path? = try {
    Paths.get(this).takeIf { it.exists() }
} catch (e: InvalidPathException) {
    null
}

@OptIn(ExperimentalPathApi::class)
@Suppress(
    "LongMethod",
    "TOO_LONG_FUNCTION"
)
fun main(args: Array<String>) {
    val properties = DiktatProperties.parse(args)
    properties.configureLogger()

    log.debug {
        "Loading diktatRuleSet using config ${properties.config}"
    }
    val diktatRuleSetFactory = DiktatRuleSetFactory(properties.config)
    val reporter = properties.reporter()
    reporter.beforeAll()

    log.debug {
        "Resolving files by patterns: ${properties.patterns}"
    }
    val currentFolder = Paths.get(".")
    properties.patterns
        .asSequence()
        .flatMap { pattern ->
            pattern.tryToPath()?.let { sequenceOf(it) }
                ?: run {
                    // create a matcher and return a filter that uses it.
                    val matcher = currentFolder.fileSystem.getPathMatcher("glob:$pattern")
                    currentFolder.walk(PathWalkOption.INCLUDE_DIRECTORIES)
                        .filter { matcher.matches(it) }
                }
        }
        .filter { file -> file.extension in setOf("kt", "kts") }
        .distinct()
        .map { it.normalize() }
        .map { file ->
            log.debug {
                "Start processing the file: $file"
            }
            val result: MutableList<DiktatErrorWithCorrectionInfo> = mutableListOf()
            DiktatProcessCommand.Builder()
                .file(file)
                .diktatRuleSetFactory(diktatRuleSetFactory)
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
