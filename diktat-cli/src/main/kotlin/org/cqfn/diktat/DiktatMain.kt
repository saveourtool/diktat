/**
 * The file contains main method
 */

package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatProcessorListener
import org.cqfn.diktat.cli.DiktatMode
import org.cqfn.diktat.cli.DiktatProperties
import org.cqfn.diktat.ktlint.DiktatBaselineFactoryImpl
import org.cqfn.diktat.ktlint.DiktatProcessorFactoryImpl
import org.cqfn.diktat.ktlint.DiktatReporterFactoryImpl
import org.cqfn.diktat.ruleset.rules.DiktatRuleConfigReaderImpl
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetFactoryImpl

import mu.KotlinLogging

import java.nio.file.Path
import java.nio.file.Paths

import kotlin.io.path.absolutePathString

private val log = KotlinLogging.logger { }

private val loggingListener = object : DiktatProcessorListener {
    override fun before(file: Path) {
        log.debug {
            "Start processing the file: $file"
        }
    }
}

fun main(args: Array<String>) {
    val diktatRunnerFactory = DiktatRunnerFactory(
        DiktatRuleConfigReaderImpl(),
        DiktatRuleSetFactoryImpl(),
        DiktatProcessorFactoryImpl(),
        DiktatBaselineFactoryImpl(),
        DiktatReporterFactoryImpl(),
    )
    val properties = DiktatProperties.parse(diktatRunnerFactory.diktatReporterFactory, args)
    properties.configureLogger()

    log.debug {
        "Loading diktatRuleSet using config ${properties.config}"
    }
    val currentFolder = Paths.get(".").toAbsolutePath().normalize()
    val diktatRunnerArguments = properties.toRunnerArguments(
        sourceRootDir = currentFolder,
        loggingListener = loggingListener,
    )

    val diktatRunner = diktatRunnerFactory(diktatRunnerArguments)
    when (properties.mode) {
        DiktatMode.CHECK -> diktatRunner.checkAll(diktatRunnerArguments)
        DiktatMode.FIX -> diktatRunner.fixAll(diktatRunnerArguments) { updatedFile ->
            log.warn {
                "Original and formatted content differ, writing to ${updatedFile.absolutePathString()}..."
            }
        }
    }
}
