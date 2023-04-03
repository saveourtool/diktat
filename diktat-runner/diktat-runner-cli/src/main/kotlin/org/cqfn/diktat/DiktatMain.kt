/**
 * The file contains main method
 */

package org.cqfn.diktat

import org.cqfn.diktat.cli.DiktatMode
import org.cqfn.diktat.api.DiktatProcessorListener
import org.cqfn.diktat.cli.DiktatProperties
import org.cqfn.diktat.ktlint.DiktatProcessorFactoryImpl
import org.cqfn.diktat.ktlint.DiktatReporterFactoryImpl
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetFactoryImpl
import org.cqfn.diktat.ruleset.utils.isKotlinCodeOrScript
import org.cqfn.diktat.util.tryToPathIfExists
import org.cqfn.diktat.util.walkByGlob
import mu.KotlinLogging
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.io.path.writeText

private val log = KotlinLogging.logger { }

@Suppress(
    "LongMethod",
    "TOO_LONG_FUNCTION"
)
fun main(args: Array<String>) {
    val diktatReporterFactory = DiktatReporterFactoryImpl()
    val properties = DiktatProperties.parse(diktatReporterFactory, args)
    properties.configureLogger()

    log.debug {
        "Loading diktatRuleSet using config ${properties.config}"
    }
    val diktatRuleSet = DiktatRuleSetFactoryImpl().create(properties.config)
    val diktatProcessor = DiktatProcessorFactoryImpl().invoke(diktatRuleSet)
    val currentFolder = Paths.get(".")
    val reporter = properties.reporter(diktatReporterFactory, currentFolder)

    log.debug {
        "Resolving files by patterns: ${properties.patterns}"
    }
    val files = properties.patterns
        .asSequence()
        .flatMap { pattern ->
            pattern.tryToPathIfExists()?.let { sequenceOf(it) }
                ?: currentFolder.walkByGlob(pattern)
        }
        .filter { file -> file.isKotlinCodeOrScript() }
        .distinct()
        .map { it.normalize() }

    val loggingListener = object : DiktatProcessorListener.Companion.Empty() {
        override fun before(file: Path) {
            log.debug {
                "Start processing the file: $file"
            }
        }
    }
    when (properties.mode) {
        DiktatMode.CHECK -> diktatProcessor.checkAll(
            listener = DiktatProcessorListener(loggingListener, reporter),
            files = files,
        )
        DiktatMode.FIX -> diktatProcessor.fixAll(
            listener = DiktatProcessorListener(loggingListener, reporter),
            files = files,
        ) { file, formatterText ->
            if (file.readText(StandardCharsets.UTF_8) != formatterText) {
                file.writeText(formatterText, Charsets.UTF_8)
            }
        }
    }
}
