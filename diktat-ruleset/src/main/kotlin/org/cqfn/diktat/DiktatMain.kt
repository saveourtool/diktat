/**
 * The file contains main method
 */

package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatError
import org.cqfn.diktat.api.DiktatMode
import org.cqfn.diktat.cli.DiktatProperties
import org.cqfn.diktat.common.utils.loggerWithKtlintConfig
import org.cqfn.diktat.ktlint.unwrap
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import org.cqfn.diktat.ruleset.utils.isKotlinCodeOrScript
import org.cqfn.diktat.util.tryToPathIfExists
import org.cqfn.diktat.util.walkByGlob
import mu.KotlinLogging
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

@Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
private val log = KotlinLogging.loggerWithKtlintConfig {}

typealias DiktatErrorWithCorrectionInfo = Pair<DiktatError, Boolean>

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
    val diktatRuleSetFactory = DiktatRuleSetProvider(properties.config)
    val reporter = properties.reporter()
    reporter.beforeAll()

    log.debug {
        "Resolving files by patterns: ${properties.patterns}"
    }
    val currentFolder = Paths.get(".")
    properties.patterns
        .asSequence()
        .flatMap { pattern ->
            pattern.tryToPathIfExists()?.let { sequenceOf(it) }
                ?: currentFolder.walkByGlob(pattern)
        }
        .filter { file -> file.isKotlinCodeOrScript() }
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
